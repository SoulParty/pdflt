/**
 * 
 */
package lt.nortal.pdflt.domain;

import java.security.InvalidParameterException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;

public abstract class SignaturePosition {
  
  public static final float DEFAULT_HEIGHT = 100;
  public static final float DEFAULT_WIDTH = 150;
  
  private static final Pattern HIDDEN_PATTERN = Pattern.compile("\\s*hidden\\s*");
  private static final Pattern RELATIVE_POSITION_PATTERN = Pattern.compile("\\s*relative\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(\\d+(?:\\.\\d+)?\\s*(?:mm|cm)?)\\s*,\\s*(\\d+(?:\\.\\d+)?\\s*(?:mm|cm)?)\\s*");
  private static final Pattern ABSOLUTE_POSITION_PATTERN = Pattern.compile("\\s*absolute\\s*,\\s*(-?\\d+)\\s*,\\s*(-?\\d+(?:\\.\\d+)?\\s*(?:mm|cm)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?\\s*(?:mm|cm)?)\\s*,\\s*(\\d+(?:\\.\\d+)?\\s*(?:mm|cm)?)\\s*,\\s*(\\d+(?:\\.\\d+)?\\s*(?:mm|cm)?)\\s*");
  private static final Pattern NAMED_POSITION_PATTERN = Pattern.compile("\\s*named\\s*,\\s*([\\S&&[^,]]+)\\s*");
  private static final Pattern NAMED_DESTINATION_POSITION_PATTERN = Pattern.compile("\\s*namedDestination\\s*,\\s*([\\S&&[^,]]+)\\s*,\\s*(-?\\d+(?:\\.\\d+)?\\s*(?:mm|cm)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?\\s*(?:mm|cm)?)\\s*,\\s*(\\d+(?:\\.\\d+)?\\s*(?:mm|cm)?)\\s*,\\s*(\\d+(?:\\.\\d+)?\\s*(?:mm|cm)?)\\s*");
  private static final Pattern DISTANCE_PATTERN = Pattern.compile("\\s*(-?\\d+(?:\\.\\d+)?)\\s*(mm|cm)?\\s*");
  private static final String MM = "mm";
  private static final String CM = "cm";
  public static final float POINTS_PER_MM = 72/25.4f;
  public static final float POINTS_PER_CM = 10*POINTS_PER_MM;
    
  /**
   * Parses position from string definition.
   * Possible definitions are:
   * <ul>
   *  <li>"hidden" - signature is not visualized;</li>
   *  <li>"relative, page, relativeX, reativeY, width, height" - signature position is relative;</li>
   *  <li>"absolute, page, x, y, width, height" - signature position is absolute;</li>
   *  <li>"named, name" - signature position is positioned to existing empty signature field;</li>
   *  <li>"namedDestination, name, x, y, width, height" - signature position is relative to named destination.</li>
   * </ul>
   * Here "page" is integer parameter; 
   *      "relativeX" and "relativeY" are floating point parameters in range [0,1];
   *      "x", "y", "width" and "height" are distance parameters (floating point numbers optionally followed by "mm" or "cm");
   *      "name" is name of signature field or named destination.
   * @param definition
   * @return
   * @throws InvalidParameterException
   */
  public static SignaturePosition parsePosition(String definition) {  
    if (definition == null) {
      return null;
    }
    
    SignaturePosition position  = parseHiddenPosition(definition);
    if (position != null) {
      return position;
    }
    
    position = parseRelativePosition(definition);
    if (position != null) {
      return position;
    }
    
    position = parseAbsolutePosition(definition);
    if (position != null) {
      return position;
    }
    
    position = parseNamedPosition(definition);
    if (position != null) {
      return position;
    }
    
    position = parseNamedDestinationPosition(definition);
    if (position != null) {
      return position;
    }

    throw new InvalidParameterException("Invalid signature position definition: \"" + definition +"\"");
  }

  public static boolean isValidPosition(String definition) {
    return
        definition == null ||
        parseHiddenPosition(definition) != null ||
        parseRelativePosition(definition) != null ||
        parseAbsolutePosition(definition) != null ||
        parseNamedPosition(definition) != null ||
        parseNamedDestinationPosition(definition) != null;
  }
  
  private static HiddenSignaturePosition parseHiddenPosition(String definition) {
    if (HIDDEN_PATTERN.matcher(definition).matches()) {
      return new HiddenSignaturePosition();
    } else {
      return null;
    }
  }

  private static RelativeSignaturePosition parseRelativePosition(String definition) {
    Matcher matcher = RELATIVE_POSITION_PATTERN.matcher(definition);
    if (matcher.matches()) {
      int page = Integer.parseInt(matcher.group(1));
      float relativeX = Float.parseFloat(matcher.group(2));
      float relativeY = Float.parseFloat(matcher.group(3));
      float width = parseDistance(matcher.group(4));
      float height = parseDistance(matcher.group(5));
      return new RelativeSignaturePosition(page, relativeX, relativeY, width, height);
    } else {
      return null;
    }
  }

  private static AbsoluteSignaturePosition parseAbsolutePosition(String definition) {
    Matcher matcher = ABSOLUTE_POSITION_PATTERN.matcher(definition);
    if (matcher.matches()) {
      int page = Integer.parseInt(matcher.group(1));
      float x = parseDistance(matcher.group(2));
      float y = parseDistance(matcher.group(3));
      float width = parseDistance(matcher.group(4));
      float height = parseDistance(matcher.group(5));
      return new AbsoluteSignaturePosition(page, x, y, width, height);
    } else {
      return null;
    }
  }

  private static NamedSignaturePosition parseNamedPosition(String definition) {
    Matcher matcher = NAMED_POSITION_PATTERN.matcher(definition);
    if (matcher.matches()) {
      String fieldName = matcher.group(1);
      return new NamedSignaturePosition(fieldName);
    } else {
      return null;
    }
  }

  private static NamedDestinationSignaturePosition parseNamedDestinationPosition(String definition) {
    Matcher matcher = NAMED_DESTINATION_POSITION_PATTERN.matcher(definition);
    if (matcher.matches()) {
      String destinationName = matcher.group(1);
      float offsetX = parseDistance(matcher.group(2));
      float offsetY = parseDistance(matcher.group(3));
      float width = parseDistance(matcher.group(4));
      float height = parseDistance(matcher.group(5));
      return new NamedDestinationSignaturePosition(destinationName, offsetX, offsetY, width, height);
    } else {
      return null;
    }
  }

  /**
   * Parses distance from string definition.
   * Distance consists of floating point number optionally followed by dimension ("mm" or "cm").
   * If dimension is not specified, default dimension of "points" is used.
   * @param definition
   * @return
   */
  public static float parseDistance(String definition) {
    Matcher matcher = DISTANCE_PATTERN.matcher(definition);
    if (matcher.matches()) {
      float value = Float.parseFloat(matcher.group(1));
      if (MM.equals(matcher.group(2))) {
        value *= POINTS_PER_MM;
      } else if (CM.equals(matcher.group(2))) {
        value *= POINTS_PER_CM;
      }
      return value;
    } else {
      throw new InvalidParameterException("Invalid dimension definition: \"" + definition +"\"");
    }
  }
  
  /**
   * Sets-up visible signature position for PdfSignatureAppearance object.
   * @param reader
   * @param sap
   * @return true, if signature is visible, false otherwise. 
   */
  public abstract boolean setupVisibleSignature(PdfReader reader, PdfSignatureAppearance sap);

}