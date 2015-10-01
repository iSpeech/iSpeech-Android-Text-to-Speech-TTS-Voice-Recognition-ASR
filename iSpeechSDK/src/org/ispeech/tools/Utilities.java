package org.ispeech.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.ispeech.core.InternalResources;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class Utilities {
	private static String TAG = "iSpeech SDK" + Utilities.class.getSimpleName();
	
	public static final boolean APIKEY_GOOD = false;

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {

		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static char[] encodeBase64(byte[] data) throws IOException {
		return Base64Coder.encode(data);
	}

	public static String getNetworkName(Context context) {
		try {
			return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName();
		} catch (Exception e) {
			return "";
		}
	}

	public static String getNetworkType(Context context) {
		try {
			TelephonyManager tm = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE));
			int type = tm.getNetworkType();
			switch (type) {
			case TelephonyManager.NETWORK_TYPE_CDMA:
				return "CDMA";
			case TelephonyManager.NETWORK_TYPE_GPRS:
				return "GPRS";
			case TelephonyManager.NETWORK_TYPE_UMTS:
				return "UMTS";
			case TelephonyManager.NETWORK_TYPE_1xRTT:
				return "1xRTT";
			case TelephonyManager.NETWORK_TYPE_EDGE:
				return "EDGE";
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
				return "EVDO_0";
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
				return "EVDO_A";

			default:
				return "UNKNOWN";
			}
		} catch (Exception e) {
			return "";
		}
	}

	/**
	 * @param dp
	 * @return
	 * 			- pixels, calculated based on the current density
	 */
	public static float dp2px(float dp, Context context) {
		return dp * context.getResources().getDisplayMetrics().density + 0.5f;
	}

	//For statistics
	public static SerializableHashTable getMetaInfo(Context context) {
		SerializableHashTable meta = new SerializableHashTable();
		meta.put("networkType", Utilities.getNetworkType(context));
		meta.put("networkName", Utilities.getNetworkName(context));
		meta.put("sdkVersion", SDKInfo.SDK_VERSION);
		meta.put("deviceType", "Android");
		return meta;
	}

	public static File loadFileFromPackage(Context c, String path, String name) throws IOException {
		InputStream stream = Utilities.class.getClassLoader().getResourceAsStream(path);
		File privateFile = new File(c.getFilesDir(), name);
		if (!privateFile.exists()) {
			if (!privateFile.isFile()) {
				if (stream != null) {
					FileOutputStream out1 = c.openFileOutput(name, Context.MODE_PRIVATE);
					byte buf[] = new byte[16384];
					int numread = 0;
					while ((numread = stream.read(buf)) > 0)
					{
						out1.write(buf, 0, numread);						
					}
					stream.close();
					out1.close();
				}
			}
		}
		return privateFile;
	}

	public static String getApiKey(Context context) {
		try {
			ApplicationInfo ai;
			ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			try {
				return ai.metaData.getString(InternalResources.ISPEECH_SCREEN_APIKEY);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	public static boolean isDebug(Context context) {
		try {
			ApplicationInfo ai;
			ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
			boolean debug = ai.metaData.getBoolean(InternalResources.ISPEECH_SCREEN_DEBUG);
			return debug;
		} catch (NameNotFoundException e) {
			return false;
		}
	}
}
