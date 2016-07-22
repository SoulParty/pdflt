package lt.nortal.pdflt.xmp;

import lt.nortal.pdflt.xmp.struct.AbstractXmpType;
import lt.nortal.pdflt.xmp.struct.LTUEntity;
import lt.nortal.pdflt.xmp.struct.LTURegistration;

import com.itextpdf.text.xml.xmp.XmpArray;
import com.itextpdf.text.xml.xmp.XmpSchema;

/**
 * An implementation of an XmpSchema for PDF-LT standard.
 */
public class LTUdSchema extends XmpSchema {

  private static final long serialVersionUID = 4687185723602197638L;
  /** default namespace identifier */
  public static final String CORE_XPATH_ID = "LTUd";
  /** default namespace uri */
  public static final String CORE_XPATH_URI = "http://archyvai.lt/pdf-ltud/2013/metadata/";
  private static final String DEFAULT_STANDARD_VERSION = "PDF-LT-V1.0";

  public static final String STANDARD_VERSION = CORE_XPATH_ID + ":standardVersion";
  public static final String AUTHORS = CORE_XPATH_ID + ":authors";
  public static final String RECIPIENTS = CORE_XPATH_ID + ":recipients";
  public static final String INSTANCE_IDENTIFIER = CORE_XPATH_ID + ":documentCopyIdentifier";
  public static final String ORIGINAL_REGISTRATIONS = CORE_XPATH_ID + ":originalRegistrations";
  public static final String ORIGINAL_RECEPTIONS = CORE_XPATH_ID + ":originalReceptions";
  public static final String ORIGINAL_RECEIVERS = CORE_XPATH_ID + ":originalReceivers";
  public static final String REGISTRATION = CORE_XPATH_ID + ":registration";
  public static final String RECEPTION = CORE_XPATH_ID + ":reception";
  public static final String RECEIVER = CORE_XPATH_ID + ":receiver";

  public static final String ENT_XPATH_ID = "LTUdEnt";
  public static final String ENT_XPATH_URI = "http://archyvai.lt/pdf-ltud/2013/metadata/Entity/";

  public static final String REG_XPATH_ID = "LTUdReg";
  public static final String REG_XPATH_URI = "http://archyvai.lt/pdf-ltud/2013/metadata/Registration/";

  public LTUdSchema() {
    super("xmlns:" + CORE_XPATH_ID + "=\"" + CORE_XPATH_URI + "\"" + " " + "xmlns:" + ENT_XPATH_ID + "=\"" + ENT_XPATH_URI + "\"" + " " + "xmlns:"
        + REG_XPATH_ID + "=\"" + REG_XPATH_URI + "\"");
  }

  public void addStandardVersion() {
    setProperty(STANDARD_VERSION, DEFAULT_STANDARD_VERSION);
  }

  public void addAuthors(LTUEntity... authors) {
    XmpComplexTypeArray<LTUEntity> array = new XmpComplexTypeArray<LTUEntity>(XmpArray.ORDERED);
    for (LTUEntity author : authors) {
      array.add(author);
    }
    setProperty(AUTHORS, array);
  }

  public void addRecipients(LTUEntity... recipients) {
    XmpComplexTypeArray<LTUEntity> array = new XmpComplexTypeArray<LTUEntity>(XmpArray.ORDERED);
    for (LTUEntity recipient : recipients) {
      array.add(recipient);
    }
    setProperty(RECIPIENTS, array);
  }

  public void addInstanceIdentifier(String instanceIdentifier) {
    setProperty(INSTANCE_IDENTIFIER, instanceIdentifier);
  }

  public void addOriginalRegistrations(LTURegistration... originalRegistrations) {
    XmpComplexTypeArray<LTURegistration> array = new XmpComplexTypeArray<LTURegistration>(XmpArray.ORDERED);
    for (LTURegistration originalRegistration : originalRegistrations) {
      array.add(originalRegistration);
    }
    setProperty(ORIGINAL_REGISTRATIONS, array);
  }

  public void addOriginalReceptions(LTURegistration... originalReceptions) {
    XmpComplexTypeArray<LTURegistration> array = new XmpComplexTypeArray<LTURegistration>(XmpArray.ORDERED);
    for (LTURegistration originalReception : originalReceptions) {
      array.add(originalReception);
    }
    setProperty(ORIGINAL_RECEPTIONS, array);
  }

  public void addOriginalReceivers(LTUEntity... originalReceivers) {
    XmpComplexTypeArray<LTUEntity> array = new XmpComplexTypeArray<LTUEntity>(XmpArray.ORDERED);
    for (LTUEntity originalReceiver : originalReceivers) {
      array.add(originalReceiver);
    }
    setProperty(ORIGINAL_RECEIVERS, array);
  }

  public void addRegistration(LTURegistration registration) {
    setProperty(REGISTRATION, registration, REG_XPATH_ID);
  }

  public void addReception(LTURegistration reception) {
    setProperty(RECEPTION, reception, REG_XPATH_ID);
  }

  public void addReceiver(LTUEntity receiver) {
    setProperty(RECEIVER, receiver, ENT_XPATH_ID);
  }

  @Override
  protected void process(StringBuffer buf, Object p) {
    Object object = this.get(p);
    if (object instanceof AbstractXmpType) {
      buf.append('<');
      buf.append(p);
      buf.append(" rdf:parseType=\"Resource\">");
      XmpComplexTypeArray.printEntity(buf, (AbstractXmpType) object);
      buf.append("</");
      buf.append(p);
      buf.append('>');
    } else {
      super.process(buf, p);
    }
  }

  public synchronized Object setProperty(String key, XmpComplexTypeArray<?> value) {
    return put(key, value);
  }

  public synchronized Object setProperty(String key, AbstractXmpType value, String namespacePfx) {
    if (value != null) {
      StringBuffer entytyBytes = new StringBuffer();
      XmpComplexTypeArray.printEntity(entytyBytes, value);
      return put(key, value);
    } else {
      return null;
    }
  }
}
