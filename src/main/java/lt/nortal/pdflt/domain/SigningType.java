package lt.nortal.pdflt.domain;

public enum SigningType {

  SIGNATURE("Signature"), SIGNATURE_WITH_TIMESTAMP("SignatureWithTimestamp"), SIGNATURE_WITH_TIMESTAMP_OCSP("SignatureWithTimestampOCSP"), DOCUMENT_TIMESTAMP(
      "DocumentTimestamp");
  private final String value;

  SigningType(String v) {
    value = v;
  }

  public String value() {
    return value;
  }

  public static SigningType fromValue(String v) {
    for (SigningType c : SigningType.values()) {
      if (c.value.equals(v)) {
        return c;
      }
    }
    throw new IllegalArgumentException(v);
  }

}
