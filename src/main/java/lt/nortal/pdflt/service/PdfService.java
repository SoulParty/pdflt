package lt.nortal.pdflt.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;

import lt.nortal.pdflt.domain.PresignData;
import lt.nortal.pdflt.domain.SignatureProperties;

import com.itextpdf.text.pdf.PdfReader;

public interface PdfService {

  PresignData prepareToSign(PdfReader reader, OutputStream pdfPresignOutputStream, SignatureProperties signatureProperties,
          X509Certificate certificate) throws Exception;

    PresignData prepareToLtv(byte[] bytesToSign, OutputStream pdfPresignOutputStream, SignatureProperties signatureProperties,
          X509Certificate certificate) throws Exception;

  void sign(InputStream pdfPresignInputStream, OutputStream pdOutputStream, PresignData presignData, byte[] signatureBytes) throws Exception;

  void timestamp(InputStream pdfInputStream, OutputStream pdOutputStream) throws Exception;

}
