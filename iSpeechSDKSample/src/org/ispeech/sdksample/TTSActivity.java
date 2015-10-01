package org.ispeech.sdksample;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;

import org.ispeech.MarkerHolder;
import org.ispeech.SpeechSynthesis;
import org.ispeech.SpeechSynthesisEvent;
import org.ispeech.VisemeHolder;
import org.ispeech.error.BusyException;
import org.ispeech.error.InvalidApiKeyException;
import org.ispeech.error.NoNetworkException;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 
 *
 */
public class TTSActivity extends Activity {

	private static final String TAG = "iSpeech SDK Sample";
	SpeechSynthesis synthesis;
	Context _context;


	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		_context = this.getApplicationContext();

		setContentView(R.layout.tts);

		((EditText) findViewById(R.id.text)).setText(R.string.tts_sample_text);
		findViewById(R.id.speak).setOnClickListener(new OnSpeakListener());
		findViewById(R.id.stop).setOnClickListener(new OnStopListener());

		
		
		prepareTTSEngine();
		
		synthesis.setStreamType(AudioManager.STREAM_MUSIC); 
	}


	private void prepareTTSEngine() {
		try {
			synthesis = SpeechSynthesis.getInstance(this);
			synthesis.setSpeechSynthesisEvent(new SpeechSynthesisEvent() {

				public void onPlaySuccessful() {
					Log.i(TAG, "onPlaySuccessful");
				}

				public void onPlayStopped() {
					Log.i(TAG, "onPlayStopped");
				}

				public void onPlayFailed(Exception e) {
					Log.e(TAG, "onPlayFailed");
					

					AlertDialog.Builder builder = new AlertDialog.Builder(TTSActivity.this);
					builder.setMessage("Error[TTSActivity]: " + e.toString())
					       .setCancelable(false)
					       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
					           public void onClick(DialogInterface dialog, int id) {
					           }
					       });
					AlertDialog alert = builder.create();
					alert.show();
				}

				public void onPlayStart() {
					Log.i(TAG, "onPlayStart");
				}

				@Override
				public void onPlayCanceled() {
					Log.i(TAG, "onPlayCanceled");
				}
				
				
			});


		} catch (InvalidApiKeyException e) {
			Log.e(TAG, "Invalid API key\n" + e.getStackTrace());
			Toast.makeText(_context, "ERROR: Invalid API key", Toast.LENGTH_LONG).show();
		}

	}


	private class OnSpeakListener implements OnClickListener {

		public void onClick(View v) {
			
//			try {
//				String ttsText = ((EditText) findViewById(R.id.text)).getText().toString();
//				byte [] b = synthesis.downloadByteArray(ttsText);
//				
//				if (b!=null){
//					Log.d("DEBUG", "SUCESSSSSSSS!!!!!");
//					MediaPlayer mediaPlayer;
//					mediaPlayer = new MediaPlayer();
//					
//					File tempMp3 = File.createTempFile("test", ".mp3", getCacheDir());
//	                FileOutputStream fos = new FileOutputStream(tempMp3);
//	                fos.write(b);
//	                fos.close();
//
//	                mediaPlayer = MediaPlayer.create(getApplicationContext(),Uri.fromFile(tempMp3));
//	                mediaPlayer.start();
//					
//					
////					mediaPlayer.setDataSource();
//				}else{
//					Log.d("DEBUG", "FAILURE :( ");
//				}
//
//			} catch (BusyException e) {
//				Log.e(TAG, "SDK is busy");
//				e.printStackTrace();
//				Toast.makeText(_context, "ERROR: SDK is busy", Toast.LENGTH_LONG).show();
//			} catch (NoNetworkException e) {
//				Log.e(TAG, "Network is not available\n" + e.getStackTrace());
//				Toast.makeText(_context, "ERROR: Network is not available", Toast.LENGTH_LONG).show();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			try {
				String ttsText = ((EditText) findViewById(R.id.text)).getText().toString();
				
				synthesis.speak(ttsText);

			} catch (BusyException e) {
				Log.e(TAG, "SDK is busy");
				e.printStackTrace();
				Toast.makeText(_context, "ERROR: SDK is busy", Toast.LENGTH_LONG).show();
			} catch (NoNetworkException e) {
				Log.e(TAG, "Network is not available\n" + e.getStackTrace());
				Toast.makeText(_context, "ERROR: Network is not available", Toast.LENGTH_LONG).show();
			}
		}
	}


	
	public class OnStopListener implements OnClickListener {

		public void onClick(View v) {
			if (synthesis != null) {
				synthesis.stop();
			}

//			VisemeHolder vh;
//			final String TAG = "VISEME INFO";
//
//			try {
//				vh = synthesis.getVisemeInfo("Hello World", "usenglishfemale", "0", "mp3");
//				int frames = vh.getFrames();
//				Log.d("DEBUG", "FRAMES:" + frames);
//				Log.d("DEBUG", "TOTAL LENGTH (in ms):" + vh.getTotalLength());
//				for (int i = 0; i<frames; i++){
//					
//					Log.d(TAG, "Start (in ms):" + vh.getStart(i));
//					Log.d(TAG, "End (in ms):" + vh.getEnd(i));
//					Log.d(TAG, "Length (in ms):" + vh.getLength(i));
//					Log.d(TAG, "Frame:" + (i));
//					Log.d(TAG, "Mouth:" + vh.getMouth(i));
//					Log.d(TAG, " ");
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
			
			
//			MarkerHolder mh;
//			final String TAG = "MARKER INFO";
//
//			try {
//				mh = synthesis.getMarkerInfo("Hello World", "usenglishfemale", "0", "mp3");
//				int words = mh.getWords();
//				Log.d("DEBUG", "WORDS:" + words);
//				Log.d("DEBUG", "TOTAL LENGTH (in ms):" + mh.getTotalLength());
//				for (int i = 0; i<words; i++){
//					
//					Log.d("DEBUG", "Start (in ms):" + mh.getStart(i));
//					Log.d("DEBUG", "End (in ms):" + mh.getEnd(i));
//					Log.d("DEBUG", "Length (in ms):" + mh.getLength(i));
//					Log.d("DEBUG", "Word:" + (i));
//					Log.d("DEBUG", "Text:" + mh.getText(i));
//					Log.d("DEBUG", " ");
//				}
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
		}
	}



	@Override
	protected void onPause() {
		synthesis.stop();	//Optional to stop the playback when the activity is paused
		super.onPause();
	}




}