package edu.mayo.kmdp.idl;

import edu.mayo.kmdp.util.NameUtils;

public class IDLNameUtil {

  protected IDLNameUtil() {

  }

  public static String toIdentifier(String s) {
    return NameUtils.camelCase(s);
  }
}
