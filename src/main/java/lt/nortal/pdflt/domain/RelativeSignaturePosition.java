package lt.nortal.pdflt.domain;

import lt.nortal.pdflt.utils.PdfSizeUtils;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;

public class RelativeSignaturePosition extends PageSignaturePosition {

  private float relativeX;
  private float relativeY;
  private float width;
  private float height;
  
  /**
   * Creates relative signature position with non-standard width and height.
   * @param page a page number. Value greater then zero means page number. Negative value means page number from the end of document counting backwards. Zero means signature is not visualized.
   * @param relativeX a relative horizontal position. Valid range is from 0 to 1, 0 meaning left side of the page and 1 - right side of the page. 
   * @param relativeY a relative vertical position. Valid range is from 0 to 1, 0 meaning top of the page and 1 - bottom of the page.
   * @param width signature width
   * @param height signature height
   * @return
   */
  public RelativeSignaturePosition(int page, float relativeX, float relativeY, float width, float height) {
    super(page);
    this.relativeX = relativeX;
    this.relativeY = relativeY;
    this.width = width;
    this.height = height;
  }

  /**
   * Creates relative signature position with standard width and height.
   * @param page a page number. Value greater then zero means page number. Negative value means page number from the end of document counting backwards. Zero means signature is not visualized.
   * @param relativeX a relative horizontal position. Valid range is from 0 to 1, 0 meaning left side of the page and 1 - right side of the page. 
   * @param relativeY a relative vertical position. Valid range is from 0 to 1, 0 meaning top of the page and 1 - bottom of the page.
   * @return
   */
  public RelativeSignaturePosition(int page, float relativeX, float relativeY) {
    this(page, relativeX, relativeY, DEFAULT_WIDTH, DEFAULT_HEIGHT);
  }

  @Override
  public boolean setupVisibleSignature(PdfReader reader, PdfSignatureAppearance signatureAppearance) {
    int signaturePageNumber = getSignaturePageNumber(reader);
    if (signaturePageNumber == 0) {
      return false;
    }
    
    Rectangle pageRect = PdfSizeUtils.getCropBoxWithRotation(reader, signaturePageNumber);
    
    float actualWidth = Math.min(width, pageRect.getWidth());
    float actualHeight = Math.min(height, pageRect.getHeight());

    float horizontalPosition = pageRect.getLeft() + relativeX * (pageRect.getWidth() - actualWidth);
    float verticalPosition = pageRect.getBottom() + (1-relativeY) * (pageRect.getHeight() - actualHeight);

    Rectangle signatureRectangle = new Rectangle(horizontalPosition, verticalPosition, horizontalPosition + actualWidth, verticalPosition + actualHeight);   
    
    signatureAppearance.setVisibleSignature(signatureRectangle, signaturePageNumber, null);
    return true;
  }

  public float getRelativeX() {
    return relativeX;
  }

  public float getRelativeY() {
    return relativeY;
  }

  public float getWidth() {
    return width;
  }

  public float getHeight() {
    return height;
  }

}
