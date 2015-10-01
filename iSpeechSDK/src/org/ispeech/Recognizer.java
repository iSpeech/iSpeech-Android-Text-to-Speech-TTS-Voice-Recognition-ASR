package org.ispeech;

import org.ispeech.error.BusyException;
import org.ispeech.error.NoNetworkException;

import android.app.Activity;

/**
 * Recognizer interface
 */
public interface Recognizer {

	/**
	 * Start recording.
	 * 
	 * @param speechRecognizerEvent
	 * @throws BusyException
	 * @throws NoNetworkException
	 */
	public void startRecord(Activity act, SpeechRecognizerEvent speechRecognizerEvent) throws BusyException, NoNetworkException;

	/**
	 * Stops the recording process.
	 */
	public void stopRecord();

	/**
	 * Cancels a recording in progress.
	 */
	public void cancelRecord();

	/**
	 * Cancels the processing of the data when it was already recorded
	 */
	public void cancelProcessing();

	/***
	 * Adds some command phrases.
	 * 
	 * @param commandPhrases
	 *            An array containing your command phrases
	 */
	public void addCommand(String[] commandPhrases);

	/***
	 * Adds a new command phrase.
	 * 
	 * @param phrase
	 *            Your command phrase
	 */
	public void addCommand(String phrase);

	/**
	 * Clears all commands.
	 */
	public void clearCommand();

	/**
	 * Adds an alias to use inside of a command.
	 * 
	 * @param name
	 *            The name of your alias for referencing inside of your
	 *            commands.
	 * @param values
	 *            The list of phrases for this alias.
	 */
	public void addAlias(String name, String[] values);

	/**
	 * Clears all aliases.
	 */
	public void clearAlias();

	/**
	 * Add meta value to synthesizer
	 * 
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

	/***
	 * Set to free form recognition.
	 */
	public void setFreeForm(FreeformType freeFormType);

	/**
	 * Get current free form type
	 * 
	 * @return {@link FreeformType}
	 */
	public FreeformType getFreeForm();

	/***
	 * Test this to determine if this is still running
	 * 
	 * @return true if the recognizer is still running
	 */
	public boolean isRunning();

	/**
	 * Sets the language model
	 */
	public void setModel(String model);

	/**
	 * Set the locale for speech recognition
	 */
	public void setLocale(String locale);

	/**
	 * Disable or enable the audio chime
	 */
	public void setChime(boolean isOn);
	
	/**
	 * Set the dialog enabled
	 */
	public void setDialogEnabled(boolean enabled);
}
