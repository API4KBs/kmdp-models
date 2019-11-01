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
package org.omg.spec.api4kp._1_0.services.tranx;

import static edu.mayo.kmdp.util.Util.isEmpty;
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.TXT;

import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfileSeries;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import edu.mayo.ontology.taxonomies.lexicon.LexiconSeries;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelMIMECoder {

  private static Logger logger = LoggerFactory.getLogger(ModelMIMECoder.class);

  protected ModelMIMECoder() {
  }

  private static final String REGEXP = FileUtil
      .readStatic("/model.mime.regexp", ModelMIMECoder.class);

  private static Pattern rxPattern = Pattern.compile(REGEXP);

  public static String encode(SyntacticRepresentation rep) {
    return encode(rep, true);
  }

  public static String encode(SyntacticRepresentation rep, boolean withVersions) {
    StringBuilder sb = new StringBuilder("model/");
    if (rep.getLanguage() != null) {
      String langTag = withVersions
          ? rep.getLanguage().getTag()
          : rep.getLanguage().getTag().substring(0, rep.getLanguage().getTag().indexOf('-'));
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
      rep.getLexicon().forEach(l -> sb.append(l.getTag()).append(","));
      sb.replace(sb.length() - 1, sb.length(), "}");
    }

    if (!rxPattern.matcher(sb.toString()).matches()) {
      throw new IllegalStateException("Invalid constructed MIME code " + sb.toString());
    }

    return sb.toString();
  }

  public static Optional<SyntacticRepresentation> decode(final String mime,
      final KnowledgeRepresentationLanguage defaultLanguage) {
    return decode(mime)
        .map(rep -> {
          if (rep.getLanguage() == null) {
            rep.setLanguage(defaultLanguage);
          }
          return rep;
        });
  }

  public static Optional<SyntacticRepresentation> decode(String mime) {
    return decompose(mime)
        .map(t -> {
          SyntacticRepresentation rep = new SyntacticRepresentation();

          if (!isEmpty(t.langVerTag)) {
            KnowledgeRepresentationLanguageSeries.resolve(t.versionedLangTag)
                .ifPresent(rep::setLanguage);
          } else {
            String tag = t.langTag + "-";
            Optional<KnowledgeRepresentationLanguage> lang = Arrays
                .stream(KnowledgeRepresentationLanguageSeries.values())
                .map(KnowledgeRepresentationLanguage::getTag)
                .filter(x -> x.startsWith(tag))
                .findFirst()
                .flatMap(KnowledgeRepresentationLanguageSeries::resolve);
            lang.ifPresent(rep::setLanguage);
            if (!lang.isPresent()) {
              SerializationFormatSeries.resolve(t.langTag)
                  .ifPresent(rep::setFormat);
            }
          }

          if (!isEmpty(t.profTag)) {
            KnowledgeRepresentationLanguageProfileSeries.resolve(t.profTag)
                .ifPresent(rep::setProfile);
          }

          if (!isEmpty(t.serialTag)) {
            KnowledgeRepresentationLanguageSerializationSeries.resolve(t.serialTag)
                .ifPresent(rep::setSerialization);
          }

          if (rep.getFormat() == null) {
            rep.setFormat(SerializationFormatSeries.resolve(t.formatTag).orElse(TXT));
          }

          if (!isEmpty(t.lexTags)) {
            Arrays.stream(t.lexTags.split(","))
                .forEach(l -> LexiconSeries.resolve(l)
                    .ifPresent(rep::withLexicon));
          }

          return rep;
        });
  }

  private static class LangTags {

    String langTag;
    String langVerTag;
    String versionedLangTag;
    String profTag;
    String serialTag;
    String formatTag;
    String lexTags;
  }

  private static Optional<LangTags> decompose(String mime) {
    if (Util.isEmpty(mime)) {
      return Optional.empty();
    }
    Matcher matcher = rxPattern.matcher(mime);
    if (!matcher.matches()) {
      return Optional.empty();
    }

    LangTags tags = new LangTags();
    tags.langTag = isEmpty(matcher.group(1)) ? "" : matcher.group(1).trim();
    tags.langVerTag = isEmpty(matcher.group(2)) ? "" : matcher.group(2).trim()
        .replace("-", "");
    tags.versionedLangTag =
        tags.langTag + (isEmpty(tags.langVerTag) ? "" : ("-" + tags.langVerTag));

    tags.profTag = isEmpty(matcher.group(3)) ? "" : matcher.group(3).trim()
        .replace("]", "")
        .replace("[", "");

    tags.serialTag = isEmpty(matcher.group(4)) ? "" : matcher.group(4).trim()
        .replaceAll("\\+", "");

    tags.formatTag = Util.isEmpty(matcher.group(4)) ? "" : matcher.group(4)
        .trim().replaceAll("\\+", "");

    tags.lexTags = isEmpty(matcher.group(5)) ? "" : matcher.group(5).trim()
        .replace("}", "")
        .replace("{", "")
        .replace(";lex=", "");

    return Optional.of(tags);
  }


  public static List<String> splitCodes(String xAccept) {
    if (Util.isEmpty(xAccept)) {
      return Collections.emptyList();
    }
    return Arrays.stream(xAccept.split(","))
        .map(String::trim)
        .map(WeightedCode::new)
        .sorted()
        .map(x -> x.code)
        .collect(Collectors.toList());
  }

  public static Optional<String> toModelCode(String s,
      KnowledgeRepresentationLanguage defaultLanguage) {
    int index = s.indexOf('/');
    if (index < 0) {
      return Optional.empty();
    }
    String space = s.substring(0, index);
    switch (space) {
      case "application":
      case "text":
        String c = "model/" + s.substring(index + 1);
        return decode(c, defaultLanguage)
            .map(ModelMIMECoder::encode);
      case "model":
        return Optional.of(s);
      case "*/*":
        return Optional.of(s);
      case "image":
      case "audio":
      case "video":
      case "example":
      case "font":
      default:
        logger.error("[Defensive] : Unsupported MIME type : {}", s);
        return Optional.empty();
    }
  }

  public static class WeightedCode implements Comparable<WeightedCode> {

    String code;
    float w;

    public WeightedCode(String wcode) {
      int index = wcode.lastIndexOf(';');
      if (index > 0) {
        code = wcode.substring(0, index);
        w = Float.parseFloat(wcode.substring(index + 1).replace("q=", ""));
      } else {
        code = wcode;
        w = 1.0f;
      }
    }

    @Override
    public int compareTo(WeightedCode o) {
      float delta = o.w - w;
      if (Math.abs(delta) < 0.001) {
        return 0;
      } else {
        return delta > 0 ? 1 : -1;
      }
    }
  }
}
