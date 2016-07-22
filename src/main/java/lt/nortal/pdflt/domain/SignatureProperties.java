package lt.nortal.pdflt.domain;

import java.io.Serializable;
import java.net.URL;

public class SignatureProperties implements Serializable {

  private static final long serialVersionUID = -1142801640602136764L;
  private boolean pdfLt;
  private SignatureMetaData sigMetaData = new SignatureMetaData();
  private SignatureDisplayProperties displayProps = new SignatureDisplayProperties();

  private DocumentMetaData docMetaData;
  private RegistrationMetaData registrationMetaData;

  public SignatureMetaData getSigMetaData() {
    return sigMetaData;
  }

  public void setSigMetaData(SignatureMetaData sigMetaData) {
    this.sigMetaData = sigMetaData;
  }

  public DocumentMetaData getDocMetaData() {
    return docMetaData;
  }

  public void setDocMetaData(DocumentMetaData docMetaData) {
    this.docMetaData = docMetaData;
  }

  public RegistrationMetaData getRegistrationMetaData() {
    return registrationMetaData;
  }

  public void setRegistrationMetaData(RegistrationMetaData registrationMetaData) {
    this.registrationMetaData = registrationMetaData;
  }

  public SignatureDisplayProperties getDisplayProps() {
    return displayProps;
  }

  public void setDisplayProps(SignatureDisplayProperties displayProps) {
    this.displayProps = displayProps;
  }

  public String getReason() {
    return this.sigMetaData.getReasonText();
  }

  public void setReason(String reason) {
    this.sigMetaData.setReasonText(reason);
  }

  public String getLocation() {
    return sigMetaData.getLocation();
  }

  public void setLocation(String location) {
    this.sigMetaData.setLocation(location);
  }

  public String getContact() {
    return sigMetaData.getContact();
  }

  public void setContact(String contact) {
    this.sigMetaData.setContact(contact);
  }

  public SigningType getSigningType() {
    return sigMetaData.getSigningType();
  }

  public void setSigningType(SigningType signingType) {
    this.sigMetaData.setSigningType(signingType);
  }

  public String getPosition() {
    return displayProps.getPosition();
  }

  public void setPosition(String position) {
    this.displayProps.setPosition(position);
  }

  public boolean isDisplayValidity() {
    return displayProps.isDisplayValidity();
  }

  public void setDisplayValidity(boolean displayValidity) {
    this.displayProps.setDisplayValidity(displayValidity);
  }

  public URL getSignatureImageUrl() {
    return displayProps.getSignatureImageUrl();
  }

  public void setSignatureImageUrl(URL signatureImageUrl) {
    this.displayProps.setSignatureImageUrl(signatureImageUrl);
  }

  public void setSignatureImageUrl(String signatureImageUrl) {
    this.displayProps.setSignatureImageUrl(signatureImageUrl);
  }

  public URL getBackgroundImageUrl() {
    return displayProps.getBackgroundImageUrl();
  }

  public void setBackgroundImageUrl(URL backgroundImageUrl) {
    this.displayProps.setBackgroundImageUrl(backgroundImageUrl);
  }

  public void setBackgroundImageUrl(String backgroundImageUrl) {
    this.displayProps.setBackgroundImageUrl(backgroundImageUrl);
  }

  public boolean isPdfLt() {
    return pdfLt;
  }

  public void setPdfLt(boolean pdfLt) {
    this.pdfLt = pdfLt;
  }
}
