package edu.mayo.kmdp.docx;

import java.math.BigInteger;
import org.apache.poi.xwpf.usermodel.XWPFDocument;

public class DocumentBuilder {

  XWPFDocument document;

  public DocumentBuilder(String titleTxt) {
    this.document = new XWPFDocument();

    this.withParagraph()
        .withText(titleTxt,"bui");
  }

  public ParaBuilder withParagraph() {
    return new ParaBuilder(this, null);
  }

  public ParaBuilder withParagraph(int num) {
    return new ParaBuilder(this, BigInteger.valueOf(num));
  }


  public XWPFDocument get() {
    return document;
  }

}
