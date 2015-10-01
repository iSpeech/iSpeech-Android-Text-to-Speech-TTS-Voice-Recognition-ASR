package org.ispeech;

/***
 * Contains the text and the confidence rating for the conversion, which is
 * a float from 0.0 to 1.0
 */
public class SpeechResult {
	/**
	 * The recognized text or null if recognition was unsuccessful.
	 */
	private String text = null;
	/**
	 * The confidence level of the recognition. 1.0 is equivalent to 100%
	 * accuracy.
	 */
	private float confidence;

	public SpeechResult(String s, float d) {
		this.text = s;
		this.confidence = d;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public float getConfidence() {
		return confidence;
	}

	public void setConfidence(float confidence) {
		this.confidence = confidence;
	}
}