package lt.nortal.pdflt.domain;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignatureAppearance;

public class NamedSignaturePosition extends SignaturePosition {

  private String fieldName;
  
  /**
   * Creates named signature position.
   * @param fieldName name of existing empty signature field
   * @return
   */
  public NamedSignaturePosition(String fieldName) {
    this.fieldName = fieldName;
  }

  @Override
  public boolean setupVisibleSignature(PdfReader reader, PdfSignatureAppearance sap) {
    sap.setVisibleSignature(fieldName);
    return true;
  }

  public String getFieldName() {
    return fieldName;
  }

}
