package lt.nortal.pdflt.domain;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidParameterException;

public class SignatureDisplayProperties implements Serializable {
  private static final long serialVersionUID = -6936652281846863398L;
  private String position;
  private boolean displayValidity;
  private URL signatureImageUrl;
  private URL backgroundImageUrl;

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public boolean isDisplayValidity() {
    return displayValidity;
  }

  public void setDisplayValidity(boolean displayValidity) {
    this.displayValidity = displayValidity;
  }

  public URL getSignatureImageUrl() {
    return signatureImageUrl;
  }

  public void setSignatureImageUrl(URL signatureImageUrl) {
    this.signatureImageUrl = signatureImageUrl;
  }

  public void setSignatureImageUrl(String signatureImageUrl) {
    try {
      this.setSignatureImageUrl(signatureImageUrl == null ? null : new URL(signatureImageUrl));
    } catch (MalformedURLException e) {
      throw new InvalidParameterException("Invalid URL");
    }
  }

  public URL getBackgroundImageUrl() {
    return backgroundImageUrl;
  }

  public void setBackgroundImageUrl(URL backgroundImageUrl) {
    this.backgroundImageUrl = backgroundImageUrl;
  }

  public void setBackgroundImageUrl(String backgroundImageUrl) {
    try {
      this.setBackgroundImageUrl(backgroundImageUrl == null ? null : new URL(backgroundImageUrl));
    } catch (MalformedURLException e) {
      throw new InvalidParameterException("Invalid URL");
    }
  }

}
