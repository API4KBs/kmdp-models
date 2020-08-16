package edu.mayo.kmdp.idl;

import edu.mayo.kmdp.util.NameUtils;
import java.util.Arrays;
import java.util.List;

public class IDLNameUtil {

 static final List<String> idlKeywords = Arrays.asList(
      "abstract", "exception", "inout", "provides", "truncatable ",
      "any", "emits", "interface", "public", "typedef ",
      "attribute", "enum", "local", "publishes", "typeid",
      "boolean", "eventtype", "long", "raises", "typeprefix"
      , "case", "factory", "module", "readonly", "unsigned"
      , "char", "FALSE", "multiple", "setraises", "union"
      , "component", "finder", "native", "sequence", "uses"
      , "const", "fixed", "Object", "short", "ValueBase"
      , "consumes", "float", "octet", "string", "valuetype"
      , "context", "getraises", "oneway", "struct", "void"
      , "custom", "home", "out", "supports", "wchar"
      , "default", "import", "primarykey", "switch", "wstring"
      , "double", "in", "private"
  );

  protected IDLNameUtil() {

  }

  public static String toIdentifier(String s) {
    return applyFieldNameMappings(NameUtils.camelCase(s));
  }

  public static String toFQName(String packageName, String name) {
    return "::" + packageName.replace(".","::") + "::" + name;
  }

  public static String applyTypeNameMappings(String name) {
    // Temporary mapping - "2" types allow for co-existence
    // of legacy and future types of the same name
    if (name.endsWith("2")) {
      return name.substring(0,name.length()-1);
    }
    return name;
  }


  public static String applyFieldNameMappings(String name) {
    if (isIDLKeyWord(name)) {
      return "_" + name;
    }
    return name;
  }

  private static boolean isIDLKeyWord(String w) {
    return idlKeywords.contains(w);
  }
}
