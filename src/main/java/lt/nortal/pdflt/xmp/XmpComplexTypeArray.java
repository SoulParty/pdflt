package lt.nortal.pdflt.xmp;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import com.itextpdf.text.xml.XMLUtil;
import lt.nortal.XmlType;
import lt.nortal.pdflt.utils.ReflectionUtils;
import lt.nortal.pdflt.xmp.struct.AbstractXmpType;

public class XmpComplexTypeArray<T extends AbstractXmpType> extends ArrayList<T> {
    private static final long serialVersionUID = - 7794262726996018618L;
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
    /**
     * the type of array.
     */
    protected final String type;

    public XmpComplexTypeArray(String type) {
        this.type = type;
    }

    /**
     * Returns the String representation of the XmpArray.
     *
     * @return a String representation
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("<");
        buf.append(type);
        buf.append('>');
        for (T ent : this) {
            // complex type specific
            buf.append("<rdf:li rdf:parseType=\"Resource\">");
            printEntity(buf, ent);
            buf.append("</rdf:li>");
        }
        buf.append("</");
        buf.append(type);
        buf.append('>');
        return buf.toString();
    }

    public static void printEntity(StringBuffer buf, AbstractXmpType ent) {
        XmlType tag;
        if ((tag = ent.getClass().getAnnotation(XmlType.class)) != null) {
            for (String prop : tag.propOrder()) {
                // extract value
                Object val;
                try {
                    Field f;
                    Method m;
                    if ((f = ReflectionUtils.findField(ent.getClass(), prop)) != null && f.isAccessible()) {
                        val = f.get(ent);
                    }
                    else {
                        String getterName = capitalizeFirstLetter(prop);
                        if ((m = ReflectionUtils.findMethod(ent.getClass(), "get" + getterName)) != null) {
                            val = m.invoke(ent, (Object[]) null);
                        }
                        else if ((m = ReflectionUtils.findMethod(ent.getClass(), "is" + getterName)) != null) {
                            val = m.invoke(ent, (Object[]) null);
                        }
                        else {
                            throw new NoSuchFieldException(prop + " can not be mapped");
                        }
                    }
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("Unable to get field value for " + ent.getClass() + ", no such field.",
                                               e);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(
                            "Unable to get field value for " + ent.getClass() + ", field not accessible.", e);
                } catch (SecurityException e) {
                    throw new RuntimeException("Unable to get field value for " + ent.getClass() + ", nu such field.",
                                               e);
                } catch (InvocationTargetException e) {
                    throw new RuntimeException("Unable to get field value for " + ent.getClass() + ", method fail.", e);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("Unable to get field value for " + ent.getClass() + ", no such value.",
                                               e);
                }

                if (val != null) {
                    // convert value
                    String valText;
                    if (val instanceof String) {
                        valText = (String) val;
                    }
                    else if (val instanceof Boolean) {
                        valText = capitalizeFirstLetter(val);
                    }
                    else if (val instanceof Calendar) {
                        try {
                            valText = DATE_FORMATTER.format(val);
                        } catch (Exception e) {
                            throw new RuntimeException("Date conversion error.", e);
                        }

                    }
                    else {
                        valText = val.toString();
                    }

                    buf.append("<");
                    buf.append(tag.namespace());
                    buf.append(":");
                    buf.append(prop);
                    buf.append('>');

                    buf.append(XMLUtil.escapeXML(valText, false));

                    buf.append("</");
                    buf.append(tag.namespace());
                    buf.append(":");
                    buf.append(prop);
                    buf.append('>');
                }

            }
        }

    }

    private static String capitalizeFirstLetter(Object val) {
        String valText = String.valueOf(val);
        char[] stringArray = valText.toCharArray();
        if (stringArray.length > 1 && Character.isLowerCase(stringArray[1])) {
            stringArray[0] = Character.toUpperCase(stringArray[0]);
            valText = new String(stringArray);
        }
        // else getters in this case are not capitalised by convention
        return valText;
    }
}
