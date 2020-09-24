
package org.omg.spec.api4kp._20200801.datatypes;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import edu.mayo.kmdp.id.adapter.CopyableHashMap;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Bindings complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Bindings"&gt;
 *   &lt;complexContent&gt;
 *     &lt;extension base="{https://www.omg.org/spec/API4KP/20200801/datatypes}Map"&gt;
 *       &lt;group ref="{https://www.omg.org/spec/API4KP/20200801/datatypes}Bindings.content"/&gt;
 *       &lt;attGroup ref="{https://www.omg.org/spec/API4KP/20200801/datatypes}Bindings.attrs"/&gt;
 *     &lt;/extension&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlRootElement(name = "bindings")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Bindings")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, defaultImpl = Bindings.class, property = "_class")
public class Bindings<K,V>
    extends CopyableHashMap<K,V,Bindings<K,V>>
{

  private final static long serialVersionUID = 1L;

  public Object clone() {
    Bindings<K,V> b = new Bindings<>();
    b.putAll(this);
    return b;
  }

  public Object createNewInstance() {
    return new Bindings<K,V>();
  }

}
