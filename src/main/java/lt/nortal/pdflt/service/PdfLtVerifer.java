package lt.nortal.pdflt.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.cert.X509Certificate;
import java.util.Set;

import lt.nortal.everifier.ws.types.everifier.VerificationDetailStatus;
import lt.nortal.pdflt.domain.PresignData;
import lt.nortal.pdflt.domain.SignatureProperties;

import com.itextpdf.text.pdf.PdfReader;

public interface PdfLtVerifer {
    Set<VerificationDetailStatus> verify(byte[] pdf, final String storagePath, final String clientId) throws IOException;
}
