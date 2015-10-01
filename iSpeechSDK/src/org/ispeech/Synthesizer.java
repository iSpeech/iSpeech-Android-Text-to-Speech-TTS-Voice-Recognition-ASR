package org.ispeech;

import org.ispeech.error.BusyException;
import org.ispeech.error.NoNetworkException;

/**
 * Synthesizer interface
 */
public interface Synthesizer {
	/**
	 * Converts text to speech
	 * @param text The text to convert into audio.
	 * @throws BusyException 
	 * @throws NoNetworkException 
	 */
	public void speak(String text) throws BusyException, NoNetworkException;

	/**
	 * Stops the currently playing audio.
	 */
	public void stop();

	/**
	 * Add meta value to synthesizer
	 * @param type
	 * @param value
	 */
	public void addMeta(MetaType type, String value);

	/**
	 * Clears optional parameters.
	 */
	public void clearOptionalCommand();

	/**
	 * Specify additional parameters to send to the server.
	 */
	public void addOptionalCommand(String command, String parameter);

}
