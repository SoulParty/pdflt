package lt.nortal.pdflt.domain;

import java.io.Serializable;
import java.security.cert.X509Certificate;
import java.util.Calendar;

public class PresignData implements Serializable {
  private static final long serialVersionUID = -6357119809768874547L;

  private byte[] bytesToSign;
  private boolean applyTimestamp;
  private boolean convertToLtv;
  private Calendar signingDate;
  private byte[] ocspBytes;
  private X509Certificate[] certificateChain;
  private long signaturePositionInFile;
  private byte[] secondDigest;
  private int estimatedSignatureSize;

  public void setBytesToSign(byte[] bytesToSign) {
    this.bytesToSign = bytesToSign;
  }

  public byte[] getBytesToSign() {
    return bytesToSign;
  }

  public boolean isConvertToLtv() {
    return convertToLtv;
  }

  public void setConvertToLtv(final boolean convertToLtv) {
    this.convertToLtv = convertToLtv;
  }

  public boolean isApplyTimestamp() {
    return applyTimestamp;
  }

  public void setApplyTimestamp(boolean applyTimestamp) {
    this.applyTimestamp = applyTimestamp;
  }

  public Calendar getSigningDate() {
    return signingDate;
  }

  public void setSigningDate(Calendar signingDate) {
    this.signingDate = signingDate;
  }

  public byte[] getOcspBytes() {
    return ocspBytes;
  }

  public void setOcspBytes(byte[] ocspBytes) {
    this.ocspBytes = ocspBytes;
  }

  public X509Certificate[] getCertificateChain() {
    return certificateChain;
  }

  public void setCertificateChain(X509Certificate[] certificateChain) {
    this.certificateChain = certificateChain;
  }

  public X509Certificate getCertificate() {
    return certificateChain[0];
  }

  public void setSignaturePositionInFile(long signaturePositionInFile) {
    this.signaturePositionInFile = signaturePositionInFile;
  }

  public long getSignaturePositionInFile() {
    return signaturePositionInFile;
  }

  public void setSecondDigest(byte[] secondDigest) {
    this.secondDigest = secondDigest;
  }

  public byte[] getSecondDigest() {
    return secondDigest;
  }

  public int getEstimatedSignatureSize() {
    return estimatedSignatureSize;
  }

  public void setEstimatedSignatureSize(int estimatedSignatureSize) {
    this.estimatedSignatureSize = estimatedSignatureSize;
  }

}
