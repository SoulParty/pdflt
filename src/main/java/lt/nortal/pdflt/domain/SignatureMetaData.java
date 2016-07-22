package lt.nortal.pdflt.domain;

import java.io.Serializable;
import java.util.Calendar;

import lt.nortal.unisign.ws.types.pdflt.common.SigningReason;

public class SignatureMetaData implements Serializable {
	private static final long serialVersionUID = -2470845427273580551L;

	//TODO make private and remove all new SignatureMetaData()
	public SignatureMetaData(Builder builder) {
		this.reason = builder.reason;
		this.name = builder.name;
		this.role = builder.role;
		this.date = builder.date;
		this.signerNotes = builder.signerNotes;
		this.reasonText = builder.reasonText;
		this.location = builder.location;
		this.contact = builder.contact;
		this.signingType = builder.signingType;
	}

	public SignatureMetaData() {
	}

	protected SigningReason reason;
	protected String name;
	protected String role;
	protected Calendar date;
	protected String signerNotes;

	private String reasonText;
	private String location;
	private String contact;
	private SigningType signingType;

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public SigningReason getReason() {
		return reason;
	}

	public void setReason(SigningReason reason) {
		this.reason = reason;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public Calendar getDate() {
		return date;
	}

	public void setDate(Calendar date) {
		this.date = date;
	}

	public String getSignerNotes() {
		return signerNotes;
	}

	public void setSignerNotes(String signerNotes) {
		this.signerNotes = signerNotes;
	}

	public String getReasonText() {
		return getReason() != null ? getReason().value() : reasonText;
	}

	public void setReasonText(String reasonText) {
		this.reasonText = reasonText;
	}

	public SigningType getSigningType() {
		return signingType;
	}

	public void setSigningType(SigningType signingType) {
		this.signingType = signingType;
	}

	public boolean isApplyTimestamp() {
		return this.signingType != null
				&& (this.signingType.equals(SigningType.SIGNATURE_WITH_TIMESTAMP_OCSP) || this.signingType
				.equals(SigningType.SIGNATURE_WITH_TIMESTAMP));
	}

	public boolean isApplyOCSP() {
		return this.signingType != null && this.signingType.equals(SigningType.SIGNATURE_WITH_TIMESTAMP_OCSP);
	}

	public static class Builder {

		private SigningReason reason;
		private String name;
		private String role;
		private Calendar date;
		private String signerNotes;
		private String reasonText;
		private String location;
		private String contact;
		private SigningType signingType;

		public Builder withReason(final SigningReason reason) {
			this.reason = reason;
			return this;
		}

		public Builder withName(final String name) {
			this.name = name;
			return this;
		}

		public Builder withRole(final String role) {
			this.role = role;
			return this;
		}

		public Builder withDate(final Calendar date) {
			this.date = date;
			return this;
		}

		public Builder withSignerNotes(final String signerNotes) {
			this.signerNotes = signerNotes;
			return this;
		}

		public Builder withReasonText(final String reasonText) {
			this.reasonText = reasonText;
			return this;
		}

		public Builder withLocation(final String location) {
			this.location = location;
			return this;
		}

		public Builder withContact(final String contact) {
			this.contact = contact;
			return this;
		}

		public Builder withSigningType(SigningType signingType) {
			this.signingType = signingType;
			return this;
		}

		//return fully build object
		public SignatureMetaData build() {
			return new SignatureMetaData(this);
		}
	}
}
