package org.ispeech.error;

/**
 * Generated when the server returns an error message instead of a successful
 * transaction.
 */
public class ApiException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2883987552548504763L;

	public ApiException(String string) {
		super(string);
	}

}
