package org.ispeech;

public enum MetaType {
	/***
	 * First name of user.
	 */
	META_FIRST_NAME("meta_first"),

	/***
	 * Last name of user.
	 */
	META_LAST_NAME("meta_last"),
	/***
	 * Identifier of user.
	 */
	META_ID("meta_id"),

	/***
	 * Operating system of user.
	 */
	META_OS("meta_os"),

	/***
	 * Phone type of user.
	 */
	META_PHONETYPE("meta_phonetype"),

	/***
	 * Provider of user.
	 */
	META_PROVIDER("meta_provider"),

	/***
	 * Your application's name.
	 */
	META_APPLICATION_NAME("meta_app"),

	/***
	 * The latitude of the user .
	 */
	META_LATITUDE("lat"),
	/***
	 * The longitude of the user.
	 */
	META_LONGITUDE("lon");

	private String value;

	private MetaType(String value) {
		this.value = value;
	}

	String getValue() {
		return value;
	}
}