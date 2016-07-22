package lt.nortal.pdflt.domain;

import java.io.Serializable;

import lt.nortal.pdflt.xmp.struct.LTUEntity;
import lt.nortal.pdflt.xmp.struct.LTURegistration;

public class RegistrationMetaData implements Serializable {
	private static final long serialVersionUID = -1077507407239705566L;

	private LTURegistration registration;
	private LTURegistration reception;
	private LTUEntity receiver;

	public RegistrationMetaData(final Builder builder) {
		this.registration = builder.registration;
		this.reception = builder.reception;
		this.receiver = builder.receiver;
	}

	public RegistrationMetaData() {
	}

	public LTURegistration getRegistration() {
		return registration;
	}

	public void setRegistration(LTURegistration registration) {
		this.registration = registration;
	}

	public LTURegistration getReception() {
		return reception;
	}

	public void setReception(LTURegistration reception) {
		this.reception = reception;
	}

	public LTUEntity getReceiver() {
		return receiver;
	}

	public void setReceiver(LTUEntity receiver) {
		this.receiver = receiver;
	}

	public static class Builder {
		private LTURegistration registration;
		private LTURegistration reception;
		private LTUEntity receiver;

		public Builder withRegistration(final LTURegistration registration) {
			this.registration = registration;
			return this;
		}

		public Builder withReception(final LTURegistration reception) {
			this.reception = reception;
			return this;
		}

		public Builder withReceiver(final LTUEntity receiver) {
			this.receiver = receiver;
			return this;
		}

		public RegistrationMetaData build() {
			return new RegistrationMetaData(this);
		}
	}
}
