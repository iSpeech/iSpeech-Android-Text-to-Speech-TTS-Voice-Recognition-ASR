package org.ispeech.sdksample;

import org.ispeech.FreeformType;
import org.ispeech.SpeechRecognizer;
import org.ispeech.SpeechRecognizerEvent;
import org.ispeech.SpeechResult;
import org.ispeech.error.BusyException;
import org.ispeech.error.InvalidApiKeyException;
import org.ispeech.error.NoNetworkException;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


public class FreeformActivity extends Activity {
	private static final String TAG = "iSpeech SDK Sample";
	SpeechRecognizer recognizer;
	private Context _context;

	private void updateInfoMessage(String msg) {
		Log.v(TAG, "INFO message: " + msg);
		TextView t = (TextView) findViewById(R.id.freeform_text);
		t.setText(msg);
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_context = this.getApplicationContext();

		setContentView(R.layout.asr_freeform);
		findViewById(R.id.freeform_record).setOnClickListener(new FreeFormListener());
		findViewById(R.id.freeform_stop).setOnClickListener(new onStopListener());

		//setupFreeFormDictation();
		

		updateInfoMessage("Start recording");
	}

  
	private void setupFreeFormDictation() {
		try { 

			
			recognizer = SpeechRecognizer.getInstance(_context);  
			recognizer.setFreeForm(FreeformType.FREEFORM_DICTATION);
//			recognizer.setLocale("zh-CN");

		} catch (InvalidApiKeyException e) {
			e.printStackTrace();
		}
	}


	private class FreeFormListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			updateInfoMessage("Recording started");
			startRecognition();
		}
	}
	
	
	private class onStopListener implements OnClickListener {
		public void onClick(View v) {
			if(recognizer != null)
				recognizer.stopRecord();
		}
	}


	/**
	 * Fire an intent to start the speech recognition activity.
	 * @throws InvalidApiKeyException 
	 */
	private void startRecognition() {
		setupFreeFormDictation();
		//recognizer.addOptionalCommand("model","assistant");
		try {
			recognizer.startRecord(new SpeechRecognizerEvent() {
				@Override
				public void onRecordingComplete() { 
					updateInfoMessage("Recording completed."); 
				}
 
				@Override
				public void onRecognitionComplete(SpeechResult result) {
					Log.v(TAG, "Recognition complete");  
					if (result != null) {
						Log.d(TAG, "Text Result:" + result.getText());
						Log.d(TAG, "Text Conf:" + result.getConfidence());

						updateInfoMessage("Result: " + result.getText() + "\n\nconfidence: " + result.getConfidence());

					} else
						Log.d(TAG, "Result is null..."); 
				}

				@Override 
				public void onRecordingCancelled() {
					updateInfoMessage("Recording cancelled.");
				}

				@Override
				public void onError(Exception exception) {
					updateInfoMessage("ERROR: " + exception.getMessage());
					exception.printStackTrace();
				}
			});
		} catch (BusyException e) {
			e.printStackTrace();
		} catch (NoNetworkException e) {
			e.printStackTrace();
		}
	}
}