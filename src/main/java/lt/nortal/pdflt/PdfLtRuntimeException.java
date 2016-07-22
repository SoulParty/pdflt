package lt.nortal.pdflt;

/**
 * Created by DK on 7/18/16.
 */

public class PdfLtRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PdfLtRuntimeException() {
		super("PDF does not conform to PDF-LT standard");
	}
	/**
	 * Constructs a new exception with the specified detail message.
	 * @param message the detail message
	 */
	public PdfLtRuntimeException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 * @param message the detail message
	 * @param cause   the cause
	 */
	public PdfLtRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

}
