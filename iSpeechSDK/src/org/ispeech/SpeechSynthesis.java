package org.ispeech;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.ispeech.SpeechSynthesisEvent.EventType;
import org.ispeech.core.InternalResources;
import org.ispeech.core.TTSEngine;
import org.ispeech.core.TTSEngine.ResponseHandler;
import org.ispeech.error.ApiException;
import org.ispeech.error.BusyException;
import org.ispeech.error.InvalidApiKeyException;
import org.ispeech.error.NoNetworkException;
import org.ispeech.marker.MarkerCallback;
import org.ispeech.marker.MarkerHolder;
import org.ispeech.marker.MarkerMovements;
import org.ispeech.tools.HttpUtils;
import org.ispeech.tools.Utilities;
import org.ispeech.viseme.MouthMovements;
import org.ispeech.viseme.VisemeCallback;
import org.ispeech.viseme.VisemeHolder;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Handler;

/**
 * Contains methods to convert text to audio and play through android's
 * {@link MediaPlayer}.
 * 
 */
public class SpeechSynthesis implements Synthesizer {
	private static final String TAG = "iSpeech SDK";

	private Context _context;
	private String apiKey;

	private TTSEngine ttsEngine;
	private AudioManager audioManager;
	private Thread ttsEngineWorkThread;
	private CallBackThread ttsCallbackThread;

	private MarkerCallback markerCallback = null;
	private VisemeCallback visemeCallback = null;
	private MarkerHolder markerHolder = null;
	private VisemeHolder visemeHolder = null;
	private boolean markerHolderReady = false;

	private String markerText;

	private Vector<SpeechSynthesisEvent> speechEventListeners = new Vector<SpeechSynthesisEvent>();

	/** Enforces singleton */
	private static SpeechSynthesis _synthesis = null;

	private OnCompletionListener onCompletion = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {

			audioManager.setStreamSolo(InternalResources.STREAM_TYPE, false); //unmute other audio sources

			createEvent(EventType.PLAY_SUCCESSFUL, null);
		}
	};

	public TTSEngine getTTSEngine() {
		return ttsEngine;
	}

	private OnErrorListener onError = new OnErrorListener() {

		@Override
		public boolean onError(MediaPlayer mp, final int what, final int extra) {

			audioManager.setStreamSolo(InternalResources.STREAM_TYPE, false); //unmute other audio sources
			createEvent(EventType.PLAY_FAILURE, new Exception("MediaPlayer Error: (" + what + "," + extra + ")"));
			return true;
		}
	};


	private SpeechSynthesis(Context context, String apiKey) throws InvalidApiKeyException {
		if (apiKey == null || apiKey.length() != 32) {
			throw new InvalidApiKeyException();
		}
		this.apiKey = apiKey;
		this._context = context;
		this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

		this.ttsEngine = TTSEngine.getInstance(InternalResources.getAPIUrl(context), apiKey);
		this.ttsEngine.setMeta(Utilities.getMetaInfo(context));
	}

	public static SpeechSynthesis getInstance(Context context, Activity callingActivity) throws InvalidApiKeyException {
		if (_synthesis == null) {
			_synthesis = new SpeechSynthesis(context, Utilities.getApiKey(context));
			callingActivity.setVolumeControlStream(InternalResources.STREAM_TYPE);
		} else {
			_synthesis._context = context;
		}

		return _synthesis;
	}

	/***
	 * Returns a VisemeHolder Object that contains all the information for a
	 * viseme animation for a given text.
	 * 
	 * @param text
	 *            text for viseme information
	 * @param voice
	 *            voice to be used for viseme information, ie usenglishfemale
	 * @param speed
	 *            speed to be used for viseme information ie 3
	 * @param format
	 *            format that speech spoken is downloaded as (mp3 or wav)
	 * @throws InvalidApiKeyException
	 */
	public VisemeHolder getVisemeInfo(String text, String voice, String speed, String format) throws InvalidApiKeyException {
		MouthMovements _voiceController;
		
		//download viseme data
		_voiceController = new MouthMovements(text, apiKey, voice, speed, format);
		VisemeHolder vHolder = _voiceController.getVisemeHolder();

		return vHolder;
	}

	public MarkerHolder getMarkerInfo(String text, String voice, String speed, String format) throws InvalidApiKeyException {

		MarkerMovements _markerController;
		
		//download marker data
		_markerController = new MarkerMovements(text, apiKey, voice, speed, format);
		MarkerHolder mHolder = _markerController.getMarkerHolder();

		return mHolder;
	}

	public static SpeechSynthesis getInstance(Activity callingActivity) throws InvalidApiKeyException {
		return getInstance(callingActivity.getApplicationContext(), callingActivity);
	}

	/**
	 * Sets streamType to be used by SDK. Default is AudioManager.STREAM_MUSIC.
	 * 
	 * @param streamType
	 *            sets stream type ie. AudioManager.STREAM_SYSTEM (int value 1)
	 *            or AudioManager.STREAM_MUSIC (int value 3)
	 */
	public void setStreamType(int streamType) {
		InternalResources.setStreamType(streamType);
	}

	/**
	 * Sets SDK so that TTS will always speak, regardless of whether the user
	 * has device on silent or not. Default is false.
	 * 
	 * @param alwaysSpeak
	 *            set to TRUE to have SDK always speak. set to FALSE to have SDK
	 *            stay silent if device is silent.
	 */
	public void setSpeakWhenSilent(boolean alwaysSpeak) {
		InternalResources.alwaysSpeak(alwaysSpeak);
	}

	public byte[] downloadByteArray(final String text) throws BusyException, NoNetworkException, IOException {
		return ttsEngine.downloadByteArray(_context, text);
	}

	/***
	 * Converts text to speech and begins playing it as soon as it is ready,
	 * method does not block.
	 * 
	 * @param text
	 *            The text to convert into audio.
	 * @throws BusyException
	 * @throws NoNetworkException
	 */
	@Override
	public void speak(final String text) throws BusyException, NoNetworkException {


		if (ttsEngine.isSpeaking()) {
			createEvent(EventType.PLAY_FAILURE, new BusyException("Device is busy?"));
			return;
		}

		if (text == null || text.length() == 0) {
			createEvent(EventType.PLAY_FAILURE, new IllegalArgumentException("Text is Empty."));
			return;
		}
		
		try {
			setupTTSEngine();

			new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... params) {
					ttsEngine.speak(_context, text);
					return null;
				}
			}.execute();

		} catch (Exception e) {
			e.printStackTrace();
			audioManager.setStreamSolo(InternalResources.STREAM_TYPE, false); // unmute all other audio sources
			createEvent(EventType.PLAY_FAILURE, e);
		}
	}

	private void setupTTSEngine() {
		ttsEngine.setResponseHandler(new ResponseHandler() {
			@Override
			public void onResponse(HttpResponse response) {
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_ACCEPTED) {
					// a failure occurred, but the server understood the request
					try {
						Map<String, String> result = HttpUtils.parseNameValuePairEntity(response.getEntity());
						throw new ApiException(result.get("message")); // throw the message as an exception
					} catch (final Exception e) {
						createEvent(EventType.PLAY_FAILURE, e);
					}
				}
			}
		});

		ttsEngine.setOnCompletionListener(onCompletion);
		ttsEngine.setOnErrorListener(onError);
		ttsEngine.setAudioStreamType(InternalResources.STREAM_TYPE);
		ttsEngine.setOnPreparedListener(new OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				mp.start();
				audioManager.setStreamSolo(InternalResources.STREAM_TYPE, true); // mute all other audio sources
				createEvent(EventType.PLAY_STARTED, null);
			}
		});
	}

	/***
	 * TODO :: this method needs to be renamed to
	 * registerSpeechSynthesisEventListener (or addSpeechSynthesisEventListener)
	 * has to correspond with
	 * {@link #unregisterSpeechSynthesisEventListener(SpeechSynthesisEvent)} See
	 * {@link SpeechSynthesisEvent}
	 */
	public void setSpeechSynthesisEvent(SpeechSynthesisEvent speechSynthesisEvent) {
		this.speechEventListeners.add(speechSynthesisEvent);
	}

	public void unregisterSpeechSynthesisEventListener(SpeechSynthesisEvent speechSynthesisEvent) {
		this.speechEventListeners.remove(speechSynthesisEvent);
	}

	/**
	 * Set the voice type
	 * 
	 * @param voiceType
	 */
	public void setVoiceType(String voiceType) {
		this.ttsEngine.setVoice(voiceType);
	}

	public int getCurrentPosition() {
		return ttsEngine.getCurrentPosition();
	}

	private void createEvent(final EventType type, final Object param) {
		Runnable run = new Runnable() {
			public void run() {
				if (speechEventListeners != null) {
					for (SpeechSynthesisEvent sse : speechEventListeners) {
						sse.stateChanged(type, param);
					}
				}

				if (markerCallback != null) {
					switch (type) {
					case PLAY_STARTED:
						ttsCallbackThread = new CallBackThread(markerText, CallBackThread.MARKER);
						markerCallback.onPlayStart();
						ttsCallbackThread.start();
						break;
					case PLAY_SUCCESSFUL:
						markerCallback.onPlaySuccessful();
						if (ttsCallbackThread != null)
							ttsCallbackThread.setThreadRunning(false);
						break;
					case PLAY_STOPPED:
						markerCallback.onPlayStopped();
						if (ttsCallbackThread != null)
							ttsCallbackThread.setThreadRunning(false);
						break;
					case PLAY_FAILURE:
						markerCallback.onPlayFailed((Exception) param);
						if (ttsCallbackThread != null)
							ttsCallbackThread.setThreadRunning(false);
						break;
					case PLAY_CANCELED:
						markerCallback.onPlayCanceled();
						if (ttsCallbackThread != null)
							ttsCallbackThread.setThreadRunning(false);
						break;
					}
				}
				if (visemeCallback != null) {
					switch (type) {
					case PLAY_STARTED:
						ttsCallbackThread = new CallBackThread(markerText, CallBackThread.VISEME);
						visemeCallback.onPlayStart();
						ttsCallbackThread.start();
						break;
					case PLAY_SUCCESSFUL:
						visemeCallback.onPlaySuccessful();
						if (ttsCallbackThread != null)
							ttsCallbackThread.setThreadRunning(false);
						break;
					case PLAY_STOPPED:
						visemeCallback.onPlayStopped();
						if (ttsCallbackThread != null)
							ttsCallbackThread.setThreadRunning(false);
						break;
					case PLAY_FAILURE:
						visemeCallback.onPlayFailed((Exception) param);
						if (ttsCallbackThread != null)
							ttsCallbackThread.setThreadRunning(false);
						break;
					case PLAY_CANCELED:
						visemeCallback.onPlayCanceled();
						if (ttsCallbackThread != null)
							ttsCallbackThread.setThreadRunning(false);
						break;
					}
				}
			}
		};
		Handler handler = new Handler(_context.getMainLooper());
		handler.post(run);
	};

	/**
	 * Stops the currently playing audio. This command is ignored if an
	 * advertisement is currently playing.
	 * 
	 * @see org.ispeech.Synthesizer#stop()
	 */
	@Override
	public void stop() {
		if (ttsEngine.isSpeaking()) {
			ttsEngine.stop();
			ttsEngine.setOnCompletionListener(null);
			createEvent(EventType.PLAY_STOPPED, null);
		}

		audioManager.setStreamSolo(InternalResources.STREAM_TYPE, false); //unmute all other audio sources that were possibly playing
	}

	public void cancel() {

		createEvent(EventType.PLAY_CANCELED, null);

		if (ttsEngine.isSpeaking()) {
			ttsEngine.cancelTTS();
		} else {
			if (ttsEngineWorkThread != null) {
				ttsEngineWorkThread.interrupt(); //send interrupt request to ttsEngineWorkThread, which breaks the wait for TTSEngine ready state and stops the TTSEngine
			}
		}

		if (markerCallback != null)
			markerCallback.onPlayCanceled();

	}

	/**
	 * Set connection timeout in milliseconds for HTTP connections
	 * 
	 * @param timeout
	 */
	public void setConnectionTimeout(int timeout) {
		if (ttsEngine != null)
			ttsEngine.setSocketTimeout(timeout);
	}

	/**
	 * Add meta value to synthesizer
	 * 
	 * @param type
	 * @param value
	 */
	@Override
	public void addMeta(MetaType type, String value) {
		ttsEngine.addMeta(type.getValue(), value);
	}

	@Override
	public void addOptionalCommand(String command, String parameter) {
		ttsEngine.addOptionalCommand(command, parameter);
	}

	@Override
	public void clearOptionalCommand() {
		ttsEngine.clearOptionalCommand();
	}

	public void addMarkerCallback(MarkerCallback mc) { // add a markerCallback
		markerCallback = mc;
	}

	public void addVisemeCallback(VisemeCallback mc) {
		visemeCallback = mc;
	}

	public void setUpMarkers(String ttsText) {
		markerText = ttsText;
		Object[] parameters = { this, ttsText }; //Prepare parameters for AsyncTask.

		AsyncTask<Object, Void, MarkerHolder> task = new AsyncTask<Object, Void, MarkerHolder>() {
			protected MarkerHolder doInBackground(Object... params) {
				try {
					MarkerHolder tempMarkerHolder;
					
					// download the marker info
					tempMarkerHolder = ((SpeechSynthesis) params[0]).getMarkerInfo(((String) params[1]), "usenglishfemale", "0", "mp3");
					return tempMarkerHolder;
				} catch (InvalidApiKeyException e) {
					createEvent(EventType.PLAY_FAILURE, e);
					return null;
				}
			}

			protected void onPostExecute(MarkerHolder tempMarkerHolder) {
				
				//callbacks
				markerHolder = tempMarkerHolder;
				markerHolderReady = true;
				if (markerCallback != null)
					markerCallback.onMarkerHolderReady();
			}
		};

		task.execute(parameters);
	}

	public void setUpVisemes(String ttsText) {
		markerText = ttsText;
		Object[] parameters = { this, ttsText }; //Prepare parameters for AsyncTask.

		AsyncTask<Object, Void, VisemeHolder> task = new AsyncTask<Object, Void, VisemeHolder>() {
			protected VisemeHolder doInBackground(Object... params) {
				try {
					VisemeHolder tempMarkerHolder;
					
					//download visemes
					tempMarkerHolder = ((SpeechSynthesis) params[0]).getVisemeInfo(((String) params[1]), "usenglishfemale", "0", "mp3");
					return tempMarkerHolder;
				} catch (InvalidApiKeyException e) {
					createEvent(EventType.PLAY_FAILURE, e);
					return null;
				}
			}

			protected void onPostExecute(VisemeHolder tempVisemeHolder) {
				visemeHolder = tempVisemeHolder;
				if (visemeCallback != null)
					visemeCallback.onVisemeHolderReady();
			}

		};

		task.execute(parameters);
	}

	public boolean getMarkerHolderReady() {
		return markerHolderReady;
	}

	public MarkerHolder getMarkerHolder() {
		return markerHolder;
	}

	private class CallBackThread extends Thread {

		boolean threadRunning = true;
		String text;
		int type;
		public static final int VISEME = 0;
		public static final int MARKER = 1;

		public CallBackThread(String text, int theType) {
			this.text = text;
			type = theType;
		}

		public void setThreadRunning(boolean thread) {
			threadRunning = thread;
		}

		public void run() {

			/* Checks to see if markers have been loaded */
			if (type == MARKER) {
				int words = markerHolder.getWords(); //Gets number of words in text
				final int threadTimeout = 5000;
				int timePassed = 300000;
				int timeNext = 0;
				int wordStartLoc = 0;
				int wordStopLoc = 0;
				float baseTime = 0;
				int deltaTime;

				try {
					timePassed = SpeechSynthesis.this.getCurrentPosition();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				while (timePassed >= 300000) { // this is so things don't go crazy if getCurrentPosition does something weird
					try {
						Thread.sleep(100);
						timePassed = SpeechSynthesis.this.getCurrentPosition();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				/* Loop to cycle through each word */
				for (int i = 0; i < words; i++) {
					String word = markerHolder.getText(i);
					wordStartLoc = text.indexOf(word, wordStopLoc);
					wordStopLoc = wordStartLoc + word.length();
					timeNext += markerHolder.getLength(i);

					if (!threadRunning)
						break;

					// throw in a callback
					markerCallback.onNewWord(wordStartLoc, wordStopLoc);

					try {
						timePassed = SpeechSynthesis.this.getCurrentPosition();
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}

					baseTime = System.nanoTime();
					// if the time passed has not exceeded the time for the next marker, stay here
					while (timePassed < timeNext) {
						try {
							timePassed = SpeechSynthesis.this.getCurrentPosition();
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						deltaTime = (int) ((System.nanoTime() - baseTime) / 1000000);
						if (deltaTime >= threadTimeout) {
							threadRunning = false;
						}
						if (!threadRunning)
							break;
					}
				}
			} else if (type == VISEME) {
				int words = visemeHolder.getFrames(); //Gets number of words in text
				final int threadTimeout = 5000;
				int timePassed = 300000;
				int timeNext = 0;
				float baseTime = 0;
				int deltaTime;

				try {
					timePassed = SpeechSynthesis.this.getCurrentPosition();
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				while (timePassed >= 300000) { // this is so things don't go crazy if getCurrentPosition does something weird
					try {
						Thread.sleep(100);
						timePassed = SpeechSynthesis.this.getCurrentPosition();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				/* Loop to cycle through each word */
				for (int i = 0; i < words; i++) {
					timeNext += visemeHolder.getLength(i);

					if (!threadRunning)
						break;

					// throw in a callback
					visemeCallback.onNewViseme(visemeHolder.getMouth(i));

					try {
						timePassed = SpeechSynthesis.this.getCurrentPosition();
					} catch (Exception e1) {
						e1.printStackTrace();
						return;
					}

					baseTime = System.nanoTime();
					// if the time passed has not exceeded the time for the next marker, stay here
					while (timePassed < timeNext) {
						try {
							timePassed = SpeechSynthesis.this.getCurrentPosition();
						} catch (Exception e1) {
							e1.printStackTrace();
							return;
						}
						deltaTime = (int) ((System.nanoTime() - baseTime) / 1000000);
						if (deltaTime >= threadTimeout) {
							threadRunning = false;
						}
						if (!threadRunning)
							break;
					}
				}
			}
		}
	}
}
