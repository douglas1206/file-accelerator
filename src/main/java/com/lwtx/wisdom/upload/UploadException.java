package com.lwtx.wisdom.upload;

public class UploadException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UploadException(String message) {
		super(message);
	}

	public UploadException() {
		super();
	}

	public UploadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public UploadException(String message, Throwable cause) {
		super(message, cause);
	}

	public UploadException(Throwable cause) {
		super(cause);
	}
}
