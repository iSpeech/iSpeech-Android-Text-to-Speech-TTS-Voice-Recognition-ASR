package org.ispeech;

import org.ispeech.error.ApiException;
import org.ispeech.error.BusyException;
import org.ispeech.error.InvalidApiKeyException;
import org.ispeech.error.NoNetworkException;
import org.ispeech.tools.Utilities;

import android.app.Activity;
import android.content.Context;

/**
 * Contains methods to recognize spoken audio and convert free form speech into
 * text.
 *
 */
public class SpeechRecognizer implements Recognizer {

	private String _key;

	private Recognizer currentRecognizer;
	private FreeformRecognizerImpl freeformRecognizer;

	private static SpeechRecognizer instance;

	/**
	 * Gets an instance of the iSpeech SpeechRecognizer class. The ApiKey
	 * parameter is only required on initial call to this method.
	 * 
	 * @param context
	 *            {@link Context} object
	 * @param isProduction
	 *            Set to true to specify that this application is in production
	 *            mode. Set to false to use the sandbox servers. Your key must
	 *            be provisioned for production use in order to deploy your
	 *            application. Note: This flag is global.
	 * @throws InvalidApiKeyException
	 */
	public static SpeechRecognizer getInstance(Context context) throws InvalidApiKeyException {
		String apiKey = Utilities.getApiKey(context);

		if (instance == null)
			instance = new SpeechRecognizer(context, apiKey);
		else {
			FreeformRecognizerImpl.getInstance(context, apiKey);
			instance._key = apiKey;
		}
		return instance;
	}

	private SpeechRecognizer(Context context, String apiKey) throws InvalidApiKeyException {
		if (apiKey == null || apiKey.length() != 32)
			throw new InvalidApiKeyException();
		this._key = apiKey;
		freeformRecognizer = FreeformRecognizerImpl.getInstance(context);
	}

	/**
	 * Starts recording audio for SpeechRecognition
	 * 
	 * @throws BusyException
	 *             If a player could not be created
	 * @throws NoNetworkException
	 */
	@Override
	public void startRecord(Activity act, SpeechRecognizerEvent speechRecognizerEvent) throws BusyException, NoNetworkException {
		try {
			currentRecognizer.startRecord(act, speechRecognizerEvent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Enables and disables silence detection.
	 * 
	 * @param silenceDetection
	 *            {@link boolean} set to true to enable silence detection, and
	 *            false to disable silence detection.
	 */
	public void setSilenceDetection(boolean silenceDetection) {
		freeformRecognizer.setSilenceDetection(silenceDetection);
	}

	/***
	 * Stops the recording process
	 * 
	 * @throws ApiException
	 *             The server could not process your command and an API
	 *             exception occurred.
	 */
	@Override
	public void stopRecord() {
		currentRecognizer.stopRecord();
	}

	/**
	 * Cancels voice recording that is in progress
	 */
	@Override
	public void cancelRecord() {
		currentRecognizer.cancelRecord();
	}

	/***
	 * To disable loading dialogs and prompts please contact sales@iSpeech.org
	 */
	public void toDisableTheDialogContact_salesATiSpeechDOTorg() {

	}

	/**
	 * Cancels the processing of the recorded voice data i.e. does not send the
	 * data to the server or ignores the responce that comes back from the
	 * server if the data was already sent.
	 */
	@Override
	public void cancelProcessing() {
		currentRecognizer.cancelProcessing();
	}

	/**
	 * Clears all commands
	 */
	@Override
	public void clearCommand() {
		currentRecognizer.clearCommand();
	}

	/***
	 * Adds a command phrases.
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * SpeechRecognizer rec = SpeechRecognizer.getInstance(&quot;APIKEY&quot;);
	 * rec.addCommand(new String[]{&quot;yes&quot;,&quot;no&quot;};);
	 * </pre>
	 * <p>
	 * The user can now speak "Yes" or "No" and it will be recognized correctly.
	 * 
	 * @param commandPhrases
	 *            An array containing your command phrases
	 */
	@Override
	public void addCommand(String[] commandPhrases) {
		/* removing commandRecognizerImpl *///
		//		smallRecognizer.addCommand(commandPhrases);
		currentRecognizer.addCommand(commandPhrases);
	}

	/***
	 * Set to free form recognition. Will not use any command or alias list.
	 * Your API key must be provisioned to use this feature. One of the
	 * {@link FreeformType} values.
	 */
	public void setFreeForm(FreeformType freeFormType) {
		currentRecognizer = freeformRecognizer;
		currentRecognizer.setFreeForm(freeFormType);
	}

	/***
	 * Get current free form value
	 * 
	 * @return free form value
	 */
	@Override
	public FreeformType getFreeForm() {
		return currentRecognizer.getFreeForm();
	}

	/***
	 * Test this to determine if this is still running
	 * 
	 * @return true if the recognizer is still running
	 */
	@Override
	public boolean isRunning() {
		return currentRecognizer.isRunning();
	}

	/**
	 * <p>
	 * Adds an alias to use inside of a command. You can reference the added
	 * alias using %ALIASNAME% from within a command. Alias names are
	 * automatically capitalized. Note: You can only have a maximum of two
	 * aliases per command.
	 * </p>
	 * <p>
	 * Example:
	 * </p>
	 * 
	 * <pre>
	 * SpeechRecognizer rec = SpeechRecognizer.getInstance(&quot;APIKEY&quot;);
	 * String[] names = new String[] { &quot;jane&quot;, &quot;bob&quot;, &quot;john&quot; };
	 * rec.addAlias(&quot;NAMES&quot;, names);
	 * rec.addCommand(&quot;call %NAMES%&quot;);
	 * </pre>
	 * <p>
	 * The user can now speak "call john" and it will be recognized correctly.
	 * </p>
	 * 
	 * @param name
	 *            The name of your alias for referencing inside of your
	 *            commands.
	 * @param values
	 *            The list of phrases for this alias.
	 */
	@Override
	public void addAlias(String name, String[] values) {
		currentRecognizer.addAlias(name.toUpperCase(), values);
	}

	/***
	 * Clear all aliases
	 */
	@Override
	public void clearAlias() {
		currentRecognizer.clearAlias();
	}

	/**
	 * Set the maximum recording time in milliseconds
	 * 
	 * @param timeout
	 */
	public void setTimeout(int timeout) {
		freeformRecognizer.setTimeout(timeout);
	}

	/**
	 * Add meta value to synthesizer
	 * 
	 * @param type
	 * @param value
	 */
	@Override
	public void addMeta(MetaType type, String value) {
		freeformRecognizer.addMeta(type, value);
	}

	/***
	 * Clear all optional commands
	 */
	@Override
	public void clearOptionalCommand() {
		freeformRecognizer.clearOptionalCommand();
	}

	/**
	 * Specify additional parameters to send to the server.
	 */
	@Override
	public void addOptionalCommand(String command, String parameter) {
		freeformRecognizer.addOptionalCommand(command, parameter);
	}

	@Override
	public void addCommand(String phrase) {
		currentRecognizer.addCommand(phrase);
	}

	@Override
	public void setModel(String model) {
		freeformRecognizer.setModel(model);
	}

	/**
	 * Sets to Locale for recognizer. Example:
	 * 
	 * <pre>
	 * {
	 * 	&#064;code
	 * 	SpeechRecognizer recognizer;
	 * 	recognizer = SpeechRecognizer.getInstance(this);
	 * 	recognizer.setLocale(&quot;en-US&quot;); //set to US-English
	 * }
	 * </pre>
	 * 
	 * @param locale
	 *            currently implemented locales: *
	 * 
	 *            <pre>
	 * Catalan 		(Catalan) (ca-ES) 
	 * Chinese 		(Taiwan) (zh-TW)
	 * Danish 		(Denmark) (da-DK)
	 * English 		(United States) (en-US)
	 * Finnish 		(Finland) (fi-FI) 
	 * French 		(France) (fr-FR)
	 * Italian 		(Italy) (it-IT)
	 * Japanese 	(Japan) (ja-JP)
	 * Korean 		(Korea) (ko-KR)
	 * Dutch 		(Netherlands) (nl-NL)
	 * Norwegian 	(Norway) (nb-NO)
	 * Polish 		(Poland) (pl-PL)
	 * Portuguese 	(Brazil) (pt-BR)
	 * Russian 		(Russia) (ru-RU)
	 * Swedish 		(Sweden) (sv-SE)
	 * Chinese 		(People's Republic of China) (zh-CN)
	 * English 		(United Kingdom) (en-GB)
	 * Spanish 		(Mexico) (es-MX)
	 * Portuguese 	(Portugal) (pt-PT)
	 * Chinese 		(Hong Kong S.A.R.) (zh-HK)
	 * English 		(Australia) (en-AU)
	 * Spanish 		(Spain) (es-ES)
	 * French 		(Canada) (fr-CA)
	 * English 		(Canada) (en-CA)
	 * </pre>
	 */
	@Override
	public void setLocale(String locale) {
		freeformRecognizer.setLocale(locale);
	}

	@Override
	public void setChime(boolean isOn) {
		freeformRecognizer.setChime(isOn);
	}
	
	/**
	 * Set the Dialog enabled or disabled
	 */
	@Override
	public void setDialogEnabled(boolean enabled) {
		freeformRecognizer.setDialogEnabled(enabled);
	}
}
