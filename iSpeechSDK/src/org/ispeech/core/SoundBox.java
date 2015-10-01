package org.ispeech.core;

import java.io.File;
import java.io.IOException;

import org.ispeech.tools.Utilities;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;

public class SoundBox {
	private static final String TAG = "SoundBox";
	private static final int S_LEN = 500; // the dings are about 500 ms long
	private Context context;
	private SoundPool soundPool;
	private SparseIntArray soundPoolMap;
	long doneTime = 0;

	private static SoundBox instance;

	public static SoundBox getInstance(Context context) {
		if (instance == null)
			instance = new SoundBox(context);
		return instance;
	}

	private SoundBox(Context context) {
		this.context = context;
		if (soundPool == null) {
			soundPool = new SoundPool(4, InternalResources.STREAM_TYPE, 100);
			try {
				File open = Utilities.loadFileFromPackage(context, "org/ispeech/raw/voice_open.mp3", "voice_open.mp3");
				File close = Utilities.loadFileFromPackage(context, "org/ispeech/raw/voice_close.mp3", "voice_close.mp3");
				soundPoolMap = new SparseIntArray();
				soundPoolMap.put(0, soundPool.load(close.getAbsolutePath(), 1));
				soundPoolMap.put(1, soundPool.load(open.getAbsolutePath(), 1));
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) { }
			} catch (IOException e) { }
		}
	}

	private void playSound(int sound) {
		/* Updated: calculate the current volume in a scale of  0.0 to 1.0 */
		AudioManager mgr = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		float streamVolumeCurrent = mgr.getStreamVolume(InternalResources.STREAM_TYPE);
		float streamVolumeMax = mgr.getStreamMaxVolume(InternalResources.STREAM_TYPE);
		float volume = streamVolumeCurrent;
		

		volume = streamVolumeMax/2;
		
		/* Play the sound with the correct volume */
		soundPool.play(soundPoolMap.get(sound), volume, volume, 1, 0, 1f);
		doneTime = System.currentTimeMillis() + S_LEN;
	}

	public void playOpen() {
		playSound(1);
	}

	public void playClose() {
		playSound(0);
	}
	
	public void blockTillDone() {
		while(System.currentTimeMillis() < doneTime);
	}
}
