package lt.nortal.pdflt.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lt.nortal.pdflt.xmp.struct.LTUEntity;
import lt.nortal.pdflt.xmp.struct.LTURegistration;

public class DocumentMetaData implements Serializable {

	private static final long serialVersionUID = -6951569203848958182L;
	private String title;
	private String instanceIdentifier;
	private List<LTUEntity> authors = new ArrayList<LTUEntity>();
	private List<LTUEntity> recipients = new ArrayList<LTUEntity>();
	private List<String> languages;

	// copy metadata
	private List<LTURegistration> originalRegistrations = new ArrayList<LTURegistration>();
	private List<LTURegistration> originalReceptions = new ArrayList<LTURegistration>();
	private List<LTUEntity> originalReceivers = new ArrayList<LTUEntity>();

	//TODO make private and remove all new DocumentMetaData()
	public DocumentMetaData(final Builder builder) {
		this.title = builder.title;
		this.instanceIdentifier = builder.instanceIdentifier;
		this.authors = builder.authors;
		this.recipients = builder.recipients;
		this.languages = builder.languages;
		this.originalRegistrations = builder.originalRegistrations;
		this.originalReceptions = builder.originalReceptions;
		this.originalReceivers = builder.originalReceivers;
	}

	public DocumentMetaData() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getInstanceIdentifier() {
		return instanceIdentifier;
	}

	public void setInstanceIdentifier(String instanceIdentifier) {
		this.instanceIdentifier = instanceIdentifier;
	}

	public List<LTUEntity> getAuthors() {
		return authors;
	}

	public void setAuthors(List<LTUEntity> authors) {
		this.authors = authors;
	}

	public List<LTUEntity> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<LTUEntity> recipients) {
		this.recipients = recipients;
	}

	public List<String> getLanguages() {
		return languages;
	}

	public void setLanguages(List<String> languages) {
		this.languages = languages;
	}

	public List<LTURegistration> getOriginalRegistrations() {
		return originalRegistrations;
	}

	public void setOriginalRegistrations(List<LTURegistration> originalRegistrations) {
		this.originalRegistrations = originalRegistrations;
	}

	public List<LTURegistration> getOriginalReceptions() {
		return originalReceptions;
	}

	public void setOriginalReceptions(List<LTURegistration> originalReceptions) {
		this.originalReceptions = originalReceptions;
	}

	public List<LTUEntity> getOriginalReceivers() {
		return originalReceivers;
	}

	public void setOriginalReceivers(List<LTUEntity> originalReceivers) {
		this.originalReceivers = originalReceivers;
	}

	public static class Builder {
		private String title;
		private String instanceIdentifier;
		private List<LTUEntity> authors = new ArrayList<LTUEntity>();
		private List<LTUEntity> recipients = new ArrayList<LTUEntity>();
		private List<String> languages;
		private List<LTURegistration> originalRegistrations = new ArrayList<LTURegistration>();
		private List<LTURegistration> originalReceptions = new ArrayList<LTURegistration>();
		private List<LTUEntity> originalReceivers = new ArrayList<LTUEntity>();

		public Builder withTitle(final String title) {
			this.title = title;
			return this;
		}

		public Builder withDocumentCopyIdentifier(final String instanceIdentifier) {
			this.instanceIdentifier = instanceIdentifier;
			return this;
		}

		public Builder withAuthors(final List<LTUEntity> authors) {
			this.authors = authors;
			return this;
		}

		public Builder withRecipients(final List<LTUEntity> recipients) {
			this.recipients = recipients;
			return this;
		}

		public Builder withLanguages(final List<String> languages) {
			this.languages = languages;
			return this;
		}

		public Builder withOriginalRegistrations(final List<LTURegistration> originalRegistrations) {
			this.originalRegistrations = originalRegistrations;
			return this;
		}

		public Builder withOriginalReceptions(final List<LTURegistration> originalReceptions) {
			this.originalReceptions = originalReceptions;
			return this;
		}

		public Builder withOriginalReceivers(final List<LTUEntity> originalReceivers) {
			this.originalReceivers = originalReceivers;
			return this;
		}

		public DocumentMetaData build() {
			return new DocumentMetaData(this);
		}
	}
}
