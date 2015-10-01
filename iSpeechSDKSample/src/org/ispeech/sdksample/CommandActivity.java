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
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class CommandActivity extends Activity {
	private static final String TAG = "iSpeech SDK Sample";
	SpeechRecognizer recognizer;
	private Context _context;



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_context = this.getApplicationContext();

		setContentView(R.layout.asr_command);
		findViewById(R.id.cmd_record).setOnClickListener(new onCommandListener());
		findViewById(R.id.cmd_stop).setOnClickListener(new onStopListener());

	}


	private class onCommandListener implements OnClickListener {

		public void onClick(View v) {
			try {
				recognizer = SpeechRecognizer.getInstance(_context);
				recognizer.setFreeForm(FreeformType.FREEFORM_DISABLED);
				recognizer.setLocale("en-US");
				recognizer.addCommand(new String[] { "check %WHAT%" });
				recognizer.addAlias("WHAT", new String[] { "status", "signal", "network", "phone number" });
				recognizer.addCommand(new String[] { "forward", "backward" });


				startRecognition();
			} catch (InvalidApiKeyException e) {
				e.printStackTrace();
			}
		}
	}

	
	private void updateInfoMessage(String text) {
		Log.v(TAG, text);
		TextView t = (TextView) findViewById(R.id.cmd_text);
		t.setText(text);
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
		try {
			recognizer.startRecord(new SpeechRecognizerEvent() {
				public void onRecordingComplete() {
					Log.v(TAG, "Recording complete");
				}

				public void onRecognitionComplete(SpeechResult result) {
					Log.v(TAG, "-> recognition complete");
					if (result != null) {
						Log.v(TAG, "Text Result:" + result.getText());
						Log.v(TAG, "Text Conf:" + result.getConfidence());
						updateInfoMessage("result: " + result.getText() + "\nconfidence: " + result.getConfidence());
					} else
						Log.v(TAG, "result is null...");
				}

				public void onRecordingCancelled() {
					Log.v(TAG, "-> recording cancelled");
				}

				public void onError(Exception exception) {
					Log.v(TAG, "-> onError: " + exception.getMessage());
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