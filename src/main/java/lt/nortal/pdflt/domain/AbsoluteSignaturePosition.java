package lt.nortal.pdflt.domain;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;

public class AbsoluteSignaturePosition extends PageSignaturePosition {

  private float x;
  private float y;
  private float width;
  private float height;
  
  /**
   * Creates absolute signature position with non-standard width and height.
   * @param page a page number. Value greater then zero means page number. Negative value means page number from the end of document counting backwards. Zero means signature is not visualized.
   * @param x a horizontal position. Positive values mean distance from left side, negative value - distance from right side. 
   * @param y a vertical position. Positive values mean distance from top, negative value - distance from bottom.
   * @param width signature width
   * @param height signature height
   * @return
   */
  public AbsoluteSignaturePosition(int page, float x, float y, float width, float height) {
    super(page);
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  /**
   * Creates absolute signature position with standard width and height.
   * @param page a page number. Value greater then zero means page number. Negative value means page number from the end of document counting backwards. Zero means signature is not visualized.
   * @param x a horizontal position. Positive values mean distance from left side, negative value - distance from right side. 
   * @param y a vertical position. Positive values mean distance from top, negative value - distance from bottom.
   * @return
   */
  public AbsoluteSignaturePosition(int page, float x, float y) {
    this(page, x, y, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  @Override
  public boolean setupVisibleSignature(PdfReader reader, PdfSignatureAppearance signatureAppearance) {
    int signaturePageNumber = getSignaturePageNumber(reader);
    if (signaturePageNumber == 0) {
      return false;
    }
    
    Rectangle pageSize = reader.getPageSizeWithRotation(signaturePageNumber);
 
    float horizontalPosition = (x >= 0) ? x : pageSize.getWidth() + x;
    float verticalPosition = (y >= 0) ? pageSize.getHeight() - y : -y;
    Rectangle signatureRectangle = new Rectangle(horizontalPosition, verticalPosition - height, horizontalPosition + width, verticalPosition);
    
    signatureAppearance.setVisibleSignature(signatureRectangle, signaturePageNumber, null);
    return true;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public float getWidth() {
    return width;
  }

  public float getHeight() {
    return height;
  }

}
