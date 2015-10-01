package org.ispeech.core;

import org.ispeech.DrawCanvas;
import org.ispeech.SpeechRecognizer;
import org.ispeech.error.InvalidApiKeyException;
import org.ispeech.tools.Utilities;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class RecDialog extends Dialog {
	Context context;
	RelativeLayout parentView;
	DrawCanvas microphone;
	private Button stop;
	private Button cancel;

	public static final int DISMISS_DIALOG = 0;
	public static final int SHOW_DIALOG = 1;
	public static final int SET_PROCESSING = 2;
	public static final int UPDATE_MICROPHONE_LEVEL = 3;
	public static final int SET_LISTENING = 4;
	
	public RecDialog(Context context) {
		super(context, android.R.style.Theme_Light_Panel);
		this.context = context;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setCancelable(false);

		initLayout();
	}

	private void initLayout() {

		GradientDrawable titleBg = new GradientDrawable(
				Orientation.BOTTOM_TOP,
				new int[] { 0xff222222, 0xff888888 });
		titleBg.setCornerRadii(new float[] { 5, 5, 5, 5, 0, 0, 0, 0 });
		int width = (int) Utilities.dp2px(150, context);

		// parentView setup
		RelativeLayout.LayoutParams parentViewLayoutParams = new RelativeLayout.LayoutParams(
				width, RelativeLayout.LayoutParams.MATCH_PARENT);
		parentViewLayoutParams.setMargins(
				(int) Utilities.dp2px(50, context),
				(int) Utilities.dp2px(10, context),
				(int) Utilities.dp2px(50, context),
				(int) Utilities.dp2px(10, context));

		parentView = new RelativeLayout(context);
		parentView.setLayoutParams(parentViewLayoutParams);
		parentView.setGravity(Gravity.CENTER);

		microphone = new DrawCanvas(context, width);
		RelativeLayout.LayoutParams microphoneLayoutParams = new RelativeLayout.LayoutParams(width, LayoutParams.WRAP_CONTENT);
		microphone.setId(1000);
		microphone.setLayoutParams(microphoneLayoutParams);
		parentView.addView(microphone);

		// set up the buttons
		LinearLayout buttons = new LinearLayout(context);
		RelativeLayout.LayoutParams buttonsLayoutParams = new RelativeLayout.LayoutParams(width, LinearLayout.LayoutParams.WRAP_CONTENT);
		buttonsLayoutParams.addRule(RelativeLayout.BELOW, microphone.getId());
		buttons.setLayoutParams(buttonsLayoutParams);
		buttons.setBackgroundColor(0xff888888);
		buttons.setGravity(Gravity.CENTER);
		addCancelStopButtons(buttons);
		parentView.addView(buttons); // add the view

		this.setContentView(parentView);
		setRecording();
	}

	private void addCancelStopButtons(LinearLayout child) {
		this.stop = new Button(context);
		stop.setText("Done");
		this.cancel = new Button(context);
		cancel.setText("Cancel");

		stop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					SpeechRecognizer.getInstance(context).stopRecord();
				} catch (InvalidApiKeyException e) {
				}
			}
		});

		cancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					SpeechRecognizer.getInstance(context).cancelRecord();
				} catch (InvalidApiKeyException e) { }
				clearLayout();
				dismiss();
			}
		});

		stop.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		cancel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		child.addView(stop);
		child.addView(cancel);
	}

	public void clearLayout() {
		parentView.removeAllViews();
	}

	public void setRecording() {
		stop.setVisibility(View.VISIBLE);
		cancel.setVisibility(View.VISIBLE);
	}

	public void setRecognizing() {
		microphone.setProcessing();
		stop.setVisibility(View.GONE);

		cancel.setVisibility(View.VISIBLE);
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					SpeechRecognizer.getInstance(context).cancelProcessing();
				} catch (InvalidApiKeyException e) {
				}
				clearLayout();
				dismiss();
			}
		});
	}

	public void startDraw() {
		microphone.startDrawImage();
	}
	
	public void setMicrophoneLevel(int level) {
		microphone.setY(level);
	}
	
	@Override
	public void dismiss() {
		super.dismiss();
	}

	@Override
	public void onBackPressed() {
		try {
			SpeechRecognizer.getInstance(context).cancelRecord();
		} catch (InvalidApiKeyException e) {
		}
		super.onBackPressed();
	}

}
