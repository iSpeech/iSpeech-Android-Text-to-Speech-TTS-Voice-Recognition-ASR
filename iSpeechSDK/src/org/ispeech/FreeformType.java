package org.ispeech;

/**
 * Freeform types used by SpeechRecognizer
 *
 */
public enum FreeformType {
	/**
	 * Disable free form speech recognition.
	 */
	FREEFORM_DISABLED(0),
	/**
	 * A SMS or TXT message.
	 */
	FREEFORM_SMS(1),
	/**
	 * A voice mail transcription
	 */
	FREEFORM_VOICEMAIL(2),
	/**
	 * Free form dictation, such as a written document
	 */
	FREEFORM_DICTATION(3),
	/**
	 * A message addressed to another person.
	 */
	FREEFORM_MESSAGE(4),
	/**
	 * A message for an instant message client
	 */
	FREEFORM_INSTANT_MESSAGE(5),
	/**
	 * General transcription
	 */
	FREEFORM_TRANSCRIPT(6),
	/**
	 * A memo or a list of items
	 */
	FREEFORM_MEMO(7);

	private final int value;

	private FreeformType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

}
