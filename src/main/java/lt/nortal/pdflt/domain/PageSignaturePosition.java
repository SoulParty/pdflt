package lt.nortal.pdflt.domain;

import com.itextpdf.text.pdf.PdfReader;

public abstract class PageSignaturePosition extends SignaturePosition {

  private int page;
  
  public PageSignaturePosition(int page) {
    this.page = page;
  }

  /**
   * Returns page number where signature should be placed.
   * Pages are numbered starting from one. Zero page number means signature is not visualized.
   * @param reader a PDF reader object
   * @return page number
   */
  protected int getSignaturePageNumber(PdfReader reader) {
    int numberOfPages = reader.getNumberOfPages();
    int result;
    if (page >= 0) {
      // Page value is positive - start counting from start of document
      result = Math.min(page, numberOfPages);
    } else {
      // Page number is negative - start counting backwards from end of document
      result = Math.max(numberOfPages + page + 1, 1);
    }
    return result;
  }

  public int getPage() {
    return page;
  }

}
