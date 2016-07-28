package lt.nortal.pdflt.utils;

import java.io.InputStream;

import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.pdf.PdfReader;

public class PdfUtils {
  private static final Logger logger = LoggerFactory.getLogger(PdfUtils.class);

  private PdfUtils() {
  }

  /**
   * Attempt to parse document with iText and report failure if any.
   * @param docBytes document as byte array.
   * @return Created {@link PdfReader} if document can be parsed.
   */
  public static PdfReader gerReaderSafe(byte[] docBytes) {
    PdfReader reader = null;
    try {
      reader = new PdfReader(docBytes);
    } catch (Exception e) {
      logger.warn("Failed to parse input document");
    }

    return reader;
  }

  /**
   * Attempt to parse document with iText and report failure if any.
   * @param docBytes document as input stream.
   * @return Created {@link PdfReader} if document can be parsed.
   */
  public static PdfReader gerReaderSafe(InputStream docBytes) {
    PdfReader reader = null;
    try {
      reader = new PdfReader(docBytes);
      // reader closes input stream internally
    } catch (Exception e) {
      logger.warn("Failed to parse input document");
    }

    return reader;
  }
}
