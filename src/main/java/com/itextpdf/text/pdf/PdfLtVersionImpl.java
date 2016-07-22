package com.itextpdf.text.pdf;

import java.io.IOException;

import com.itextpdf.text.DocWriter;
import com.itextpdf.text.pdf.internal.PdfVersionImp;

/**
 * Implementation, which supports PDF-LT related extensions header.
 * @author Erik Sabiun
 */
public class PdfLtVersionImpl extends PdfVersionImp {

  private static final String LTUD_EXTENSION_HEADER = "\n<</Type /Catalog\n" + " /Extensions\n" + "  <</LTUd\n" + "    <</BaseVersion /1.7\n"
      + "      /ExtensionLevel 1\n" + "    >>\n" + "  >>\n" + ">>";
  public static final byte[] LTUD_EXTENSION_HEADER_B = DocWriter.getISOBytes(LTUD_EXTENSION_HEADER);

  @Override
  public void writeHeader(OutputStreamCounter os) throws IOException {
    if (appendmode) {
      os.write(HEADER[0]);
    } else {
      os.write(HEADER[1]); //﻿%âãÏÓshould be first
      os.write(getVersionAsByteArray(header_version));
      os.write(HEADER[2]);
      os.write(LTUD_EXTENSION_HEADER_B);
      headerWasWritten = true;
    }
  }
}
