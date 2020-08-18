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
package org.omg.spec.api4kp._20200801.services.transrepresentation;

import static edu.mayo.kmdp.util.Util.isEmpty;
import static org.omg.spec.api4kp._20200801.AbstractCarrier.rep;
import static org.omg.spec.api4kp.taxonomy.krformat.SerializationFormatSeries.JSON;
import static org.omg.spec.api4kp.taxonomy.krformat.SerializationFormatSeries.TXT;
import static org.omg.spec.api4kp.taxonomy.krformat.SerializationFormatSeries.XML_1_1;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.HTML;
import static org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries.XHTML;

import edu.mayo.kmdp.util.FileUtil;
import edu.mayo.kmdp.util.Util;
import edu.mayo.ontology.taxonomies.mimetype.IMIMEType;
import edu.mayo.ontology.taxonomies.mimetype.MIMETypeSeries;
import java.util.Arrays;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.omg.spec.api4kp._20200801.services.SyntacticRepresentation;
import org.omg.spec.api4kp.taxonomy.krformat.SerializationFormat;
import org.omg.spec.api4kp.taxonomy.krformat.SerializationFormatSeries;
import org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguage;
import org.omg.spec.api4kp.taxonomy.krlanguage.KnowledgeRepresentationLanguageSeries;
import org.omg.spec.api4kp.taxonomy.krprofile.KnowledgeRepresentationLanguageProfileSeries;
import org.omg.spec.api4kp.taxonomy.krserialization.KnowledgeRepresentationLanguageSerializationSeries;
import org.omg.spec.api4kp.taxonomy.lexicon.LexiconSeries;

public class ModelMIMECoder {

  protected ModelMIMECoder() {
  }

  public static final String TYPE = "model";

  private static final String REGEXP = FileUtil
      .readStatic("/model.mime.regexp", ModelMIMECoder.class);

  private static final String WEIGHT_REGEXP = "(.+);q=([01].\\d+)(;.+)*";

  private static final Pattern RX_PATTERN = Pattern.compile(REGEXP);

  private static final Pattern WX_PATTERN = Pattern.compile(WEIGHT_REGEXP);

  public static String encode(SyntacticRepresentation rep) {
    return encode(rep, true);
  }

  public static String encode(SyntacticRepresentation rep, boolean withVersions) {
    StringBuilder sb = new StringBuilder(TYPE + "/");
    if (rep.getLanguage() != null) {
      String langTag = withVersions
          ? rep.getLanguage().getTag()
          : rep.getLanguage().getTag().substring(0, rep.getLanguage().getTag().indexOf('-'));
      sb.append(langTag);
    } else {
      sb.append("*");
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
      rep.getLexicon().forEach(l -> sb.append(l.getTag()).append(";"));
      sb.replace(sb.length() - 1, sb.length(), "}");
    }
    if (rep.getCharset() != null) {
      sb.append(";charset=").append(rep.getCharset());
    }
    if (rep.getEncoding() != null) {
      sb.append(";enc=").append(rep.getEncoding());
    }

    if (!RX_PATTERN.matcher(sb.toString()).matches()) {
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
        .map(ModelMIMECoder::decodeTags);
  }

  protected static SyntacticRepresentation decodeTags(LangTags t) {

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
      StringTokenizer tok = new StringTokenizer(t.serialTag, "+/");
      while (tok.hasMoreTokens()) {
        Optional<SerializationFormat> detectedFormat = SerializationFormatSeries
            .resolveTag(tok.nextToken());
        detectedFormat.ifPresent(rep::setFormat);
      }
      if (rep.getFormat() == null) {
        rep.setFormat(TXT);
      }
    }

    if (!isEmpty(t.lexTags)) {
      Arrays.stream(t.lexTags.split(";"))
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
  }

  private static String ensureFormalized(String mimeCode) {
    if (Util.isEmpty(mimeCode) || mimeCode.startsWith(TYPE)) {
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
      case Application_Xml:
        mappedMime = ModelMIMECoder.encode(rep(null,XML_1_1));
        break;
      case JSON_Data:
        mappedMime = ModelMIMECoder.encode(rep(null,JSON));
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
    Float w;
  }

  private static Optional<LangTags> decompose(String mime) {
    if (Util.isEmpty(mime)) {
      return Optional.empty();
    }
    Matcher matcher = RX_PATTERN.matcher(mime);
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

    tags.w = isEmpty(matcher.group(7))
        ? 1.0f
        : Float.parseFloat(matcher.group(7).replace(";q=",""));

    tags.charsetTag = isEmpty(matcher.group(8)) ? "" : matcher.group(8).trim()
        .replace(";charset=", "");

    tags.encodingTag = isEmpty(matcher.group(9)) ? "" : matcher.group(9).trim()
        .replace(";enc=", "");

    return Optional.of(tags);
  }


  public static WeightedRepresentation decodeWeighted(String code) {
    return decodeWeighted(code, null);
  }

  public static WeightedRepresentation decodeWeighted(String code, SyntacticRepresentation rep) {
    if (code.startsWith(TYPE)) {
      Optional<LangTags> tags = decompose(code);
      return tags
          .map(t -> new WeightedRepresentation(
              code,
              tags.map(ModelMIMECoder::decodeTags)
                  .orElse(null),
              t.w))
          .orElseThrow(() ->
              new IllegalArgumentException("Unable to decode formal MIME code " + code));
    } else {
      int idx = code.indexOf(';');
      String coreCode = code.substring(0, idx < 0 ? code.length() : idx).trim();
      return new WeightedRepresentation(
              code,
              MIMETypeSeries.resolveTag(coreCode)
                  .flatMap(ModelMIMECoder::mapKnownMimes)
                  .flatMap(mime -> ModelMIMECoder.decode( mime, rep != null ? rep.getLanguage() : null))
                  .orElse(null),
              detectWeight(code).orElse(1.0f)
          );
    }
  }

  private static Optional<Float> detectWeight(String code) {
    Matcher m = WX_PATTERN.matcher(code);
    if (m.matches()) {
      return Optional.of(Float.parseFloat(m.group(2)));
    }
    return Optional.empty();
  }

  public static class WeightedRepresentation implements Comparable<WeightedRepresentation> {

    String code;
    SyntacticRepresentation rep;
    float weight;

    public WeightedRepresentation(String code, SyntacticRepresentation rep, Float w) {
      this.code = code;
      this.rep = rep;
      this.weight = w;
    }

    @Override
    public int compareTo(WeightedRepresentation other) {
      return Float.compare(other.weight,this.weight);
    }

    public String getCode() {
      return code;
    }
    public Optional<SyntacticRepresentation> getRep() {
      return Optional.ofNullable(rep);
    }
    public float getWeight() {
      return weight;
    }
  }

}