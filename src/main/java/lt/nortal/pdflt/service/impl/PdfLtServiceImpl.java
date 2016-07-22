package lt.nortal.pdflt.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import lt.nortal.pdflt.PdfLtRuntimeException;
import lt.nortal.pdflt.domain.DocumentMetaData;
import lt.nortal.pdflt.domain.PresignData;
import lt.nortal.pdflt.domain.RegistrationMetaData;
import lt.nortal.pdflt.domain.SignatureMetaData;
import lt.nortal.pdflt.domain.SignatureProperties;
import lt.nortal.pdflt.xmp.LTUdSchema;
import lt.nortal.pdflt.xmp.struct.LTUEntity;
import lt.nortal.pdflt.xmp.struct.LTURegistration;
import lt.nortal.rc.unisign.util.pdf.XmpReader;
import lt.webmedia.sigute.service.common.utils.FileUtils;
import lt.webmedia.sigute.service.common.utils.PdfFileUtils;
import sun.nio.cs.StandardCharsets;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.exceptions.BadPasswordException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.ByteBuffer;
import com.itextpdf.text.pdf.PdfAConformanceLevel;
import com.itextpdf.text.pdf.PdfDate;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfLtSignature;
import com.itextpdf.text.pdf.PdfLtVersionImpl;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSignature;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.internal.PdfVersionImp;
import com.itextpdf.text.xml.xmp.DublinCoreSchema;
import com.itextpdf.text.xml.xmp.PdfAXmpWriter;
import com.itextpdf.text.xml.xmp.XmpArray;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service("pdfLtService")
public class PdfLtServiceImpl extends PdfServiceImpl {

	private static Logger LOGGER = LoggerFactory.getLogger(PdfLtServiceImpl.class);

	private String xmpSchema;
	private String identifier;

	@Value(value = "classpath:/xmp/PDF-LT-V1.0.xmp")
	public void setXmpSchema(Resource xmpSchema) throws IOException {
		ByteArrayOutputStream tempOutStream = new ByteArrayOutputStream();
		FileUtils.copyStreamsAndClose(xmpSchema.getInputStream(), tempOutStream);
		this.xmpSchema = new String(tempOutStream.toByteArray(), "UTF-8");
	}

	@Value(value = "classpath:/xmp/Identifier.xmp")
	public void setIdentifier(Resource identifier) throws IOException {
		ByteArrayOutputStream tempOutStream = new ByteArrayOutputStream();
		FileUtils.copyStreamsAndClose(identifier.getInputStream(), tempOutStream);
		this.identifier = new String(tempOutStream.toByteArray(), "UTF-8");
	}

	@Override
	protected PdfStamper setupStamper(PdfReader reader, final OutputStream pdfOutputStream, SignatureProperties signatureProps)
			throws DocumentException,
			IOException {
		PdfStamper stamper = super.setupStamper(reader, pdfOutputStream, signatureProps);

		boolean isB;
		boolean isA = false;

		XmpReader xmpReader = new XmpReader(reader.getMetadata());
		if (xmpReader.nodeExists("http://ns.adobe.com/xap/1.0/", "Identifier")) {
			isA = xmpReader.getNodeValue("http://ns.adobe.com/xap/1.0/", "Identifier").contains("A");
		}
		isB = isPdfLtB(reader);

		if (!isA && signatureProps.getDocMetaData() != null && isB) {
			DocumentMetaData documentMetaData = signatureProps.getDocMetaData();
			// it is document creation, we need to fill-in all document metadata correctly

			ByteArrayOutputStream xmpStream = new ByteArrayOutputStream();

			//TODO maybe there's some other metadata that should be saved?
			PdfAXmpWriter xmpWriter = new PdfAXmpWriter(xmpStream, getConformance(reader));


			if (!xmpReader.nodeExists("http://www.aiim.org/pdfa/ns/extension/", "schemas")) {
				xmpWriter.addRdfDescription("xmlns:pdfaExtension=\"http://www.aiim.org/pdfa/ns/extension/\"\n"
								+ "    xmlns:pdfaSchema=\"http://www.aiim.org/pdfa/ns/schema#\"\n"
								+ "    xmlns:pdfaProperty=\"http://www.aiim.org/pdfa/ns/property#\"\n"
								+ "    xmlns:pdfaType=\"http://www.aiim.org/pdfa/ns/type#\"\n"
								+ "    xmlns:pdfaField=\"http://www.aiim.org/pdfa/ns/field#\"",
						xmpSchema);
			}

			if (!xmpReader.nodeExists("http://ns.adobe.com/xap/1.0/", "Identifier")) {
				xmpWriter.addRdfDescription("xmlns:xmp=\"http://ns.adobe.com/xap/1.0/\"\n"
								+ "xmlns:xmpidq=\"http://ns.adobe.com/xmp/Identifier/qual/1.0/\"",
						identifier);
			}

			if (!xmpReader.nodeExists("http://purl.org/dc/elements/1.1/", "title") ||
					!xmpReader.nodeExists("http://purl.org/dc/elements/1.1/", "creator")) {
				DublinCoreSchema dc = new DublinCoreSchema();
				dc.addAuthor(documentMetaData.getAuthors().get(0).getName());
				dc.addTitle(documentMetaData.getTitle());
				if (!documentMetaData.getLanguages().isEmpty()) {
					XmpArray array = new XmpArray(XmpArray.UNORDERED);
					for (String lang : documentMetaData.getLanguages()) {
						array.add(lang);
					}
					dc.setProperty(DublinCoreSchema.LANGUAGE, array);
				}

				xmpWriter.addRdfDescription(dc);
			}
			//TODO else add existing values?


			LTUdSchema ltu = new LTUdSchema();

			if (!xmpReader.nodeExists("http://archyvai.lt/pdf-ltud/2013/metadata/", "authors") ||
			!xmpReader.nodeExists("http://archyvai.lt/pdf-ltud/2013/metadata/", "recipients")) {
				ltu.addStandardVersion();
				ltu.addAuthors(documentMetaData.getAuthors().toArray(new LTUEntity[documentMetaData.getAuthors().size()]));
				if (!documentMetaData.getRecipients().isEmpty()) {
					ltu.addRecipients(documentMetaData.getRecipients().toArray(new LTUEntity[documentMetaData.getRecipients().size()]));
				}

				if (documentMetaData.getInstanceIdentifier() != null) {
					ltu.addInstanceIdentifier(documentMetaData.getInstanceIdentifier());
				}

				if (!documentMetaData.getOriginalReceivers().isEmpty()) {
					ltu.addOriginalReceivers(
							documentMetaData.getOriginalReceivers().toArray(new LTUEntity[documentMetaData.getOriginalReceivers().size()]));
				}
				if (!documentMetaData.getOriginalReceptions().isEmpty()) {
					ltu.addOriginalReceptions(
							documentMetaData.getOriginalReceptions().toArray(new LTURegistration[documentMetaData.getOriginalReceptions().size()]));
				}
				if (!documentMetaData.getOriginalRegistrations().isEmpty()) {
					ltu.addOriginalRegistrations(documentMetaData.getOriginalRegistrations().toArray(
							new LTURegistration[documentMetaData.getOriginalRegistrations().size()]));
				}
			}


			if (!xmpReader.nodeExists("http://archyvai.lt/pdf-ltud/2013/metadata/", "registration") ||
			!xmpReader.nodeExists("http://archyvai.lt/pdf-ltud/2013/metadata/", "reception") ||
			!xmpReader.nodeExists("http://archyvai.lt/pdf-ltud/2013/metadata/", "receiver")) {
				if (signatureProps.getRegistrationMetaData() != null) {
					// it is registration case
					RegistrationMetaData regMeta = signatureProps.getRegistrationMetaData();
					ltu.addReception(regMeta.getReception());
					ltu.addRegistration(regMeta.getRegistration());
					ltu.addReceiver(regMeta.getReceiver());
				}
			}

			xmpWriter.addRdfDescription(ltu);
			xmpWriter.close();

			Map<String, String> info = reader.getInfo();
			info.put("Title", documentMetaData.getTitle());
			info.put("Creator", "VĮ Registrų centras Unisign 2.1.6");
			info.put("Author", documentMetaData.getAuthors().get(0).getName());

			stamper.setMoreInfo(info);
			stamper.setXmpMetadata(xmpStream.toByteArray());
//		} else if (signatureProps.getRegistrationMetaData() != null) { //TODO when is this used?
//			// it is registration case
//			RegistrationMetaData regMeta = signatureProps.getRegistrationMetaData();
//			LTUdSchema ltu = new LTUdSchema();
//			ltu.addReception(regMeta.getReception());
//			ltu.addRegistration(regMeta.getRegistration());
//			ltu.addReceiver(regMeta.getReceiver());
//
//			ByteArrayOutputStream xmpStream = new ByteArrayOutputStream();
////			XmpWriter xmpWrt = new XmpWriter(xmpStream);
//			PdfAXmpWriter xmpWriter = new PdfAXmpWriter(xmpStream, getConformance(reader));
//			xmpWriter.addRdfDescription(ltu);
//			xmpWriter.close();
//			stamper.setXmpMetadata(xmpStream.toByteArray());
		} else {
			throw new PdfLtRuntimeException();
		}

		return stamper;
	}

	private PdfAConformanceLevel getConformance(final PdfReader reader) throws IOException {
		if (reader.getMetadata() != null) {
			return new XmpReader(reader.getMetadata()).getConformance();
		} else {
			return PdfAConformanceLevel.PDF_A_2A;
		}
	}

	private boolean isPdfLtB(PdfReader pdfReader) {
		try {
			PdfReader reader = new PdfReader(pdfReader);

			PdfDictionary rootCatalog = reader.getCatalog();

//			B*Min pdf versija 1.7
			if (!PdfFileUtils.extractPdfVersion(reader).equals("1.7")) {
				return false;
			}

			PdfDictionary namesDictionary = rootCatalog.getAsDict(PdfName.NAMES);
			if (namesDictionary != null) {
//				B*Negali būti embedded files
//				B*Negali būti file specification dictionary
				if (rootCatalog.getAsDict(PdfName.NAMES) != null && namesDictionary.getAsDict(PdfName.EMBEDDEDFILES) != null && namesDictionary
						.getAsDict(PdfName.EF) != null) {
					return false;
				}
			}

//			B*Patikrinimas ar yra puslapių medis ( Dictionary “Pages”)
			if (rootCatalog.getAsDict(PdfName.PAGES) == null) {
				return false;
			}

//			B*PDF/A2 conformance patikrinimas
			XmpReader xmpReader = new XmpReader(reader.getMetadata());
			if (!PdfFileUtils.checkConformanceIsEqualOrAbovePdfA2(xmpReader.getConformance())) {
				return false;
			}

			AcroFields fields = reader.getAcroFields();
			for (String signame : fields.getSignatureNames()) {
				PdfDictionary sig = fields.getSignatureDictionary(signame);

//				B*Nustatyti ar PDF-LT-B
				if (sig.get(PdfName.REASON) == null || sig.get(PdfName.NAME) == null || sig.get(PdfName.M) == null) {
					return false;
				}

//				B*Parašo saugojimo vietos patikrinimas, ar yra  “Contents” žodyne.
				if (sig.get(PdfName.CONTENTS) == null) {
					return false;
				}

//				B*Negali būti sertifikavimo ar naudojimo teisių parašų
				//check signature "Reason"
				String reason = sig.get(PdfName.REASON) != null ? sig.get(PdfName.REASON).toString() : null;
				if (reason == null) {
					return false;
				}

//				B*Elektroniniame dokumente galimi tik PAdES-BES, PAdES-EPES arba PAdES-LTV
				if (!sig.get(PdfName.SUBFILTER).equals(PdfName.ETSI_CADES_DETACHED)) {
					return false;
				}
			}
		} catch (BadPasswordException e) {
//			*Šifravimo patikrinimas
			return false;
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	@Override
	protected PdfSignature setupCryptoDic(PresignData data, SignatureMetaData signatureMeta) {
		PdfLtSignature cryptoDictionary = new PdfLtSignature(PdfName.ADOBE_PPKLITE, PdfName.ETSI_CADES_DETACHED);
		cryptoDictionary.setName(signatureMeta.getName());
		cryptoDictionary.setReason(signatureMeta.getReasonText());
		cryptoDictionary.setRole(signatureMeta.getRole());
		cryptoDictionary.setDate(new PdfDate(signatureMeta.getDate()));
		cryptoDictionary.setSignerNotes(signatureMeta.getSignerNotes());
		return cryptoDictionary;
	}

	/**
	 * This is synchronized block, which is able to "sneak" into itext and insert XMP extension header into result file.
	 *
	 * @param reader               original document reader.
	 * @param pdfOutputStream      resulting out stream.
	 * @param isLtDocumentCreation denotes that document should be overwritten with correct header and possibly metadata,
	 *                             no appending is allowed.
	 * @param append               should we append to or overwrite original document.
	 * @return {@link PdfStamper}.
	 * @throws DocumentException
	 * @throws IOException
	 */
	@Override
	protected PdfStamper setupStamperWithHead(final PdfReader reader, final OutputStream pdfOutputStream, final boolean isLtDocumentCreation,
			boolean append) throws DocumentException, IOException {

		byte[] orig = PdfVersionImp.HEADER[2];
		if (isLtDocumentCreation) {
			byte[] add = PdfLtVersionImpl.LTUD_EXTENSION_HEADER_B;
			// preserve all other headers
			String head = PdfFileUtils.readHeader(reader);
			boolean hasHead = PdfFileUtils.EXT_PATTERN.matcher(head).find();

			// // we have to overwrite file, make sure it has correct headers
			// // FIXME very dirty way, which makes all PDFs in this classloader to have PDF-LT extension header
			ByteBuffer bb = new ByteBuffer(orig.length + add.length + head.length());
			bb.append(head);
			if (!hasHead) {
				// original header might already have LTUd extension
				bb.append(add);
			}
			bb.append(orig);
			PdfVersionImp.HEADER[2] = bb.getBuffer();
		}

		PdfStamper stp = PdfStamper.createSignature(reader, pdfOutputStream, MIN_PDF_LT_VERSION, null, append);

		if (isLtDocumentCreation) {
			// reset it back after file header is written already
			PdfVersionImp.HEADER[2] = orig;
		}

		return stp;
	}

}
