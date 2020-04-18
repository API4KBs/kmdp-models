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
import static edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries.XML_1_1;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;
import static edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries.XHTML;
import static org.omg.spec.api4kp._1_0.AbstractCarrier.rep;

import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormat;
import edu.mayo.ontology.taxonomies.krformat.SerializationFormatSeries;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguage;
import edu.mayo.ontology.taxonomies.krlanguage.KnowledgeRepresentationLanguageSeries;
import edu.mayo.ontology.taxonomies.krprofile.KnowledgeRepresentationLanguageProfileSeries;
import edu.mayo.ontology.taxonomies.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import edu.mayo.ontology.taxonomies.lexicon.LexiconSeries;
import edu.mayo.ontology.taxonomies.mimetype.IMIMEType;
import edu.mayo.ontology.taxonomies.mimetype.MIMETypeSeries;
import java.util.Arrays;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.omg.spec.api4kp._1_0.services.SyntacticRepresentation;

public class ModelMIMECoder {

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
    if (rep.getCharset() != null) {
      sb.append(";charset=").append(rep.getCharset());
    }
    if (rep.getEncoding() != null) {
      sb.append(";enc=").append(rep.getEncoding());
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
    String formalMime = ensureFormalized(mime);
    return decompose(formalMime)
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

          // currently, serialization meta-format XOR serialization are supported:
          // prefer the former, try to use the latter if unsuccessful.
          if (!isEmpty(t.formatTag)) {
            rep.setFormat(SerializationFormatSeries.resolve(t.formatTag)
                .orElse(null));
          }

          if (!isEmpty(t.serialTag) && rep.getFormat() == null) {
            KnowledgeRepresentationLanguageSerializationSeries.resolve(t.serialTag)
                .ifPresent(rep::setSerialization);
          }

          if (rep.getFormat() == null && rep.getSerialization() != null) {
            StringTokenizer tok = new StringTokenizer(t.serialTag,"+/");
            while (tok.hasMoreTokens()) {
              Optional<SerializationFormat> detectedFormat = SerializationFormatSeries.resolveTag(tok.nextToken());
              detectedFormat.ifPresent(rep::setFormat);
            }
            if (rep.getFormat() == null) {
              rep.setFormat(TXT);
            }
          }


          if (!isEmpty(t.lexTags)) {
            Arrays.stream(t.lexTags.split(","))
                .forEach(l -> LexiconSeries.resolve(l)
                    .ifPresent(rep::withLexicon));
          }

          if (!isEmpty(t.charsetTag)) {
            rep.setCharset(t.charsetTag);
          }

          if (!isEmpty(t.encodingTag)) {
            rep.setEncoding(t.encodingTag);
          }

          return rep;
        });
  }

  private static String ensureFormalized(String mimeCode) {
    if (Util.isEmpty(mimeCode) || mimeCode.startsWith("model")) {
      return mimeCode;
    }
    return MIMETypeSeries.resolve(mimeCode)
        .flatMap(ModelMIMECoder::mapKnownMimes)
        .orElse(mimeCode);
  }

  /**
   * Existing MIME codes
   *  - do not make consistent distinctions between languages, formats, profiles, etc
   *  - even then, do not apply a consistent encoding strategy
   *
   *  This method normalizes some well known MIME codes, encoding the components
   *  according to the current 'grammar'
   * @param mimeType an encoded, standard MIME type
   * @return the formal re-encoding of the input MIME type
   */
  private static Optional<String> mapKnownMimes(IMIMEType mimeType) {
    String mappedMime = null;
    switch (mimeType.asEnum()) {
      case HyperText_Markup_Language:
        mappedMime = ModelMIMECoder.encode(rep(HTML,TXT));
        break;
      case Application_Xhtmlxml:
        mappedMime = ModelMIMECoder.encode(rep(XHTML,XML_1_1));
        break;
      default:
    }
    return Optional.ofNullable(mappedMime);
  }

  private static class LangTags {

    String langTag;
    String langVerTag;
    String versionedLangTag;
    String profTag;
    String serialTag;
    String formatTag;
    String lexTags;
    String charsetTag;
    String encodingTag;
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

    tags.lexTags = isEmpty(matcher.group(6)) ? "" : matcher.group(6).trim()
        .replace("}", "")
        .replace("{", "")
        .replace(";lex=", "");

    // Group 7 is the weight q

    tags.charsetTag = isEmpty(matcher.group(8)) ? "" : matcher.group(8).trim()
        .replace(";charset=", "");

    tags.encodingTag = isEmpty(matcher.group(9)) ? "" : matcher.group(9).trim()
        .replace(";enc=", "");

    return Optional.of(tags);
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
      if (!code.equals(o.code)) {
        return 0;
      }
      float delta = o.w - w;
      if (Math.abs(delta) < 0.001) {
        return 0;
      } else {
        return delta > 0 ? 1 : -1;
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof WeightedCode)) {
        return false;
      }

      WeightedCode that = (WeightedCode) o;

      if (Math.abs(w-that.w) > 0.001) {
        return false;
      }
      return code.equals(that.code);
    }

    @Override
    public int hashCode() {
      int result = code.hashCode();
      result = 31 * result + (w != +0.0f ? Float.floatToIntBits(w) : 0);
      return result;
    }
  }
}
