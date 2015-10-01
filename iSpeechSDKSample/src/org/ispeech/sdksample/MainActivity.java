package org.ispeech.sdksample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;


public class MainActivity extends Activity {

	private static final String TAG = "iSpeech SDK Sample";
	private Context _context;

   
 
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		_context = this.getApplicationContext();
		setContentView(R.layout.main);

		findViewById(R.id.tts).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent myIntent = new Intent(_context, TTSActivity.class);
				startActivity(myIntent);
			}
		});


		findViewById(R.id.freeform).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent myIntent = new Intent(_context, FreeformActivity.class);
				startActivity(myIntent);
			}
		});


		findViewById(R.id.cmd).setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent myIntent = new Intent(_context, CommandActivity.class);
				startActivity(myIntent);
			}
		});
	}

}