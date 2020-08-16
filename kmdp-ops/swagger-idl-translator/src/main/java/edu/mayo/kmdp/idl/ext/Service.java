package edu.mayo.kmdp.idl.ext;

import edu.mayo.kmdp.idl.Interface;
import edu.mayo.kmdp.idl.Operation;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Service {

  private String name;
  private String documentation;
  private Map<Interface, List<Operation>> interfaces;

  public Service(String name, String documentation, Map<Interface, List<Operation>> interfaces) {
    this.name = name;
    this.documentation = documentation;
    this.interfaces = interfaces;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Collection<Interface> getInterfaces() {
    return interfaces.keySet();
  }

  public List<Operation> getOperations(Interface intf) {
    return interfaces.get(intf);
  }

  public String getDocumentation() {
    return documentation;
  }

  public void setDocumentation(String documentation) {
    this.documentation = documentation;
  }
}
