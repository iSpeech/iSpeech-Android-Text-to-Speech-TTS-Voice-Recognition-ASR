package org.ispeech;


/**
 * Used to notify when the synthesis state changes
 */
public abstract class SpeechSynthesisEvent {

	private static final String TAG = "iSpeech SDK";

	/**
	 * Types of synthesis event
	 */
	public enum EventType {
		PLAY_STARTED, PLAY_SUCCESSFUL, PLAY_STOPPED, PLAY_FAILURE, PLAY_CANCELED
	};

	/**
	 * The media started playing
	 */
	public void onPlayStart() {
	}

	/**
	 * The media was played successfully to the end
	 */
	public void onPlaySuccessful() {
	};

	/**
	 * The media was stopped either by calling the Stop() method, or interrupted
	 * by another media event.
	 */
	public void onPlayStopped() {
	};

	/**
	 * The media failed to play.
	 */
	public void onPlayFailed(Exception e) {
	};

	/**
	 * The media failed to play.
	 */
	public void onPlayCanceled() {
	};

	
	/**
	 * Dispatch SpeechSynthesis event changes.
	 * 
	 * @param event
	 *            PLAY_SUCCESSFUL, PLAY_STOPPED, or PLAY_FAILURE
	 * @param param
	 *            Contains exception information or null if none is available
	 */
	void stateChanged(EventType event, Object param) {
		switch (event) {
		case PLAY_STARTED:
			onPlayStart();
			break;
		case PLAY_SUCCESSFUL:
			onPlaySuccessful();
			break;
		case PLAY_STOPPED:
			onPlayStopped();
			break;
		case PLAY_FAILURE:
			onPlayFailed((Exception) param);
			break;
		case PLAY_CANCELED:
			onPlayCanceled();
			break;
		}
	};
}