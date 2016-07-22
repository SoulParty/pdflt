package lt.nortal.pdflt.xmp.struct;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "LTUdEnt", propOrder = {"individual", "name", "code", "address", "eMail"})
public class LTUEntity extends AbstractXmpType implements Serializable {
  private static final long serialVersionUID = -8265437360489016324L;

  protected Boolean individual;
  protected String name;
  protected String code;
  protected String address;
  protected String eMail;

  public Boolean getIndividual() {
    return individual;
  }

  public void setIndividual(Boolean individual) {
    this.individual = individual;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String geteMail() {
    return eMail;
  }

  public void seteMail(String eMail) {
    this.eMail = eMail;
  }

}
