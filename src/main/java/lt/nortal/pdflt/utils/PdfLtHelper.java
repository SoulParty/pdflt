package lt.nortal.pdflt.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import lt.nortal.pdflt.domain.DocumentMetaData;
import lt.nortal.pdflt.domain.RegistrationMetaData;
import lt.nortal.pdflt.domain.SignatureMetaData;
import lt.nortal.pdflt.domain.SignatureProperties;
import lt.nortal.pdflt.domain.SigningType;
import lt.nortal.pdflt.xmp.struct.LTUEntity;
import lt.nortal.pdflt.xmp.struct.LTURegistration;
import lt.nortal.unisign.ws.types.pdflt.common.SigningReason;



/**
 * Created by DK on 7/7/16.
 */
public class PdfLtHelper {


	private PdfLtHelper() {
	}

	public static RegistrationMetaData createRegistrationMetaData(final LTURegistration ltuRegistration) {
		LTUEntity receiver = new LTUEntity();
		receiver.setName("RC");
		receiver.setCode("123");
		receiver.seteMail("RC");
		receiver.setAddress("RC");

		LTURegistration reception = new LTURegistration();
		reception.setNumber("123");
		reception.setCode("123");
		reception.setDate(Calendar.getInstance());

		return new RegistrationMetaData.Builder()
				.withRegistration(ltuRegistration)
				.withReception(reception)
				.withReceiver(receiver)
				.build();
	}

	public static DocumentMetaData createDocumentMetaData(final LTUEntity author, final LTUEntity recipient, final String pdfLtTitle,
			final String pdfLtDCIdentifier, final String pdfLtLang) {
		return new DocumentMetaData.Builder()
				.withAuthors(Collections.singletonList(author))
				.withRecipients(Collections.singletonList(recipient))
				.withTitle(pdfLtTitle)
				.withDocumentCopyIdentifier(pdfLtDCIdentifier)
				.withLanguages(Collections.singletonList(pdfLtLang))
				.build();
	}

	public static SignatureMetaData createSignatureMetaData(
			String name,
			String contact,
			String location,
			String reason,
			String role,
			String signerNotes,
			SigningType signingType) {
		return new SignatureMetaData.Builder()
				.withDate(Calendar.getInstance())
				.withName(name)
				.withContact(contact)
				.withLocation(location)
				.withReason(SigningReason.SIGNATURE)
				.withReasonText(reason)
				.withRole(role)
				.withSignerNotes(signerNotes)
				.withSigningType(signingType)
				.build();
	}

	public static void setPdfLtDetails(SignatureProperties signatureProperties,
			final String pdfLtName,
			final String pdfLtCode,
			final String pdfLtEmail,
			final String pdfLtAddress,

			final String pdfLtRecipientName,
			final String pdfLtRecipientLastName,
			final String pdfLtRecipientEmail,
			final String pdfLtRecipientAddress,

			final String pdfLtTitle,
			final String pdfLtLang,
			final String pdfLtDCIdentifier,

			final String pdfLtRegistration,
			final String pdfLtRegistrationCode,
			final String pdfLtRegistrationDate,

			final String pdfLtSignerName,
			final String pdfLtContact,
			final String pdfLtLocation,
			final String pdfLtReason,
			final String pdfLtSignerNotes,
			final String pdfLtRole) {

		LTUEntity author = new LTUEntity();
		author.setName(pdfLtName);
		author.setCode(pdfLtCode);
		author.seteMail(pdfLtEmail);
		author.setAddress(pdfLtAddress);

		LTUEntity recipient = new LTUEntity();
		author.setName(pdfLtRecipientName);
		author.setCode(pdfLtRecipientLastName);
		author.seteMail(pdfLtRecipientEmail);
		author.setAddress(pdfLtRecipientAddress);

		LTURegistration ltuRegistration = new LTURegistration();
		ltuRegistration.setNumber(pdfLtRegistration);
		ltuRegistration.setCode(pdfLtRegistrationCode);

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date registrationDate;
		try {
			registrationDate = df.parse(pdfLtRegistrationDate);
			String newDateString = df.format(registrationDate);
			System.out.println(newDateString);
			Calendar instance = Calendar.getInstance();
			instance.setTime(registrationDate);
			ltuRegistration.setDate(instance);
		} catch (Exception e) {
			ltuRegistration.setDate(Calendar.getInstance());
		}


		setPdfLtDetails(signatureProperties,
				PdfLtHelper.createDocumentMetaData(author, recipient, pdfLtTitle, pdfLtDCIdentifier, pdfLtLang),
				PdfLtHelper.createSignatureMetaData(pdfLtSignerName, pdfLtContact, pdfLtLocation, pdfLtReason, pdfLtSignerNotes, pdfLtRole, SigningType.SIGNATURE),
				PdfLtHelper.createRegistrationMetaData(ltuRegistration));
	}

	public static void setPdfLtDetails(SignatureProperties signatureProperties,
			final DocumentMetaData documentMetaData,
			final SignatureMetaData signatureMetaData,
			final RegistrationMetaData registrationMetaData) {

		signatureProperties.setPdfLt(true);
		signatureProperties.setDocMetaData(documentMetaData);
		signatureProperties.setSigMetaData(signatureMetaData);
		signatureProperties.setRegistrationMetaData(registrationMetaData);
	}
}
