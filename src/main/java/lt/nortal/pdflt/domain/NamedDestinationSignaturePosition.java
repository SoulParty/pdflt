package lt.nortal.pdflt.domain;

import lt.nortal.pdflt.utils.PdfSizeUtils;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.pdf.PdfArray;
import com.itextpdf.text.pdf.PdfIndirectReference;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;

public class NamedDestinationSignaturePosition extends SignaturePosition {

  private static Logger logger = LoggerFactory.getLogger(NamedDestinationSignaturePosition.class);

  private String destinationName;
  private float offsetX;
  private float offsetY;
  private float width;
  private float height;
  
  /**
   * Creates signature position relative to named destination in PDF document.
   * @param destinationName
   * @param offsetX
   * @param offsetY
   * @param width
   * @param height
   */
  public NamedDestinationSignaturePosition(String destinationName, float offsetX, float offsetY, float width, float height) {
    this.destinationName = destinationName;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
    this.width = width;
    this.height = height;
  }

  /**
   * Creates signature position with default size relative to named destination in PDF document.
   * @param destinationName
   * @param offsetX
   * @param offsetY
   */
  public NamedDestinationSignaturePosition(String destinationName, float offsetX, float offsetY) {
    this(destinationName, offsetX, offsetY, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  @Override
  public boolean setupVisibleSignature(PdfReader reader, PdfSignatureAppearance sap) {
    PdfObject destination = reader.getNamedDestination().get(destinationName);
    if (destination == null) {
      // Named destination not found
      logger.warn("PDF document does not contain named destination \"" + destinationName + "\"");
      return false;
    }

    PdfArray destinationArray = (PdfArray) destination;

    int pageNumber = getDestinationPageNumber(reader, destinationArray);
    Rectangle signatureRect = getSignatureRectangle(reader, destinationArray, pageNumber);
    sap.setVisibleSignature(signatureRect, pageNumber, null);

    return true;
  }

  private int getDestinationPageNumber(PdfReader reader, PdfArray destinationArray) {
    PdfIndirectReference pageReference = destinationArray.getAsIndirectObject(0);
    if (pageReference == null) {
      throw new IllegalStateException("Invalid structure of PDF explicit destination array - page reference not found at array index 0.");
    }
    
    int pageNumber = 0;
    for (int i=reader.getNumberOfPages(); i>0; i--) {
      PdfIndirectReference reference =reader.getPageOrigRef(i);
      if (reference.getNumber() == pageReference.getNumber() && reference.getGeneration() == pageReference.getGeneration()) {
        pageNumber = i;
        break;
      }
    }
    if (pageNumber == 0) {
      throw new IllegalStateException("Invalid PDF - page reference in explicit destination array does not reference actual page.");
    }
    return pageNumber;
  }


  private Rectangle getSignatureRectangle(PdfReader reader, PdfArray destinationArray, int pageNumber) {
    PdfName typeName = destinationArray.getAsName(1);
    if (typeName == null) {
      throw new IllegalStateException("Invalid structure of PDF explicit destination array - destination type name not found.");
    }

    
    Point point;   
    if (typeName.equals(PdfName.XYZ)) {
      point = new Point(destinationArray.getAsNumber(2).floatValue(), destinationArray.getAsNumber(3).floatValue());
      point = PdfSizeUtils.getPointWithRotation(point, reader, pageNumber);
    } else if (typeName.equals(PdfName.FIT) || typeName.equals(PdfName.FITB)) {
      Rectangle cropBox = PdfSizeUtils.getCropBoxWithRotation(reader, pageNumber);
      point = new Point(cropBox.getLeft(), cropBox.getTop());
    } else if (typeName.equals(PdfName.FITH) || typeName.equals(PdfName.FITBH)) {
      point = new Point(reader.getCropBox(pageNumber).getLeft(), destinationArray.getAsNumber(2).floatValue());
      point = PdfSizeUtils.getPointWithRotation(point, reader, pageNumber);
    } else if (typeName.equals(PdfName.FITV) || typeName.equals(PdfName.FITBV)) {
      point = new Point(destinationArray.getAsNumber(2).floatValue(), reader.getCropBox(pageNumber).getTop());
      point = PdfSizeUtils.getPointWithRotation(point, reader, pageNumber);
    } else if (typeName.equals(PdfName.FITR)) {
      Rectangle rect = new Rectangle(
          destinationArray.getAsNumber(2).floatValue(), 
          destinationArray.getAsNumber(3).floatValue(),
          destinationArray.getAsNumber(5).floatValue(),
          destinationArray.getAsNumber(6).floatValue());
      rect = PdfSizeUtils.getRectangleWithRotation(rect, reader, pageNumber);
      point = new Point(rect.getLeft(), rect.getTop());
    } else {
      throw new IllegalStateException("Invalid structure of PDF explicit destination array - unexpected type name.");      
    }
    
    float x = point.getX() + offsetX;
    float y = point.getY() - offsetY;    
    return new Rectangle(x, y-height, x + width, y);
  }

  public String getDestinationName() {
    return destinationName;
  }

  public float getOffsetX() {
    return offsetX;
  }

  public float getOffsetY() {
    return offsetY;
  }

  public float getWidth() {
    return width;
  }

  public float getHeight() {
    return height;
  }

}
