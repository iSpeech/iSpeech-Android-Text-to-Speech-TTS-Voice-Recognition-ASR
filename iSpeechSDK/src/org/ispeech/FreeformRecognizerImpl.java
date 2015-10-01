package org.ispeech;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.http.client.ClientProtocolException;
import org.ispeech.SpeechRecognizerEvent.EventType;
import org.ispeech.core.HttpRequestParams;
import org.ispeech.core.InternalResources;
import org.ispeech.core.RecDialog;
import org.ispeech.core.SilenceDetection;
import org.ispeech.core.SoundBox;
import org.ispeech.error.ApiException;
import org.ispeech.error.BusyException;
import org.ispeech.error.InvalidApiKeyException;
import org.ispeech.error.NoNetworkException;
import org.ispeech.speex.FrequencyBand;
import org.ispeech.speex.SpeexEncoder;
import org.ispeech.tools.SerializableHashTable;
import org.ispeech.tools.Utilities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Message;

/**
 * FreeformRecognizerImpl
 * 
 * @hide
 */
class FreeformRecognizerImpl implements Recognizer {
	private static final String TAG = "iSpeech SDK";

	private static FreeformRecognizerImpl _instance = null;
	
	private boolean DIALOG_ENABLED=true;

	// default 8000 frequency settings
	private int FREQUENCY = 16000;
	private int MIN_RECORD_BUFF_SIZE = 640;
	private int SPEEX_MODE = 2;
	private static final String ASR_DOMAIN = "api.ispeech.org";
	
	private String _apiKey;
	private FreeformType freeFormValue = FreeformType.FREEFORM_SMS;
	long silenceStart =-1;

	private SpeechRecognizerEvent recognizerEventListener = null;
	private DialogHandler alertDialogHandler;
	private TimerTask timeoutTask;
	private long timeout = 60000;
	private int socketTimeout = 60000;
	
	private SerializableHashTable meta;
	private Map<String, String> optionalCommands = new Hashtable<String, String>();
	
	private Map<String, List<String>> aliasList = new HashMap<String, List<String>>();
	private Vector<String> phraseList = new Vector<String>();

	private boolean soundTone = true;
	private int silenceTime;
	private long lastTime = 0;
	private static final int SILENCE_TIMEOUT = 1000;
	private static final int MIN_REC = 2000;
	private long recStart = 0;

	int voiceCount = 0;

	private Context _context;

	private AudioManager audioManager;

	private AudioRecord audioRecorder;

	private VoiceDataTransporter voiceDataTransporter;
	
	private boolean silenceDetectOn;

	private void createEventThread(final EventType type, final Object param) {
		Handler handler = new Handler(_context.getMainLooper());

		Runnable run = new Runnable() {
			public void run() {
				if (recognizerEventListener != null)
					recognizerEventListener.stateChanged(type, param);
			}
		};

		handler.post(run); // run the callback on the UI (main) thread
	};

	/**
	 * 
	 * Constructor (private to enforce singleton pattern)
	 * 
	 * @param context
	 * @param apiKey
	 * @throws InvalidApiKeyException
	 */
	private FreeformRecognizerImpl(Context context) throws InvalidApiKeyException {
		String apiKey = Utilities.getApiKey(context);

		if (apiKey == null || apiKey.length() != 32) {
			throw new InvalidApiKeyException();
		}

		this._apiKey = apiKey;
		this._context = context.getApplicationContext();
		this.meta = Utilities.getMetaInfo(context);
		SoundBox.getInstance(context);

		//Initialize system audio manager
		audioManager = (AudioManager) _context.getSystemService(Context.AUDIO_SERVICE);

	}
	
	@Override
	public void addAlias(String name, String[] values) {
		if (!aliasList.containsKey(name.toUpperCase(Locale.US)))
			aliasList.put(name.toUpperCase(Locale.US), new ArrayList<String>());
		
		for (String value : values)
			aliasList.get(name).add(value);
	}
	
	public void addCommand(String phrase) {
	if (!phraseList.contains(phrase))
		phraseList.add(phrase);
	}
	
	@Override
	public void addCommand(String[] phrases) {
		if (phrases != null) {
			for (String s : phrases) {
				this.addCommand(s);
			}
		}
	}

	public static FreeformRecognizerImpl getInstance(Context context) throws InvalidApiKeyException {
		if (_instance == null) {
			_instance = new FreeformRecognizerImpl(context);
		}
		return _instance;
	}
	
	public static FreeformRecognizerImpl getInstance(Context context, String apiKey) throws InvalidApiKeyException {
		if (_instance == null) {
			_instance = new FreeformRecognizerImpl(context);
		}
		return _instance;
	}

	public void setSilenceDetection(boolean newDetection){
		silenceDetectOn=newDetection;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	@Override
	public void addMeta(MetaType type, String value) {
		meta.put(type.getValue(), value);
	}

	@Override
	public void addOptionalCommand(String command, String parameter) {
		optionalCommands.put(command, parameter);
	}
	private void removeOptionalCommand(String command)
	{
		if(optionalCommands.containsKey(command))
			optionalCommands.remove(command);
	}

	@Override
	public void clearOptionalCommand() {
		optionalCommands.clear();
	}

	@Override
	public void setFreeForm(FreeformType freeFormType) {
		freeFormValue = freeFormType;
	}

	@Override
	public void clearCommand() {
		phraseList.clear();
		clearAlias();
	}

	@Override
	public void stopRecord() {
		if (timeoutTask != null) {
			timeoutTask.cancel();
		}

		if(voiceDataTransporter != null)
			voiceDataTransporter.stopRecording();
	}
	
	void stopTimeoutTask()
	{
		if (timeoutTask != null) {
			timeoutTask.cancel();
		}
	}

	@Override
	public void cancelRecord() {
		stopRecord();
		voiceDataTransporter.cancelRecording();

		voiceDataTransporter.interrupt(); // Send termination signal to data collector thread

		createEventThread(EventType.RECORDING_CANCELED, null);
	}

	@Override
	public void cancelProcessing() {

		if (voiceDataTransporter != null) {
			voiceDataTransporter.interrupt(); // Send termination signal to data transporter thread
		}

		//send notification event that the RECOGNITION has been canceled
		createEventThread(EventType.RECOGNITION_CANCELED, null);
	}

	private void setupProcessingDialogHandler(RecDialog dialog) {
		alertDialogHandler = new DialogHandler(dialog);
	}
	
	private static class DialogHandler extends Handler {
		private RecDialog dialog;
		
		public DialogHandler(RecDialog dialog) {
			super(dialog.getContext().getMainLooper());
			this.dialog = dialog;
		}
		
		@Override
		public void handleMessage(Message msg) {

			if (msg.what == RecDialog.DISMISS_DIALOG) { // turn on
				dialog.clearLayout();
				dialog.dismiss();
			} else if (msg.what == RecDialog.SHOW_DIALOG) {
				dialog.show();
			} else if (msg.what == RecDialog.SET_PROCESSING) {
				dialog.setRecognizing();
			} else if (msg.what == RecDialog.UPDATE_MICROPHONE_LEVEL) {
				dialog.setMicrophoneLevel(msg.arg1);
			} else if(msg.what == RecDialog.SET_LISTENING) {
				dialog.startDraw();
			}
		}
	}

	private void setupRecordingTimeoutTask() {
		timeoutTask = new TimerTask() {
			@Override
			public void run() {
				if (audioRecorder!=null && audioRecorder.getState() == AudioRecord.STATE_INITIALIZED && audioRecorder.getState() == AudioRecord.RECORDSTATE_RECORDING) {
					stopRecord();
				}
			}
		};

		new Timer().schedule(timeoutTask, timeout);
	}

	private class VoiceDataTransporter extends Thread {
		
		private Socket serverConnection;
		private OutputStream toServer = null;
		private InputStream fromServer = null;
		private ByteArrayOutputStream audioBuffer = new ByteArrayOutputStream();
		private final int AUDIO_PACKET_SIZE = 1000;
		
		private boolean isStopped = false;
		private boolean isCanceled = false;
		
		public VoiceDataTransporter() {
			super("VoiceDataTransporter");
		}
		
		private void commit() throws IOException {
			sendPacket(audioBuffer.toByteArray(), false);
			audioBuffer.reset();
		}
		
		private synchronized void write(byte[] data, int offset, int length) throws IOException {
			audioBuffer.write(data, offset, length);
			if(audioBuffer.size() >= AUDIO_PACKET_SIZE) {
				commit();
			}
		}
		
		private void stopStreaming() throws IOException {
			commit();
			audioBuffer.close();
		}
		
		public boolean isRecording() {
			return !isStopped;
		}

		public void stopRecording() {
			if(audioRecorder != null && audioRecorder.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
				audioRecorder.stop();
		}

		public void cancelRecording() {
			stopRecording();
			isStopped = true;
			isCanceled = true;
		}

		public boolean isRecordingCanceled() {
			return isCanceled;
		}
		
		private synchronized void sendFirstPacket() throws IOException {
			if(toServer == null)
				return;
			
			String requestHeader = "POST /api/rest?" + buildURL() + " HTTP/1.0\r\n";
			requestHeader += "Host:" + ASR_DOMAIN + "\r\n";
			requestHeader += "X-Stream: http\r\n";
			requestHeader += "Content-Length: 10000000\r\n";
			requestHeader += "Content-Type: audio/speex\r\n";
			requestHeader += "\r\n";
			
			toServer.write(requestHeader.getBytes("utf8"));
		}
		
		private synchronized void sendPacket(byte[] postData, boolean firstPacket) throws UnsupportedEncodingException, IOException {
			if(toServer == null)
				return;
			toServer.write(intToByteArray(postData.length));
			toServer.write(postData);
		}
		
		private void sendLastASRPacket() throws IOException {
			toServer.write(intToByteArray(0)); // int 0 to kill
			toServer.flush();
		}
		
		private byte[] intToByteArray(int number) {
			byte[] ret = new byte[4];
			
			ret[0] = (byte) ((number >> 24) & 0xFF);
		    ret[1] = (byte) ((number >> 16) & 0xFF);
		    ret[2] = (byte) ((number >> 8) & 0xFF);  
		    ret[3] = (byte) (number & 0xFF);
		    
		    return ret;
		}

		private SerializableHashTable getASRResponse() throws IOException {
			
			byte[] buff = new byte[4096];
			
			int endHeader, bytesRead = 0, pos = 0;
			
			while(bytesRead != -1) { // read the response into buff
				
				bytesRead = fromServer.read(buff, pos, buff.length-pos);
				
				pos += bytesRead;
			}
			
			endHeader = getEndPatternPos(buff, 0, pos); // find the end of the headers
			
			int contentLength = getContentLength(buff, endHeader);
			
			byte[] retBuff = copy(buff, endHeader, endHeader+contentLength);
			
			//deserialize the response
			return SerializableHashTable.deserialize(retBuff);
		}
		
		@SuppressLint("NewApi")
		private byte[] copy(byte[] src, int start, int end) {
			
			if(Build.VERSION.SDK_INT >= 9) // this might be faster, but it's not available below android 9
				return Arrays.copyOfRange(src, start, end);
			
			// so for those old phones we copy the bytes manually :(
			byte[] ret = new byte[end-start];
			
			for(int i = 0; i < end-start; i++) {
				ret[i] = src[start+i];
			}
			
			return ret;
		}
		
		private int getEndPatternPos(byte[] buff, int start, int length) {
			
			int state = 0;
			
			// search for the pattern \r\n\r\n (end of http headers)
			for(int i = start; i < start+length && i < buff.length; i++) {
				
				if((state == 0 || state == 2) && buff[i] == '\r') {
					state++;
				} else if(state == 1 && buff[i] == '\n') {
					state++;
				} else if(state == 3 && buff[i] == '\n') {
					return i+1;
				} else {
					state = 0;
				}
			}
			
			return -1;
		}
		
		private int getContentLength(byte[] buff, int length) {
			
			// search for the Content-Length: header and return the value
			
			byte[] pattern = new byte[] {'C', 'o', 'n', 't', 'e', 'n', 't', '-', 'L', 'e', 'n', 'g', 't', 'h', ':', ' '};
			int stateIndex = 0;

			int start = -1, end;
			
			
			for(int i = 0; i < length; i++) {

				if(stateIndex == pattern.length) {
					
					if(start == -1)
						start = i;
					
					if(buff[i] == '\r') {
						end = i;
						return Integer.parseInt(new String(buff, start, end-start));
					}
					
				} else if(buff[i] == pattern[stateIndex]) {
					stateIndex++;
				} else {
					stateIndex = 0;
				}
			}
			
			
			return -1;
		}
		
		private void startAudioRecorder() {
			audioManager.setStreamSolo(InternalResources.STREAM_TYPE, true);
			MIN_RECORD_BUFF_SIZE = AudioRecord.getMinBufferSize(FREQUENCY, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
			audioRecorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, FREQUENCY, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, MIN_RECORD_BUFF_SIZE);
			audioRecorder.startRecording();
			recStart = System.currentTimeMillis();
		}
	 	
		private short[] convertBytesToShortsArray(byte [] data) {    	
			short[] shorts = new short[data.length/2];
			
			// to turn bytes to shorts as either big endian or little endian. 
			ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
		  	
			return shorts;
		}
		
		public void run() {
			try {

				// play a sound
				if (soundTone) {
					SoundBox.getInstance(_context).playOpen();
					SoundBox.getInstance(_context).blockTillDone();
				}
				
				// now we're listening
				startAudioRecorder();
				
				if(DIALOG_ENABLED)
					alertDialogHandler.sendEmptyMessage(RecDialog.SET_LISTENING);
				
				connectToHost();
				
				if (isInterrupted()) {
					audioRecorder.stop();
					audioRecorder.release();
					return;
				}
				
				SerializableHashTable response;
				
				SpeexEncoder encoder = new SpeexEncoder(FrequencyBand.WIDE_BAND, 9);
				
				SilenceDetection silenceDetection = new SilenceDetection();
				
				byte[] rawAudioBuffer = new byte[encoder.getFrameSize()*2];
				byte[] speexData;
				int bytesRead = 1;
				int totalBytes = 0;
				
				outerLoop : while(!isStopped) {
					
					// take that sound and fill the frame with it (the encoder needs a full frame)
					while(totalBytes < rawAudioBuffer.length) {
						bytesRead = audioRecorder.read(rawAudioBuffer, totalBytes, rawAudioBuffer.length-totalBytes);
						
						if(bytesRead <= 0) {
							isStopped = true;
							break outerLoop;
						}
						
						totalBytes += bytesRead;
					}
					totalBytes = 0;
					
					// feed data to the silence detection
					int energy = silenceDetection.addSound(rawAudioBuffer);
					
					if(DIALOG_ENABLED) {
						Message m = Message.obtain();
						m.what = RecDialog.UPDATE_MICROPHONE_LEVEL;
						m.arg1 = energy;
						alertDialogHandler.sendMessage(m);
					}
					
					//SPEEX START
					short[] rawShorts = convertBytesToShortsArray(rawAudioBuffer);
					speexData = encoder.encode(rawShorts);
					write(speexData, 0, speexData.length);
					//SPEEX END
					
					//silence detection here
					if(silenceDetectOn) {
						if(lastTime == 0) lastTime = System.currentTimeMillis();
						
						boolean isSilence = silenceDetection.isSilence() && (System.currentTimeMillis() - recStart > MIN_REC);
						if(isSilence && (silenceTime > SILENCE_TIMEOUT)) {
							stopRecord();
						} else if(isSilence) {
							silenceTime += System.currentTimeMillis() - lastTime;
						} else {
							silenceTime = 0;
						}
						
						lastTime = System.currentTimeMillis();
					}
				}
				
				audioRecorder.stop();
				audioRecorder.release();
				stopStreaming();
				sendLastASRPacket();
				
				if (soundTone) {
					SoundBox.getInstance(_context).playClose();
				}
				
				if(DIALOG_ENABLED)
					alertDialogHandler.sendEmptyMessage(RecDialog.SET_PROCESSING);
				
				
				createEventThread(EventType.RECORDING_COMPLETE, null);
				
				response = getASRResponse();
				
				parseResults(response);
				
			} catch (ClientProtocolException e) {
				e.printStackTrace();
				createEventThread(EventType.ERROR, e);
			} catch (IOException e) {
				e.printStackTrace();
				createEventThread(EventType.ERROR, e);
			} catch (ApiException e) {
				e.printStackTrace();
				createEventThread(EventType.ERROR, e);
			} catch (InvalidApiKeyException e) {
				e.printStackTrace();
			}

			finally {
				audioManager.setStreamSolo(InternalResources.STREAM_TYPE, false);
				timeoutTask.cancel();
				voiceCount = 0;
			}
		}
		
		private void connectToHost() throws UnsupportedEncodingException, IOException, ApiException {
			serverConnection = new Socket();
			serverConnection.setSoTimeout(socketTimeout);
			serverConnection.connect(new InetSocketAddress(ASR_DOMAIN, 80));
			
			if(serverConnection.isConnected()) {
				
				toServer = serverConnection.getOutputStream();
				fromServer = serverConnection.getInputStream();
				sendFirstPacket();
				
			} else
				throw new ApiException("Could not connect to host");
			
		}

		private void parseResults(SerializableHashTable results) throws InvalidApiKeyException, IOException, ApiException {
			SpeechResult asrResult = null;

			//pull the text and confidence out of the results
			if (results.containsKey("result") && results.getString("result").equals("success")) {
				asrResult = new SpeechResult(results.getString("text"), Float.parseFloat(results.getString("confidence")));
				
			} else if (results.containsKey("result") && results.getString("result").equals("error")) {
				
				// asr failed, get the result code and throw an exception
				if (results.containsKey("code")) {
					int code = Integer.parseInt(results.getString("code"));
					switch (code) {
					case 1:
						if(DIALOG_ENABLED)
							alertDialogHandler.sendEmptyMessage(RecDialog.DISMISS_DIALOG);
						break;
					case 3:
						break;
					case 101:
						break;
					case 999:
						throw new InvalidApiKeyException();
					default:
						throw new IOException();
					}
				}
				throw new ApiException(results.getString("message"));
			}


			if (isInterrupted() || voiceDataTransporter.isRecordingCanceled()) { //if the recording wasn't cancelled, post RECOGINITION_COMPLETE event
				return;
			} else {
				createEventThread(EventType.RECOGNITION_COMPLETE, asrResult);
				if(DIALOG_ENABLED)
					alertDialogHandler.sendEmptyMessage(RecDialog.DISMISS_DIALOG);
			}
		}
		
		private String buildURL() throws UnsupportedEncodingException {
			String ret =  "apikey=" + _apiKey;
	        ret += "&action=recognize";
	        ret += "&speexmode=" + SPEEX_MODE;
	        ret += "&content-type=speex";
	        ret += "&freeform=1";
	        ret += "&deviceType=Android";
	        ret += "&output=hash";
	        try {
				ret += "&" + HttpRequestParams.META + "=" + new String(Utilities.encodeBase64(meta.serialize()));
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
	        if(!optionalCommands.containsKey("locale")) ret += "&locale=en-us";
	        
	        
	        ArrayList<String> alias = new ArrayList<String>();
	        
	        int limit = phraseList.size(); // add commands
	        for(int x = 0; x < limit; x++) {
	        	ret += "&command" + (x+1) + "=" + URLEncoder.encode(phraseList.get(x), "UTF-8");
	        	alias.add("command" + (x+1));
	        }
	        
	        if(aliasList.size() > 0) {
	        	Iterator<Entry<String, List<String>>> it = aliasList.entrySet().iterator();
	        	while (it.hasNext()) { // add aliases
	        		Entry<String, List<String>> pairs = it.next();
	        		
	        		ret += "&" + pairs.getKey().toUpperCase(Locale.US) + "=" + pipeSeparateArray(pairs.getValue().toArray(new String[pairs.getValue().size()]));
	        		
	        		alias.add(pairs.getKey().toUpperCase(Locale.US));
	        		
	        		it.remove(); // avoids a ConcurrentModificationException
	        	}
	        }
	        
	        if(alias.size() > 0) ret += "&alias=" + pipeSeparateArray( alias.toArray(new String[alias.size()])); // add the names of everything we added
	        
	        for(String key : optionalCommands.keySet()) {
	        	String value = optionalCommands.get(key);
	        	
	        	ret+="&" + URLEncoder.encode(key, "UTF-8")+"="+URLEncoder.encode(value, "UTF-8");
	        }
	        
	        return ret;
		}
		
		/**
		 * returns pipe seaprated string of an array of strings handed to it
		 * @param arr
		 * @return
		 * @throws UnsupportedEncodingException
		 */
		private String pipeSeparateArray(String[] arr) throws UnsupportedEncodingException {
			String ret = URLEncoder.encode(arr[0], "UTF-8");
			
			int limit = arr.length;
			
			for(int x = 1; x < limit; x++) {
				ret += "|" + URLEncoder.encode(arr[x], "UTF-8");
			}
			
			return ret;
		}
	}
	
	@Override
	public synchronized void startRecord(Activity act, SpeechRecognizerEvent speechRecognizerEvent) throws BusyException, NoNetworkException {

		if (!Utilities.isNetworkAvailable(_context)) {
			throw new NoNetworkException("Network is not available.");
		}


		
		this.recognizerEventListener = speechRecognizerEvent;

		// stop if we are already recording
		if (voiceDataTransporter != null && voiceDataTransporter.isRecording()) { // 
			createEventThread(EventType.ERROR, new IllegalStateException("Device is busy?"));

			voiceDataTransporter.cancelRecording();
			voiceDataTransporter.interrupt();
			return;
		}

		silenceStart=-1; // reset silence

		if(DIALOG_ENABLED) { // set up the dialog
			setupProcessingDialogHandler(new RecDialog(act));
		}

		setupRecordingTimeoutTask();
		
		// start the transport
		voiceDataTransporter = new VoiceDataTransporter();
		voiceDataTransporter.start();
		
		// start the dialog
		if(DIALOG_ENABLED)
			alertDialogHandler.sendEmptyMessage(RecDialog.SHOW_DIALOG);

	}

	@Override
	public FreeformType getFreeForm() {
		return freeFormValue;
	}

	@Override
	public boolean isRunning() {
		//TODO :: this is WRONG... But since I do not know why would we have this when we have callbacks, I will just leave it as is.
		//This can be probably just removed from the API
		
		if(voiceDataTransporter != null && voiceDataTransporter.isAlive())
			return true;
		else
			return false;
	}

	@Override
	public void clearAlias() {
		aliasList.clear();
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public void enableSoundTone() {
		soundTone = true;
	}

	public void disableSoundTone() {
		soundTone = false;
	}

	@Override
	public void setModel(String model) {
		if(model!=null)
			this.addOptionalCommand("model",model);
		else
			this.removeOptionalCommand("model");
	}

	@Override
	public void setLocale(String locale) {
		if(locale!=null)
			this.addOptionalCommand("locale",locale);
		else
			this.removeOptionalCommand("locale");
	}

	@Override
	public void setChime(boolean isOn) {
		if(isOn)
			enableSoundTone();
		else
			disableSoundTone();
	}
	
	@Override
	public void setDialogEnabled(boolean enabled) {
		DIALOG_ENABLED = enabled;
	}
}
