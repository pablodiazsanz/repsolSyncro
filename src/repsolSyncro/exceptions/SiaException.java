package repsolSyncro.exceptions;

public class SiaException extends Exception {
	
	private String errorCode;
	private String message;
	private Throwable cause;

	public SiaException() {
		super();
		// TODO Auto-generated constructor stub
	}

	// @Override
	public SiaException(String errorCode, String message, Throwable cause) {
		this.errorCode = errorCode;
		this.message = message;
		this.cause = cause;
	}

	// @Override
	public SiaException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	// @Override
	public SiaException(String error) {
		super(error);
		// TODO Auto-generated constructor stub
	}

	public String getErrorCode() {
		return errorCode;
	}

	public String getMessage() {
		return message;
	}

	public Throwable getCause() {
		return cause;
	}
	
	

}
