package lt.nortal.pdflt.xmp.struct;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "LTUdReg", propOrder = {"date", "number", "code"})
public class LTURegistration extends AbstractXmpType implements Serializable {
  private static final long serialVersionUID = -3861359226207214417L;

  protected Calendar date = GregorianCalendar.getInstance();
  protected String number = "IV-1458";
  protected String code = "1234568";

  public Calendar getDate() {
    return date;
  }

  public void setDate(Calendar date) {
    this.date = date;
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    this.number = number;
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

}
