package org.ispeech.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.ispeech.tools.HttpUtils;
import org.ispeech.tools.SerializableHashTable;
import org.ispeech.tools.Utilities;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;

public class TTSEngine {
	protected static final String TAG = "iSpeech SDK";
	
	private String apikey;
	private String url;
	private static MediaPlayer mediaPlayer;
	private boolean isRunning;
	private String voiceType;
	private int streamType = InternalResources.STREAM_TYPE;
	private boolean changeVolume=false;
	private int originalVolume;

	private volatile static TTSEngine _instance;
	private ResponseHandler handler;
	private SerializableHashTable meta = new SerializableHashTable();
	private Map<String, String> optional = new HashMap<String, String>();

	/** Default socket timeout 60 seconds. Can be changed to another value by SDK user */
	private int socketTimeout = 60 * 1000; 

	public interface ResponseHandler {
		public void onResponse(HttpResponse response);
	};

	public static TTSEngine getInstance(String url, String apikey) {
		if (_instance == null) {
			_instance = new TTSEngine(url, apikey);
		}
		if (mediaPlayer == null) {
			mediaPlayer = new MediaPlayer();
		}
		return _instance;
	}
	
	public void setVoice(String voice) {
		voiceType = voice;
	}
	
	public void setAudioStreamType(int streamtype) {
		streamType = streamtype;
	}

	public void setMeta(SerializableHashTable m) {
		meta = m;
	}

	public void addMeta(String key, String value) {
		meta.put(key, value);
	}

	public void setResponseHandler(ResponseHandler handler) {
		this.handler = handler;
	}

	private TTSEngine(String url, String apikey) {
		this.apikey = apikey;
		this.url = url;
	}

	public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener) {
		mediaPlayer.setOnCompletionListener(listener);
	}

	public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
		mediaPlayer.setOnPreparedListener(listener);
	}
	
	public int getPosition(){
		return mediaPlayer.getCurrentPosition();
	}

	public boolean isSpeaking() {
		synchronized (_instance) {
			if (mediaPlayer != null)
				return mediaPlayer.isPlaying();
			else{
				return false;
			}
		}
	}
	
	public boolean isRunning(){
		return isRunning;
	}

	public void stop() {
		synchronized (_instance) {
			if (mediaPlayer.isPlaying()) {
				mediaPlayer.stop();
			}
		}
	}

	public void release() {
		synchronized (_instance) {
			if (!mediaPlayer.isPlaying()) {
				mediaPlayer.release();
				mediaPlayer = new MediaPlayer();
			}
			isRunning = false;
		}
	}
	
	public byte[] downloadByteArray(Context context, String text) throws IOException {
		
		// build the url
		List<NameValuePair> nameValuePairs = new CopyOnWriteArrayList<NameValuePair>();
		nameValuePairs.add(new BasicNameValuePair(HttpRequestParams.APIKEY, apikey));
		nameValuePairs.add(new BasicNameValuePair(HttpRequestParams.TEXT, text));
		nameValuePairs.add(new BasicNameValuePair(HttpRequestParams.DEVICE, HttpRequestParams.DEVICE_ANDROID));
		nameValuePairs.add(new BasicNameValuePair(HttpRequestParams.ACTION, HttpRequestParams.ACTION_CONVERT));
		if (voiceType != null)
			nameValuePairs.add(new BasicNameValuePair(HttpRequestParams.VOICE, voiceType));

		if (meta != null)
			nameValuePairs.add(new BasicNameValuePair(HttpRequestParams.META, new String(Utilities.encodeBase64(meta.serialize()))));

		if (optional != null && optional.size() > 0) {
			for (String command : optional.keySet()) {
				nameValuePairs.add(new BasicNameValuePair(command, optional.get(command)));
			}
		}

		// make a url
		String urlStr = HttpUtils.addParamsToUrl(url, nameValuePairs);

		// connect to the url
		HttpClient client = new DefaultHttpClient();
		client.getParams().setParameter(HttpRequestParams.SOCKET_TIMEOUT, Integer.valueOf(socketTimeout));
		HttpResponse response = client.execute(new HttpGet(urlStr));

		if (handler != null) {
			handler.onResponse(response);
		}

		// if it's audio
		if (response.getEntity().getContentType().getValue().contains("audio")) {
			ByteArrayOutputStream ret = new ByteArrayOutputStream();
			
			// write everything to a ByteArrayOutputStream
			response.getEntity().writeTo(ret);
			
			return ret.toByteArray(); // and return that
		}
		
		return null;
	}

	public void speak(Context context, String text) {
		isRunning = true;
		final AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		int streamVolumeCurrent = mAudioManager.getStreamVolume(InternalResources.STREAM_TYPE);
		
		// make sure we can speak even if we are silent if that setting is set
		if (InternalResources.ALWAYS_SPEAK==true) {
			if (streamVolumeCurrent <= 0){
				changeVolume=true;
				mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
				originalVolume = mAudioManager.getStreamVolume(InternalResources.STREAM_TYPE);
				int maxVolume = mAudioManager.getStreamMaxVolume(InternalResources.STREAM_TYPE);
				mAudioManager.setStreamVolume(InternalResources.STREAM_TYPE, (maxVolume/2), AudioManager.FLAG_SHOW_UI);
			}
		}
		
		try { // connect to the server
			List<NameValuePair> nameValuePairs = new CopyOnWriteArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair(HttpRequestParams.APIKEY, apikey)); // set the headers
			nameValuePairs.add(new BasicNameValuePair(HttpRequestParams.TEXT, text));
			nameValuePairs.add(new BasicNameValuePair(HttpRequestParams.DEVICE, HttpRequestParams.DEVICE_ANDROID));
			nameValuePairs.add(new BasicNameValuePair(HttpRequestParams.ACTION, HttpRequestParams.ACTION_CONVERT));
			
			if (voiceType != null)
				nameValuePairs.add(new BasicNameValuePair(HttpRequestParams.VOICE, voiceType));

			if (meta != null)
				nameValuePairs.add(new BasicNameValuePair(HttpRequestParams.META, new String(Utilities.encodeBase64(meta.serialize()))));

			if (optional != null && optional.size() > 0) {
				for (String command : optional.keySet()) {
					nameValuePairs.add(new BasicNameValuePair(command, optional.get(command)));
				}
			}

			// get the url string
			String urlStr = HttpUtils.addParamsToUrl(url, nameValuePairs);
			
			// start up the media player
			synchronized (_instance) {
				mediaPlayer.reset();
				mediaPlayer.setDataSource(urlStr);
				mediaPlayer.setAudioStreamType(streamType);
				mediaPlayer.prepare();
			}
			
			if(changeVolume)
				mAudioManager.setStreamVolume(InternalResources.STREAM_TYPE, originalVolume, AudioManager.FLAG_SHOW_UI);
			
			mediaPlayer.start();

			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			isRunning = false;
		}
	}

	public void setOnErrorListener(OnErrorListener onError) {
		mediaPlayer.setOnErrorListener(onError);
	}

	public int getCurrentPosition() {
		return mediaPlayer.getCurrentPosition();
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public void addOptionalCommand(String command, String parameter) {
		optional.put(command, parameter);
	}

	public void clearOptionalCommand() {
		optional.clear();
	}

	public void cancelTTS() {
		mediaPlayer.stop();		
	}
}
