package lt.nortal.pdflt.service.impl;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import lt.nortal.everifier.ws.types.everifier.VerificationDetailStatus;
import lt.nortal.pdflt.service.PdfLtVerifer;
import lt.nortal.rc.unisign.util.pdf.XmpReader;
import lt.nortal.rc.verifier.service.PdfLtVerifier;
import lt.webmedia.sigute.service.common.utils.PdfFileUtils;

import com.itextpdf.text.exceptions.BadPasswordException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by DK on 6/26/16.
 */
@Service("pdfLtVerifier")
public class PdfLtVerifierImpl implements PdfLtVerifer {

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfLtVerifier.class);

	public Set<VerificationDetailStatus> verify(byte[] pdf, final String storagePath, final String clientId) throws IOException {
		Set<VerificationDetailStatus> detailStatuses = new HashSet<VerificationDetailStatus>();
		PdfReader reader = new PdfReader(pdf);
		try {
			PdfDictionary rootCatalog = reader.getCatalog();

//			B*Min pdf versija 1.7
			if (!PdfFileUtils.extractPdfVersion(reader).equals("1.7")) {
				detailStatuses.add(VerificationDetailStatus.LOW_PDF_VERSION);
			}

			PdfDictionary namesDictionary = rootCatalog.getAsDict(PdfName.NAMES);
			if (namesDictionary != null) {
//				B*Negali būti embedded files
//				B*Negali būti file specification dictionary
				if (rootCatalog.getAsDict(PdfName.NAMES) != null && namesDictionary.getAsDict(PdfName.EMBEDDEDFILES) != null && namesDictionary
						.getAsDict(PdfName.EF) != null) {
					detailStatuses.add(VerificationDetailStatus.DETECTED_EMBEDDED_FILE);
				}
			}

//			B*Patikrinimas ar yra puslapių medis ( Dictionary “Pages”)
			if (rootCatalog.getAsDict(PdfName.PAGES) == null) {
				detailStatuses.add(VerificationDetailStatus.NO_PAGES_DICTIONARY);
			}

//			B*PDF/A2 conformance patikrinimas
			XmpReader xmpReader = new XmpReader(reader.getMetadata());
			if (!PdfFileUtils.checkConformanceIsEqualOrAbovePdfA2(xmpReader.getConformance())) {
				detailStatuses.add(VerificationDetailStatus.NOT_PDFA);
			}
//			if (xmpReader.getConformanceString() != null) { //Online only
//				try {
//					File file = new File(storagePath + "/" + clientId + ".pdf");
//					FileUtils.writeByteArrayToFile(file, pdf);
//					HttpClient client = new DefaultHttpClient();
//					HttpPost post = new HttpPost("http://localhost:9000/api/validate/" + xmpReader.getConformanceString());
//					MultipartEntity entity = new MultipartEntity();
//					entity.addPart("file", new FileBody(file));
//					post.setEntity(entity);
//					HttpResponse verificationResponse = client.execute(post);
//					String responseString = EntityUtils.toString(verificationResponse.getEntity(), "UTF-8");
//					if (responseString.contains("\"compliant\":false")) {
//						LOGGER.info(responseString);
//						detailStatuses.add(VerificationDetailStatus.NOT_PDFA);
//					}
//				} catch (Exception e) {
//					LOGGER.error(e.getMessage(), e);
//					detailStatuses.add(VerificationDetailStatus.NOT_PDFA);
//				}
//			} else {
//				detailStatuses.add(VerificationDetailStatus.NOT_PDFA);
//			}

			boolean isPdfLtA = true;
			boolean isPdfLtB = true;

//			A*Header “Extensions” buvimo patikrinimas
			if (!PdfFileUtils.EXT_PATTERN.matcher(PdfFileUtils.readHeader(reader)).find()) {
//				detailStatuses.add(VerificationDetailStatus.NO_PDFLT_HEADER);
				isPdfLtA = false;
			}

			AcroFields fields = reader.getAcroFields();
			for (String signame : fields.getSignatureNames()) {
				PdfDictionary sig = fields.getSignatureDictionary(signame);

//				B*Nustatyti ar PDF-LT-B
				if (sig.get(PdfName.REASON) == null || sig.get(PdfName.NAME) == null || sig.get(PdfName.M) == null) {
					isPdfLtB = false;
				}

//				B*Parašo saugojimo vietos patikrinimas, ar yra  “Contents” žodyne.
				if (sig.get(PdfName.CONTENTS) == null) {
					detailStatuses.add(VerificationDetailStatus.SIGNATURE_NOT_IN_CONTENTS);
				}

//				B*Negali būti sertifikavimo ar naudojimo teisių parašų
				//check signature "Reason"
				String reason = sig.get(PdfName.REASON) != null ? sig.get(PdfName.REASON).toString() : null;
				if (reason == null) {
					detailStatuses.add(VerificationDetailStatus.BAD_REASON);
				}

//				A*Katalogo LTUd patikrinimas
//				A*Nustatyti ar PDF-LT-A
				if (sig.get(new PdfName("LTUd_Role")) == null || sig.get(new PdfName("LTUd_SignerNotes")) == null) {
					isPdfLtA = false;
				}

//				B*Elektroniniame dokumente galimi tik PAdES-BES, PAdES-EPES arba PAdES-LTV
				if (!sig.get(PdfName.SUBFILTER).equals(PdfName.ETSI_CADES_DETACHED)) {
					detailStatuses.add(VerificationDetailStatus.BAD_SUBFILTER);
				}
			}

			if (xmpReader.nodeExists("http://ns.adobe.com/xap/1.0/", "Identifier")) {
				if (xmpReader.getNodeValue("http://ns.adobe.com/xap/1.0/", "Identifier").contains("A") && isPdfLtA) {
//					*ANustatyti ar PDF-LT-A
//					*AMetadata patikrinimas pagal schema
					isPdfLtA = allPdfLtANodesExist(xmpReader);
				} else {
					detailStatuses.add(VerificationDetailStatus.NO_IDENTIFIER);
				}
			}

			if (detailStatuses.size() == 0) {
				if (isPdfLtA) {
					detailStatuses.add(VerificationDetailStatus.PDFLTA);
				} else if (isPdfLtB) {
					detailStatuses.add(VerificationDetailStatus.PDFLTB);
				}
			}

		} catch (BadPasswordException e) {
//			*Šifravimo patikrinimas
			LOGGER.error(e.getMessage(), e);
			detailStatuses.add(VerificationDetailStatus.PDF_ENCRYPTED);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			detailStatuses.add(VerificationDetailStatus.EXCEPTION);
		}

		return detailStatuses;
		//TODO http://lowagie.com/img/summit2012/pades.pdf page 20 for LTV
	}

	private boolean allPdfLtANodesExist(final XmpReader xmpReader) {
		return xmpReader.attributeExists("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "Description", "LTUd:standardVersion")
				|| xmpReader.nodeExists("http://archyvai.lt/pdf-ltud/2013/metadata/", "LTUd:standardVersion") &&
				xmpReader.nodeExists("http://archyvai.lt/pdf-ltud/2013/metadata/", "authors") &&
				xmpReader.nodeExists("http://archyvai.lt/pdf-ltud/2013/metadata/", "recipients") && //TODO check if not empty
				xmpReader.nodeExists("http://purl.org/dc/elements/1.1/", "title") &&
				xmpReader.nodeExists("http://purl.org/dc/elements/1.1/", "creator");
	}

	public boolean claimsToBePdfLt(byte[] pdf) throws IOException {
		PdfReader reader = new PdfReader(pdf);
		XmpReader xmpReader = new XmpReader(reader.getMetadata());
		return xmpReader.nodeExists("http://ns.adobe.com/xap/1.0/", "Identifier");
	}
}
