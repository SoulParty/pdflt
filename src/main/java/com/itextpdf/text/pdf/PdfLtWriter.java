package com.itextpdf.text.pdf;

import java.io.OutputStream;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;

/**
 * Adopted for PDF-LT standard. {@link PdfWriter} which is able to generate correct document header as per Adobe PDF
 * v.1.7 extension 3 specification.
 * @author Erik Sabiun
 */
public class PdfLtWriter extends PdfWriter {

  protected PdfLtWriter(PdfDocument document, OutputStream os) {
    super(document, os);
    pdf_version = new PdfLtVersionImpl();
  }

  public static PdfWriter getPdfLtInstance(final Document document, final OutputStream os) throws DocumentException {
    PdfDocument pdf = new PdfDocument();
    document.addDocListener(pdf);
    PdfWriter writer = new PdfLtWriter(pdf, os);
    pdf.addWriter(writer);
    return writer;
  }

}
