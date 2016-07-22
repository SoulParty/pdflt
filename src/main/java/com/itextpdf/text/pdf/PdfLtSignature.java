package com.itextpdf.text.pdf;

public class PdfLtSignature extends PdfSignature {

  public PdfLtSignature(PdfName filter, PdfName subFilter) {
    super(filter, subFilter);
  }

  public void setRole(String role) {
    put(new PdfName("LTUd_Role"), new PdfString(role, PdfObject.TEXT_UNICODE));
  }

  public void setSignerNotes(String signerNotes) {
    put(new PdfName("LTUd_SignerNotes"), new PdfString(signerNotes, PdfObject.TEXT_UNICODE));
  }

}
