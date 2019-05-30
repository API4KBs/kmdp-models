/**
 * Copyright © 2018 Mayo Clinic (RSTKNOWLEDGEMGMT@mayo.edu)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.omg.spec.api4kp._1_0.services.language;

import static edu.mayo.kmdp.util.Util.isEmpty;

import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.krformat._2018._08.SerializationFormat;
import edu.mayo.ontology.taxonomies.krlanguage._2018._08.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krprofile._2018._08.KnowledgeRepresentationLanguageProfile;
import edu.mayo.ontology.taxonomies.krserialization._2018._08.KnowledgeRepresentationLanguageSerialization;
import edu.mayo.ontology.taxonomies.lexicon._2018._08.Lexicon;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public class ModelMIMECoder {

  public static final String regexp = FileUtil
      .readStatic("/model.mime.regexp", ModelMIMECoder.class);

  private static Pattern rxPattern = Pattern.compile(regexp);

  public static String encode(SyntacticRepresentation rep) {
    return encode(rep, true);
  }

  public static String encode(SyntacticRepresentation rep, boolean withVersions) {
    StringBuilder sb = new StringBuilder("model/");
    if (rep.getLanguage() != null) {
      String langTag = withVersions
          ? rep.getLanguage().getTag()
          : rep.getLanguage().getTag().substring(0, rep.getLanguage().getTag().indexOf("-"));
      sb.append(langTag);
    }
    if (rep.getProfile() != null) {
      sb.append("[").append(rep.getProfile().getTag()).append("]");
    }
    if (rep.getSerialization() != null) {
      String serTag = rep.getSerialization().getTag().startsWith(rep.getLanguage().getTag())
          ? rep.getSerialization().getTag().substring(rep.getLanguage().getTag().length() + 1)
          : rep.getSerialization().getTag();
      sb.append("+").append(serTag);
    } else if (rep.getFormat() != null) {
      sb.append("+").append(rep.getFormat().getTag());
    }
    if (!rep.getLexicon().isEmpty()) {
      sb.append(";lex=").append("{");
      rep.getLexicon().forEach((l) -> sb.append(l.getTag()).append(","));
      sb.replace(sb.length() - 1, sb.length(), "}");
    }

    if (!rxPattern.matcher(sb.toString()).matches()) {
      throw new IllegalStateException("Invalid constructed MIME code " + sb.toString());
    }

    return sb.toString();
  }

  public static Optional<SyntacticRepresentation> decode(String mime) {
    Matcher matcher = rxPattern.matcher(mime);
    if (!matcher.matches()) {
      return Optional.empty();
    }

    SyntacticRepresentation rep = new SyntacticRepresentation();

    String langTag = isEmpty(matcher.group(1)) ? "" : matcher.group(1).trim();
    String langVerTag = isEmpty(matcher.group(2)) ? "" : matcher.group(2).trim()
        .replaceAll("-", "");
    String versionedLangTag = langTag + (isEmpty(langVerTag) ? "" : ("-" + langVerTag));

    String profTag = isEmpty(matcher.group(3)) ? "" : matcher.group(3).trim()
        .replaceAll("\\]", "")
        .replaceAll("\\[", "");

    String serialTag = isEmpty(matcher.group(4)) ? "" : matcher.group(4).trim()
        .replaceAll("\\+", "");
    String fullSerialTag = isEmpty(matcher.group(4)) ? "" :
        (Util.isEmpty(versionedLangTag) ? "" : (versionedLangTag + "+" + serialTag));

    String formatTag = Util.isEmpty(matcher.group(4)) ? "" : matcher.group(4)
        .trim().replaceAll("\\+", "");

    String lexTags = isEmpty(matcher.group(5)) ? "" : matcher.group(5).trim()
        .replaceAll("\\}", "")
        .replaceAll("\\{", "")
        .replaceAll("\\;lex=", "");

    if (!isEmpty(langVerTag)) {
      KnowledgeRepresentationLanguage.resolve(versionedLangTag)
          .ifPresent(rep::setLanguage);
    } else {
      String tag = langTag + "-";
      Arrays.stream(KnowledgeRepresentationLanguage.values())
          .map(KnowledgeRepresentationLanguage::getTag)
          .filter((t) -> t.startsWith(tag))
          .findFirst()
          .flatMap(KnowledgeRepresentationLanguage::resolve)
          .ifPresent(rep::setLanguage);
    }

    if (!isEmpty(profTag)) {
      KnowledgeRepresentationLanguageProfile.resolve(profTag)
          .ifPresent(rep::setProfile);
    }

    if (!isEmpty(serialTag)) {
      KnowledgeRepresentationLanguageSerialization.resolve(serialTag)
          .ifPresent(rep::setSerialization);
    }

    rep.setFormat(SerializationFormat.resolve(formatTag).orElse(SerializationFormat.TXT));

    if (!isEmpty(lexTags)) {
      Arrays.stream(lexTags.split(",")).forEach((l) -> {
        Lexicon.resolve(l).ifPresent(rep::withLexicon);
      });
    }

    return Optional.of(rep);
  }
}
