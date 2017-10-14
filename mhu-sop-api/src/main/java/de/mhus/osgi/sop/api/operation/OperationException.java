package de.mhus.osgi.sop.api.operation;

public class OperationException extends Exception {

	private static final long serialVersionUID = 1L;
	private long returnCode;
	private String caption;

	public OperationException(long rc) {
		this(rc,"", null, null);
	}
	
	public OperationException(long rc, String msg) {
		this(rc, msg, null, null);
	}
	
	public OperationException(long rc, String msg, Throwable cause) {
		super(msg, cause);
		this.returnCode = rc;
	}

	public OperationException(long rc, String msg, String caption) {
		this(rc, msg, caption, null);
	}
	
	public OperationException(long rc, String msg, String caption, Throwable cause) {
		super(msg, cause);
		this.returnCode = rc;
		this.caption = caption;
	}
	
	public long getReturnCode() {
		return returnCode;
	}

	public String toString() {
		return returnCode + " " + super.toString();
	}
	public String getCaption() {
		return caption;
	}
	
}
