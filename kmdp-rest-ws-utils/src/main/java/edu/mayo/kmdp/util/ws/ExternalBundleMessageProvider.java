package edu.mayo.kmdp.util.ws;

import java.util.Locale;
import org.springframework.context.support.ResourceBundleMessageSource;

public class ExternalBundleMessageProvider {

  ResourceBundleMessageSource msgSource;

  public ExternalBundleMessageProvider(String srcBundle) {
    this.msgSource = loadMessageSourceBundle(srcBundle);
  }

  public String getMessage(
      String msgCode, Object[] args, String defaultMessage, Locale aDefault) {
    return msgSource != null
        ? msgSource.getMessage(msgCode, args, defaultMessage, aDefault)
        : null;
  }

  private ResourceBundleMessageSource loadMessageSourceBundle(String srcBundle) {
    var source = new ResourceBundleMessageSource();
    source.setBasenames(srcBundle);
    source.setUseCodeAsDefaultMessage(true);
    return source;
  }

}
