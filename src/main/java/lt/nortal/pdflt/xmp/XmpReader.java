package lt.nortal.pdflt.xmp;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import com.itextpdf.text.pdf.PdfAConformanceLevel;
import com.itextpdf.text.xml.xmp.PdfASchema;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmpReader {

	private static Logger logger = LoggerFactory.getLogger(XmpReader.class);
	private Document domDocument;

	/**
	 * Constructs an XMP reader
	 * @param bytes the XMP content
	 * @throws ExceptionConverter
	 * @throws IOException
	 * @throws SAXException
	 */
	public XmpReader(byte[] bytes) throws IOException {
		if (bytes == null || bytes.length <= 0) {
			logger.warn("No XMP metadata provided with the document.");
			return;
		}
		try {
			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
			f.setExpandEntityReferences(true);
			f.setIgnoringComments(true);
			f.setIgnoringElementContentWhitespace(true);
			f.setValidating(false);
			f.setCoalescing(true);
			f.setNamespaceAware(true);

			DocumentBuilder db = f.newDocumentBuilder();
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			domDocument = db.parse(bais);
			domDocument.getDocumentElement().normalize();
		} catch (ParserConfigurationException e) {
			throw new ExceptionConverter(e);
		} catch (SAXException e) {
			throw new ExceptionConverter(e);
		}
	}

	public String getNodeValue(String namespaceURI, String localName) {
		if (domDocument == null) {
			return null;
		}
		NodeList nodes = domDocument.getElementsByTagNameNS(namespaceURI, localName);
		Node node;
		if (nodes.getLength() == 0)
			return null;
		if (nodes.getLength() > 1) {
			throw new AssertionError("Non unique entry for " + namespaceURI + ":" + localName);
		}
		String val = nodes.item(0).getTextContent();
		return (val != null && !val.trim().isEmpty()) ? val.trim() : null;
	}

	public String getNodeAttributeValue(String namespaceURI, String localName, String attributeName) {
		if (domDocument == null) {
			return null;
		}
		NodeList nodes = domDocument.getElementsByTagNameNS(namespaceURI, localName);
		if (nodes.getLength() == 0)
			return null;
		Element e = (Element) nodes.item(0);
		String attributeValue = e.getAttribute(attributeName);
		return (attributeValue != null && !attributeValue.trim().isEmpty()) ? attributeValue.trim() : null;
	}

	public int getNodeLength(String namespaceUri, String localName) {
		if (domDocument == null) {
			return 0;
		}
		NodeList nodes = domDocument.getElementsByTagNameNS(namespaceUri, localName);
		return nodes != null ? nodes.getLength() : 0;
	}

	public boolean nodeExists(String namespaceUri, String localName) {
		if (domDocument == null) {
			return false;
		}
		NodeList nodes = domDocument.getElementsByTagNameNS(namespaceUri, localName);
		return (nodes != null && nodes.getLength() > 0);
	}

	public boolean attributeExists(String namespaceUri, String localName, String attributeName) {
		String attributeValue = getNodeAttributeValue(namespaceUri, localName, attributeName);
		return attributeValue != null;
	}

	public PdfAConformanceLevel getConformance() {
		PdfAConformanceLevel lev = null;
		String conformance = getNodeValue(PdfASchema.DEFAULT_XPATH_URI, "conformance");
		String part = getNodeValue(PdfASchema.DEFAULT_XPATH_URI, "part");

		if (conformance == null) {
			conformance = getNodeAttributeValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "Description", PdfASchema.CONFORMANCE);
			part = getNodeAttributeValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "Description", PdfASchema.PART);
		}

		if (conformance != null && part != null) {
			String conformanceName = "PDF_A_" + part + conformance;
			lev = PdfAConformanceLevel.valueOf(conformanceName);
		}

		return lev;
	}

	public String getConformanceString() {
		PdfAConformanceLevel lev = null;
		String conformance = getNodeValue(PdfASchema.DEFAULT_XPATH_URI, "conformance");
		String part = getNodeValue(PdfASchema.DEFAULT_XPATH_URI, "part");

		if (conformance == null) {
			conformance = getNodeAttributeValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "Description", PdfASchema.CONFORMANCE);
			part = getNodeAttributeValue("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "Description", PdfASchema.PART);
		}

		if (conformance != null && part != null) {
			return part + conformance;
		}

		return null;
	}

	public static PdfAConformanceLevel getConformance(byte[] bytes) throws IOException {
		XmpReader r = new XmpReader(bytes);
		return r.getConformance();
	}
}
