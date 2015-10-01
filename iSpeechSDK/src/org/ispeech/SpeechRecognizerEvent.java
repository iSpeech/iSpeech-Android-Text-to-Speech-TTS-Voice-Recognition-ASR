package org.ispeech;

/**
 * Used to notify when the recognition state changes
 */
public abstract class SpeechRecognizerEvent {

	private static final String TAG = "iSpeech SDK";

	/**
	 * Types of recognizer event
	 *
	 */
	public enum EventType {
		RECORDING_COMPLETE, RECOGNITION_COMPLETE, RECORDING_CANCELED, RECOGNITION_CANCELED, ERROR
	};

	/***
	 * Fired when recording timer expires or the user dismisses the dialog.
	 */
	public void onRecordingComplete() {
	};

	/***
	 * Fired when audio recording has been committed. No new audio will be
	 * recorded after you receive this event.
	 */
	abstract public void onRecognitionComplete(SpeechResult result);

	/***
	 * Fired when audio recording has been canceled. A user can cancel
	 * recording by pressing the cancel button.
	 */
	public void onRecordingCancelled() {
	};

	/***
	 * Fired when audio recorder throws errors. 
	 */
	public void onError(Exception exception) {
		
	}

	void stateChanged(EventType event, Object param) {
		switch (event) {
		case RECORDING_COMPLETE:
			onRecordingComplete();
			break;
		case RECOGNITION_COMPLETE:
			onRecognitionComplete((SpeechResult) param);
			break;
		case RECORDING_CANCELED:
			onRecordingCancelled();
			break;
		case ERROR:
			onError((Exception) param);
			break;
		}
	};
}
