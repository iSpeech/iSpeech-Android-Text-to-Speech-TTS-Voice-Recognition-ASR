package org.ispeech;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

public class DrawCanvas extends RelativeLayout {

	private Context myContext;
	private ProgressBar loadingBar;
	private DrawMicrophone microphone;
	private Rect volumeBar;
	private Paint volumeBarPaint;

	private int width;
	private int height;
	private int volumeBarPercent = 0;
	private long lastTime = System.currentTimeMillis();
	private int deltaTime = 0;
	private double lastPerc = 0;

	private final double UP_RATE = 0.15f; // percent per millisecond
	private final double DOWN_RATE = 0.3; // percent per millisecond
	private final int PER_THRESH = 3;

	public DrawCanvas(Context context) {
		super(context);

		init(context, 0);
	}

	public DrawCanvas(Context context, int nWidth) {
		super(context);

		init(context, nWidth);
	}

	private void init(Context context, int nWidth) {
		myContext = context;
		width = nWidth;
		this.setFocusable(true); // set this layout to be focusable

		// set the background dark
		this.setBackgroundResource(android.R.color.background_dark);

		// set up the loading bar
		loadingBar = new ProgressBar(DrawCanvas.this.myContext, null, android.R.attr.progressBarStyleLarge);
		RelativeLayout.LayoutParams loadingBarLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		loadingBarLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		loadingBar.setLayoutParams(loadingBarLayoutParams);
		DrawCanvas.this.addView(loadingBar);
		loadingBar.setVisibility(View.VISIBLE);

		// set up the microphone image
		microphone = new DrawMicrophone(myContext);
	}

	int i = 200;
	
	public void setY(int y) {
		
		if(y > i) i = y;
		
		volumeBarPercent = (y*100) / i;
	}

	private int percToPos(int perc) {
		int ret = 0;
		ret = (perc * height) / 100;
		ret -= height;
		ret *= -1;
		
		return ret;
	}
	
	public int getVolumeBarHeight() {
		deltaTime = (int) (System.currentTimeMillis() - lastTime);
		lastTime = System.currentTimeMillis();
		
		
		double deltaChange = volumeBarPercent - lastPerc;
		
		if(Math.abs(deltaChange) > PER_THRESH) {
			
			if(deltaChange > 0) {
				deltaChange = Math.min(deltaChange, deltaTime*UP_RATE);
			} else {
				deltaChange = Math.max(deltaChange, -deltaTime*DOWN_RATE);
			}
			
			lastPerc += deltaChange;
		}
		
		return percToPos((int) lastPerc);
	}

	public void setProcessing() { // start showing that the recording is uploading
		microphone.setVisibility(View.GONE);
		DrawCanvas.this.setBackgroundResource(android.R.color.background_dark);
		loadingBar.setVisibility(View.VISIBLE);
	}

	public void startDrawImage() {
		loadingBar.setVisibility(View.GONE);
		DrawCanvas.this.addView(microphone);
		microphone.setVisibility(View.VISIBLE);
	}

	private class DrawMicrophone extends SurfaceView implements
			SurfaceHolder.Callback {
		private Bitmap microphoneImage;
		private CanvasThread canvasThread;
		int drawHeight;

		public DrawMicrophone(Context context) {
			super(context);

			loadImage();

			// once you know the size of the image, set the size of the view
			setLayoutParams(new LayoutParams(microphoneImage.getWidth(), microphoneImage.getHeight()));

			setWillNotDraw(false);
			getHolder().addCallback(this);
		}

		private Bitmap scaleBitmap(Bitmap src) { // scale the bitmap's height to
													// the correct width
			float wToH = (float) src.getHeight() / (float) src.getWidth(); // get the ratio of height to width
			int height = (int) (width * wToH);

			return Bitmap.createScaledBitmap(src, width, height, false);
		}

		private void loadImage() { // load the microphoneImage
			microphoneImage = BitmapFactory.decodeStream(DrawCanvas.class.getClassLoader().getResourceAsStream("org/ispeech/raw/microphone_back_v_7.png"));
			microphoneImage = scaleBitmap(microphoneImage);
		}

		public void render(Canvas canvas) {
			canvas.drawColor(Color.BLACK); // clean the canvas

			// get the coordinates of the canvas to draw the mic volume
			height = canvas.getHeight();
			
			drawHeight = getVolumeBarHeight();
			volumeBar.set(0, drawHeight, width, height);
			canvas.drawRect(volumeBar, volumeBarPaint);
			canvas.drawCircle(width / 2, drawHeight, width / 4, volumeBarPaint);
			canvas.drawBitmap(microphoneImage, 0, 0, null);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) { }

		@Override
		public void surfaceCreated(SurfaceHolder holder) {

			// set up the volume bar
			volumeBar = new Rect();
			volumeBarPaint = new Paint();
			volumeBarPaint.setColor(Color.WHITE);

			if(canvasThread != null) {
				canvasThread.setRunning(false);
				canvasThread = null;
			}
			
			// create the canvasThread
			canvasThread = new CanvasThread(holder);

			// start the canvasThread
			canvasThread.setRunning(true);
			canvasThread.start();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// kill the canvasThread
			canvasThread.setRunning(false);
		}

		private class CanvasThread extends Thread {
			private boolean isRunning = false;
			SurfaceHolder mySurfaceHolder;
			Canvas myCanvas;

			CanvasThread(SurfaceHolder holder) {
				mySurfaceHolder = holder;
			}

			public void setRunning(boolean run) {
				isRunning = run;
			}

			@Override
			public void run() {
				while (isRunning) {
					myCanvas = null;
					try {
						myCanvas = mySurfaceHolder.lockCanvas(); // lock the canvas
						if (myCanvas == null)
							continue;
						synchronized (mySurfaceHolder) {
							DrawCanvas.this.microphone.render(myCanvas);
						}
					} finally {
						if (myCanvas != null) {
							mySurfaceHolder.unlockCanvasAndPost(myCanvas);
						}
					}
				}
			}
		}
	}
}
