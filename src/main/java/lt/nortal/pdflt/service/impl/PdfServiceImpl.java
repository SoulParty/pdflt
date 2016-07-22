package lt.nortal.pdflt.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Random;

import javax.annotation.PostConstruct;

import lt.nortal.pdflt.domain.SignatureMetaData;
import lt.nortal.pdflt.domain.SignaturePosition;
import lt.nortal.pdflt.domain.SignatureProperties;
import lt.nortal.pdflt.service.PdfService;
import lt.nortal.pdflt.domain.PresignData;
import lt.nortal.rc.unisign.util.cert.CertificateChainService;
import lt.nortal.rc.unisign.util.ocsp.OcspService;
import lt.nortal.rc.unisign.util.pdf.XmpReader;
import lt.webmedia.sigute.service.common.utils.CertificateUtils;
import lt.webmedia.sigute.service.common.utils.FileUtils;
import lt.nortal.pdflt.utils.MarkerOutputStream;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ByteBuffer;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfAConformanceLevel;
import com.itextpdf.text.pdf.PdfAStamper;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignature;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfString;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.LtvTimestamp;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.security.MakeSignature.CryptoStandard;
import com.itextpdf.text.pdf.security.PdfPKCS7;
import com.itextpdf.text.pdf.security.TSAClient;
import com.itextpdf.text.pdf.security.TSAClientBouncyCastle;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service("pdfService")
public class PdfServiceImpl implements PdfService {

  private static Logger logger = LoggerFactory.getLogger(PdfServiceImpl.class);

  private static final float MARGIN = 2;
//  private static final String DIGEST_ALGORITHM = "SHA1";
  private static final String DIGEST_ALGORITHM = "SHA256";
  private static final CryptoStandard CRYPTO_STANDARD = CryptoStandard.CADES;

  protected static final char DEF_PDF_VERSION = '\0';
  /**
   * Minimal signed document version for pdf-lt documents. Also used for rising not yet signed docs.
   */
  protected static final char MIN_PDF_LT_VERSION = '7';

  private static int estimatedSignatureSize = 8303 + 2048; // addition for second root certificate

  private MessageSource messageSource;
  private CertificateChainService certificateChainService;
  private String tsaUrl;
  private TSAClient tsaClient;
  private OcspService ocspService;
  private Random random = new Random();

  @Autowired
  public void setMessageSource(MessageSource messageSource) {
    this.messageSource = messageSource;
  }

  @Autowired
  public void setCertificateChainService(CertificateChainService certificateChainService) {
    this.certificateChainService = certificateChainService;
  }

  public String getTsaUrl() {
    return tsaUrl;
  }

  @Value("${tsa.url}")
  public void setTsaUrl(String tsaUrl) {
    this.tsaUrl = tsaUrl;
  }

  @Autowired
  public void setOcspService(OcspService ocspService) {
    this.ocspService = ocspService;
  }

  @Value("classpath:/FreeSans.ttf")
  public void setSignatureFont(Resource fontFile) throws DocumentException, IOException {

    try {
      File file = fontFile.getFile();
      FontFactory.register(file.getAbsolutePath(), "signature_font");
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
  }

  @PostConstruct
  public void initEnvironment() throws Exception {
    Security.addProvider(new BouncyCastleProvider());
  }

  /*
   * (non-Javadoc)
   * @see lt.nortal.rc.unisign.server.service.PdfService#prepareToSign(com.itextpdf.text.pdf.PdfReader,
   * java.io.OutputStream, lt.nortal.rc.unisign.server.domain.SignatureProperties, java.security.cert.X509Certificate)
   */
  @Override
  public PresignData prepareToSign(PdfReader reader, OutputStream pdfPresignOutputStream, SignatureProperties signatureProperties, X509Certificate certificate)
      throws Exception {

    PresignData data = new PresignData();
    data.setApplyTimestamp(signatureProperties.getSigMetaData().isApplyTimestamp());
    data.setSigningDate(GregorianCalendar.getInstance());
    data.setCertificateChain(certificateChainService.getCertificateChain(certificate));

    // OCSP check is only performed if timestamp is requested
    if (signatureProperties.getSigMetaData().isApplyOCSP()) {
      data.setOcspBytes(ocspService.getOcspBytes(data.getCertificateChain()));
    }

    byte[] dummySig = createUniqueDummySignature(data);
    byte[] dummySigFileRepresentation = getSignatureFileRepresentation(dummySig, data);

    MarkerOutputStream markerOutputStream = new MarkerOutputStream(pdfPresignOutputStream, dummySigFileRepresentation);
    PdfSignatureAppearance sap = buildSignatureAppearance(reader, markerOutputStream, data, signatureProperties);
    byte[] secondDigest = calculateDigest(sap.getRangeStream(), DIGEST_ALGORITHM);
    data.setSecondDigest(secondDigest);

    prepareSignature(data);

    PdfDictionary dic2 = new PdfDictionary();
    dic2.put(PdfName.CONTENTS, new PdfString(dummySig).setHexWriting(true));
    sap.close(dic2);

    if (markerOutputStream.getMarkerPositions().size() == 1) {
      data.setSignaturePositionInFile(markerOutputStream.getMarkerPositions().get(0));
    } else if (markerOutputStream.getMarkerPositions().size() < 1) {
      throw new RuntimeException("Signature marker not found in presigned PDF file.");
    } else {
      throw new RuntimeException("More than one signature marker not found in presigned PDF file.");
    }

    return data;
  }

  @Override
  public PresignData prepareToLtv(byte[] bytesToSign, OutputStream pdfPresignOutputStream, SignatureProperties signatureProperties, X509Certificate
          certificate)
      throws Exception {

    PresignData data = new PresignData();
    data.setSigningDate(GregorianCalendar.getInstance());
    data.setCertificateChain(certificateChainService.getCertificateChain(certificate));
    data.setConvertToLtv(true);
    data.setBytesToSign(bytesToSign);
    // OCSP check is only performed if timestamp is requested
    if (signatureProperties.getSigMetaData().isApplyOCSP()) {
      data.setOcspBytes(ocspService.getOcspBytes(data.getCertificateChain()));
    }
    return data;
  }

  public PdfPKCS7 prepareSignature(PresignData data) throws GeneralSecurityException {
    PdfPKCS7 sgn = new PdfPKCS7(null, data.getCertificateChain(), DIGEST_ALGORITHM, null, new BouncyCastleDigest(), false);
    byte bytesToSign[] = sgn.getAuthenticatedAttributeBytes(data.getSecondDigest(), data.getSigningDate(), data.getOcspBytes(), null, CRYPTO_STANDARD);
    // Check if bytes to sign match actually signed bytes
    if (data.getBytesToSign() != null && !Arrays.equals(bytesToSign, data.getBytesToSign())) {
      throw new GeneralSecurityException("Signed bytes differ from bytes that need to be signed.");
    } else {
      data.setBytesToSign(bytesToSign);
    }
    return sgn;
  }

  private byte[] createUniqueDummySignature(PresignData presign) {
    byte[] dummySignature = new byte[getEstimatedSignatureSize(presign)];
    random.nextBytes(dummySignature);
    return dummySignature;
  }

  private byte[] getSignatureFileRepresentation(byte[] signature, PresignData data) {
    ByteBuffer buffer = new ByteBuffer();
    buffer.append('<');
    for (int i = 0; i < signature.length; i++) {
      buffer.appendHex(signature[i]);
    }
    int estimatedSignatureSize = getEstimatedSignatureSize(data);
    if (logger.isDebugEnabled()) {
      logger.debug("Actual signature size was " + signature.length + " and our estimate is " + estimatedSignatureSize);
    }
    for (int i = signature.length; i < estimatedSignatureSize; i++) {
      buffer.appendHex((byte) 0);
    }
    buffer.append('>');
    byte[] fileBytes = buffer.toByteArray();
    FileUtils.close(buffer);
    return fileBytes;
  }

  public void sign(InputStream pdfPresignInputStream, OutputStream pdfOutputStream, PresignData data, byte[] signatureBytes) throws Exception {
    // Check if signature bytes are correct
    PublicKey publicKey = data.getCertificate().getPublicKey();
//    Signature signature = Signature.getInstance("SHA256" + "with" + publicKey.getAlgorithm());
    Signature signature = Signature.getInstance(DIGEST_ALGORITHM + "with" + publicKey.getAlgorithm());
    signature.initVerify(publicKey);
    signature.update(data.getBytesToSign());
    if (!signature.verify(signatureBytes)) {
      throw new GeneralSecurityException("Failed to verify user signature using public key stored in the certificate.");
    }

    PdfPKCS7 sgn = prepareSignature(data);
    sgn.setExternalDigest(signatureBytes, null, data.getCertificateChain()[0].getPublicKey().getAlgorithm());

    TSAClient tsc = null;
    if (data.isApplyTimestamp()) {
      tsc = getTsaClient();
    }

    byte[] encodedSig = sgn.getEncodedPKCS7(data.getSecondDigest(), data.getSigningDate(), tsc, data.getOcspBytes(), null, CRYPTO_STANDARD);

    int currentEstimate = getEstimatedSignatureSize(data);
    if (currentEstimate + 2 < encodedSig.length) {
      estimatedSignatureSize += encodedSig.length - currentEstimate;
      Exception exception = new Exception("Not enough space to write signature. We have reserved " + currentEstimate + "b and actual signature is "
          + encodedSig.length + "b long. Consider increasing initial size to avoid failures.");
      throw exception;
    }

    byte[] sigFileRepresentation = getSignatureFileRepresentation(encodedSig, data);

    byte[] buffer = new byte[1024 * 16];
    long bytesToCopy = data.getSignaturePositionInFile();
    int count;
    while ((count = pdfPresignInputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesToCopy))) > 0 && bytesToCopy > 0) {
      pdfOutputStream.write(buffer, 0, count);
      bytesToCopy -= count;
    }
    pdfOutputStream.write(sigFileRepresentation);
    if (pdfPresignInputStream.skip(sigFileRepresentation.length) < sigFileRepresentation.length) {
      logger.error("Failed to skip signature bytes. We might fail writing signed PDF document.");
    }
    while ((count = pdfPresignInputStream.read(buffer)) > 0) {
      pdfOutputStream.write(buffer, 0, count);
    }
    FileUtils.close(pdfPresignInputStream);
    FileUtils.close(pdfOutputStream);
  }

  @Override
  public void timestamp(InputStream pdfInputStream, OutputStream pdOutputStream) throws Exception {
    PdfStamper stp = setupStamper(pdfInputStream, pdOutputStream);
    timestamp(stp);
  }

  private void timestamp(PdfStamper stp) throws Exception {
    PdfSignatureAppearance sap = stp.getSignatureAppearance();
    LtvTimestamp.timestamp(sap, getTsaClient(), null);
  }

  private PdfSignatureAppearance buildSignatureAppearance(PdfReader reader, OutputStream pdfOutputStream, PresignData data,
      SignatureProperties signatureProperties) throws IOException, DocumentException, BadElementException, MalformedURLException {
    PdfStamper stp = setupStamper(reader, pdfOutputStream, signatureProperties);

//    try {
//      LtvTimestamp.timestamp(stp.getSignatureAppearance(), getTsaClient(), null);
//    } catch (GeneralSecurityException e) {
//      logger.error(e.getMessage(), e);
//    }

    X509Certificate[] certificateChain = data.getCertificateChain();

    PdfSignatureAppearance sap = stp.getSignatureAppearance();
    sap.setSignDate(data.getSigningDate());
    sap.setReason(signatureProperties.getReason());
    sap.setLocation(signatureProperties.getLocation());
    sap.setContact(signatureProperties.getContact());

    // Check if signature should be visualized
    if (signatureProperties.getPosition() != null) {
      // Setup visible signature position
      SignaturePosition position = SignaturePosition.parsePosition(signatureProperties.getPosition());
      boolean isVisible = position.setupVisibleSignature(reader, sap);

      if (isVisible) {
        // Build signature text
        MessageSourceAccessor msgSrcAccessor = new MessageSourceAccessor(messageSource);
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append(msgSrcAccessor.getMessage("PdfService.signer", new Object[] {CertificateUtils.getSubjectCN(certificateChain[0])}));
        textBuilder.append("\n").append(msgSrcAccessor.getMessage("PdfService.date", new Object[] {data.getSigningDate().getTime()}));
        if (StringUtils.hasText(signatureProperties.getReason())) {
          textBuilder.append("\n").append(msgSrcAccessor.getMessage("PdfService.reason", new Object[] {signatureProperties.getReason()}));
        }
        if (StringUtils.hasText(signatureProperties.getLocation())) {
          textBuilder.append("\n").append(msgSrcAccessor.getMessage("PdfService.location", new Object[] {signatureProperties.getLocation()}));
        }
        if (StringUtils.hasText(signatureProperties.getContact())) {
          textBuilder.append("\n").append(msgSrcAccessor.getMessage("PdfService.contact", new Object[] {signatureProperties.getContact()}));
        }
        sap.setLayer2Text(textBuilder.toString());

        // Set signature image
        if (signatureProperties.getSignatureImageUrl() != null) {
          sap.setRenderingMode(PdfSignatureAppearance.RenderingMode.GRAPHIC_AND_DESCRIPTION);
          sap.setSignatureGraphic(Image.getInstance(signatureProperties.getSignatureImageUrl()));
        }

        // Set background image
        if (signatureProperties.getBackgroundImageUrl() != null) {
          sap.setImage(Image.getInstance(signatureProperties.getBackgroundImageUrl()));
        }

        // Setup signature validity visualization
        sap.setAcro6Layers(!signatureProperties.isDisplayValidity());

        // Setup signature font
        Font signatureFont = FontFactory.getFont("signature_font", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, Font.UNDEFINED, Font.UNDEFINED, BaseColor.BLACK,
            BaseFont.CACHED);
        sap.setLayer2Font(signatureFont);

        // If only signature text is shown and no signature validity is shown,
        // iText leaves blank space at the top of the signature visualization
        // Work around this problem
        if (sap.getRenderingMode() == PdfSignatureAppearance.RenderingMode.DESCRIPTION && !signatureProperties.isDisplayValidity()) {
          renterLayer2Description(sap);
        }
      }
    }

    PdfSignature cryptoDictionary = setupCryptoDic(data, signatureProperties.getSigMetaData());
    sap.setCryptoDictionary(cryptoDictionary);

    HashMap<PdfName, Integer> exc = new HashMap<PdfName, Integer>();
    exc.put(PdfName.CONTENTS, getEstimatedSignatureSize(data) * 2 + 2);

    sap.preClose(exc);
    return sap;
  }
  /**
  * Returns an image with Smask off for ISO coplient (PDF/A) documents.
          * @param imageUrl
  * @param stp
  * @return
          * @throws BadElementException
  * @throws MalformedURLException
  * @throws IOException
  */
  protected Image getSafeImage(URL imageUrl, PdfStamper stp) throws BadElementException, MalformedURLException, IOException {
    Image img = Image.getInstance(imageUrl);
    if (stp.getWriter().isPdfIso()) {
      // disable transparency for ISO compliant documents
      img.setSmask(false);
    }
    return img;
  }

   /**
   * Append only.
   * @param pdfInputStream
   * @param pdOutputStream
   * @return
   * @throws DocumentException
   * @throws IOException
   * @throws IOException
   */
   protected PdfStamper setupStamper(InputStream pdfInputStream, OutputStream pdOutputStream) throws DocumentException, IOException {
     return setupStamper(new PdfReader(pdfInputStream), pdOutputStream);
   }

  protected PdfStamper setupStamper(PdfReader reader, OutputStream pdOutputStream) throws DocumentException, IOException {
    PdfStamper stp;
    PdfAConformanceLevel currentPdfAConformance = XmpReader.getConformance(reader.getMetadata());
    if (currentPdfAConformance != null) {
      logger.debug("Document claims to be PDF/A conformant " + currentPdfAConformance + ". Creating complient signature.");
      stp = PdfAStamper.createSignature(reader, pdOutputStream, DEF_PDF_VERSION, null, true, currentPdfAConformance);
    } else {
      stp = PdfStamper.createSignature(reader, pdOutputStream, DEF_PDF_VERSION, null, true);
    }
    return stp;
  }

  /**
   * PDF signatures are supported starting from PDF version 5, but Pades conformance insured from version 7. </p> If PDF
   * version is lower, build output file with correct PDF version and also overwrite all file rather than appending
   * changes to end of original file
   * <p>
   * Overridden for PDF-LT.
   * @param reader
   * @param pdfOutputStream
   * @param signatureProperties for basic signing case it is ignored.
   * @return Instantiated {@link PdfStamper}.
   * @throws DocumentException
   * @throws IOException
   */
  protected PdfStamper setupStamper(final PdfReader reader, final OutputStream pdfOutputStream, final SignatureProperties signatureProperties)
      throws DocumentException, IOException {
    boolean isLtDocumentCreation = signatureProperties.getDocMetaData() != null;
    // for document creation case or if we have older PDF version we overwrite original document
    boolean append = !isLtDocumentCreation && reader.getPdfVersion() >= MIN_PDF_LT_VERSION;
    // we where told to overwrite document, make sure that document is not signed
    if (!append && !reader.getAcroFields().getSignatureNames().isEmpty()) {
      // we can not append to the signed document but we can manage with original version for non pdf-lt case
      if (isLtDocumentCreation) {
        logger.info("PDF-LT document already signed");
//        throw new DocumentException("Document is signed and version is too low " + reader.getPdfVersion() + " while minimal PDF-LT supported version is "
//            + MIN_PDF_LT_VERSION);
      } else {
        logger.warn("Reducing PDF version due to compatibility reasons to be " + reader.getPdfVersion());
        append = true;
      }
    }
    if (append) {
      // no locking needed
      return PdfStamper.createSignature(reader, pdfOutputStream, DEF_PDF_VERSION, null, append);
    } else {
      // use synchronized method
      return setupStamperWithHead(reader, pdfOutputStream, isLtDocumentCreation, append);
    }
  }

  /**
   * This is synchronized block, which is able to "sneak" into itext and insert XMP extension header into result file.
   * FIXME should be removed as part of PDF-LT functionality
   * @param reader original document reader.
   * @param pdfOutputStream resulting out stream.
   * @param isLtDocumentCreation denotes that document should be overwritten with correct header and possibly metadata,
   *          no appending is allowed.
   * @param append should we append to or overwrite original document.
   * @return {@link PdfStamper}.
   * @throws DocumentException
   * @throws IOException
   */
  protected PdfStamper setupStamperWithHead(final PdfReader reader, final OutputStream pdfOutputStream, final boolean isLtDocumentCreation,
      boolean append) throws DocumentException, IOException {

    // FIXME PDF-LT
    // byte[] orig = PdfVersionImp.HEADER[2];
    // if (isLtDocumentCreation) {
    // byte[] add = PdfLtVersionImpl.LTUD_EXTENSION_HEADER_B;
    // // preserve all other headers
    // String head = readHeader(reader);
    // boolean hasHead = PdfFileUtils.EXT_PATTERN.matcher(head).find();
    //
    // // we have to overwrite file, make sure it has correct headers
    // // FIXME very dirty way, which makes all PDFs in this classloader to have PDF-LT extension header
    // ByteBuffer bb = new ByteBuffer(orig.length + add.length + head.length());
    // bb.append(head);
    // if (!hasHead) {
    // // original header might already have LTUd extension
    // bb.append(add);
    // }
    // bb.append(orig);
    // PdfVersionImp.HEADER[2] = bb.getBuffer();
    // }

    PdfStamper stp = PdfStamper.createSignature(reader, pdfOutputStream, MIN_PDF_LT_VERSION, null, append);

    // FIXME PDF-LT
    // if (isLtDocumentCreation) {
    // // reset it back after file header is written already
    // PdfVersionImp.HEADER[2] = orig;
    // }

    return stp;
  }

  /**
   * Using standard signature fields.
   * <p>
   * Overridden for PDF-LT.
   * @param data
   * @param signatureMetaData
   * @return Instantiated detached PKCS7 {@link PdfSignature}.
   */
  protected PdfSignature setupCryptoDic(PresignData data, SignatureMetaData signatureMetaData) {
    PdfSignature cryptoDictionary = new PdfSignature(PdfName.ADOBE_PPKLITE, PdfName.ETSI_CADES_DETACHED);
    cryptoDictionary.setReason(signatureMetaData.getReasonText());
    cryptoDictionary.setLocation(signatureMetaData.getLocation());
    cryptoDictionary.setContact(signatureMetaData.getContact());
    cryptoDictionary.setDate(new PdfDate(data.getSigningDate()));
    return cryptoDictionary;
  }

  private void renterLayer2Description(PdfSignatureAppearance sap) throws DocumentException {
    PdfTemplate layer2 = sap.getLayer(2);

    Rectangle rect = layer2.getBoundingBox();

    if (sap.getImage() != null) {
      if (sap.getImageScale() == 0) {
        layer2.addImage(sap.getImage(), rect.getWidth(), 0, 0, rect.getHeight(), 0, 0);
      } else {
        float usableScale = sap.getImageScale();
        if (sap.getImageScale() < 0) {
          usableScale = Math.min(rect.getWidth() / sap.getImage().getWidth(), rect.getHeight() / sap.getImage().getHeight());
        }
        float w = sap.getImage().getWidth() * usableScale;
        float h = sap.getImage().getHeight() * usableScale;
        float x = (rect.getWidth() - w) / 2;
        float y = (rect.getHeight() - h) / 2;
        layer2.addImage(sap.getImage(), w, 0, 0, h, x, y);
      }
    }

    Font font = sap.getLayer2Font();
    if (font == null) {
      font = new Font();
    }
    float size = font.getSize();

    Rectangle dataRect = new Rectangle(MARGIN, MARGIN, rect.getWidth() - MARGIN, rect.getHeight() - MARGIN);

    if (size <= 0) {
      Rectangle sr = new Rectangle(dataRect.getWidth(), dataRect.getHeight());
      size = ColumnText.fitText(font, sap.getLayer2Text(), sr, 12, sap.getRunDirection());
    }
    ColumnText ct = new ColumnText(layer2);
    ct.setRunDirection(sap.getRunDirection());
    ct.setSimpleColumn(new Phrase(sap.getLayer2Text(), font), dataRect.getLeft(), dataRect.getBottom(), dataRect.getRight(), dataRect.getTop(), size,
        Element.ALIGN_LEFT);
    ct.go();
  }

  private static byte[] calculateDigest(InputStream data, String digestAlgorithm) throws NoSuchAlgorithmException, IOException {
    MessageDigest messageDigest = MessageDigest.getInstance(digestAlgorithm);

    byte buf[] = new byte[8192];
    int n;
    while ((n = data.read(buf)) > 0) {
      messageDigest.update(buf, 0, n);
    }
    return messageDigest.digest();
  }

  private int getEstimatedSignatureSize(PresignData presign) {
    int estimated = presign.getEstimatedSignatureSize();
    if (estimated == 0) {
      estimated = calculateSignatureSize(null, presign.getOcspBytes(), presign.isApplyTimestamp());
      presign.setEstimatedSignatureSize(estimated);
    }
    return estimated;
  }

  private int calculateSignatureSize(Collection<byte[]> crlBytes, byte[] ocspBytes, boolean applyTs) {
    int estimatedSize = estimatedSignatureSize;
    if (crlBytes != null) {
      for (byte[] element : crlBytes) {
        estimatedSize += element.length + 10;
      }
    }
    if (ocspBytes != null) {
      logger.debug("OCSP size is " + ocspBytes.length + "b");
      estimatedSize += ocspBytes.length + 10;
    }
    if (applyTs)
      estimatedSize += 4192;
    return estimatedSize;
  }

  /**
   * Factory method which creates timestamp client.
   * @return Initialized instance.
   */
  public TSAClient getTsaClient() {
    if (tsaClient == null) {
      tsaClient = new TSAClientBouncyCastle(tsaUrl, null, null, 4096, DIGEST_ALGORITHM);
    }
    return tsaClient;
  }

}
