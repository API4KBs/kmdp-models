package edu.mayo.kmdp.docx;

import edu.mayo.kmdp.util.Util;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

public class ParaBuilder {

  private DocumentBuilder doc;
  XWPFParagraph p;

  public static final int DEFAULT_SIZE = 10;
  public static final String DEFAULT_COLOR = "000000";
  public static final String DEFAULT_FONT = "Times New Roman";

  protected ParaBuilder(DocumentBuilder document, BigInteger number) {
    this.doc = document;
    this.p = doc.document.createParagraph();
    this.p.setAlignment(ParagraphAlignment.LEFT);
    if (number != null) {
      this.p.setNumID(number);
    }
  }

  public ParaBuilder withTextLn(String text, String formatCode, int size) {
    return withText(text, formatCode, size).newLine();
  }

  public ParaBuilder withText(String text, String formatCode, int size) {
    if (text == null) {
      return this;
    }
    List<String> runs = Arrays.stream(text.split("\n"))
        .filter(s -> ! Util.isEmpty(s))
        .collect(Collectors.toList());

    for (int j = 0; j < runs.size(); j++) {
      this.withLine(runs.get(j), formatCode, size);
      if (j != runs.size() - 1) {
        this.newLine();
      }
    }

    return this;
  }

  private ParaBuilder withLine(String text, String formatCode, int size) {
    XWPFRun run = newRun(formatCode, size);
    run.setText(text);
    return this;
  }


  public ParaBuilder withTextLn(String text, int size) {
    return withText(text, size).newLine();
  }

  public ParaBuilder withText(String text, int size) {
    return withText(text, "", size);
  }


  public ParaBuilder withTextLn(String text, String formatCode) {
    return withText(text, formatCode).newLine();
  }


  public ParaBuilder withText(String text, String formatCode) {
    return withText(text, formatCode, DEFAULT_SIZE);
  }


  public ParaBuilder withTextLn(String text) {
    return withText(text).newLine();
  }

  public ParaBuilder withText(String text) {
    return withText(text, "", DEFAULT_SIZE);
  }

  private XWPFRun newRun(String formatCode, int size) {
    XWPFRun run = p.createRun();
    run.setColor(DEFAULT_COLOR);
    run.setFontFamily(DEFAULT_FONT);
    run.setBold(formatCode.contains("b"));
    run.setItalic(formatCode.contains("i"));
    run.setUnderline(formatCode.contains("u") ? UnderlinePatterns.SINGLE : UnderlinePatterns.NONE);
    run.setFontSize(size);
    return run;
  }

  public ParaBuilder newLine() {
    XWPFRun run = newRun("",DEFAULT_SIZE);
    run.addCarriageReturn();
    return this;
  }

  public ParaBuilder withTab() {
    XWPFRun run = newRun("",DEFAULT_SIZE);
    run.addTab();
    return this;
  }
}
