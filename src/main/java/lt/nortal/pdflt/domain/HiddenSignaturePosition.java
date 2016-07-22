package lt.nortal.pdflt.domain;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;

public class HiddenSignaturePosition extends SignaturePosition {

  /**
   * Creates hidden signature position.
   * @return
   */
  public HiddenSignaturePosition() {
  }

  @Override
  public boolean setupVisibleSignature(PdfReader reader, PdfSignatureAppearance sap) {
    return false;
  }

}
