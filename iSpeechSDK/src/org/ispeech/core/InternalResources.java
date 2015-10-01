package org.ispeech.core;

import org.ispeech.tools.Utilities;
import android.content.Context;
import android.media.AudioManager;


public class InternalResources {
	private InternalResources() {

	}

	public static int mutex = 1;

	public static final String ISPEECH_SCREEN_APIKEY = "ispeech_api_key";
	public static final String ISPEECH_SCREEN_DEBUG = "debug";

	public static int STREAM_TYPE = AudioManager.STREAM_MUSIC; //changed from system because no longer works on jellybean
	public static boolean ALWAYS_SPEAK = false;
	public static final String LIVE_ASR_DOMAIN="asr.ispeech.org";
	
	public static final String LIVE_API_URL = "http://api.ispeech.org/api/rest"; //normal
	public static final String DEVELOPMENT_API_URL = "http://dev.ispeech.org/api/rest";
	public static final String DEVELOPMENT_ASR_DOMAIN="dev.ispeech.org";
	public static boolean Production = false;
	public static String getAPIUrl(Context context) {
		if (!Utilities.isDebug(context))
			return LIVE_API_URL;
		else
			return DEVELOPMENT_API_URL;
	}
	public static String getASRDomain(Context context)
	{
		if (!Utilities.isDebug(context))
			return LIVE_ASR_DOMAIN;
		else
			return DEVELOPMENT_ASR_DOMAIN;
	}
	
	/**
	 * Sets streamType to be used by SDK. Default is AudioManager.STREAM_MUSIC.
	 * @param streamType sets stream type ie. AudioManager.STREAM_SYSTEM (int value 1) or AudioManager.STREAM_MUSIC (int value 3)
	 */
	public static void setStreamType(int streamType){
		STREAM_TYPE = streamType;
	}
	
	/**
	 * Sets SDK so that TTS will always speak, regardless of whether the user has device on silent or not. Default is false.
	 * @param alwaysSpeak set to TRUE to have SDK always speak. set to FALSE to have SDK stay silent if device is silent. 
	 */
	public static void alwaysSpeak(boolean alwaysSpeak){
		ALWAYS_SPEAK=alwaysSpeak;
	}
}
