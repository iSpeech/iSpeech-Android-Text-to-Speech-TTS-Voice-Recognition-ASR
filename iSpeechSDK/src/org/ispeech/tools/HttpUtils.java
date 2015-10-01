package org.ispeech.tools;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpUtils {
	
	private static final String TAG = "HttpUtils";

	public static String addParamsToUrl(String url, List<NameValuePair> params) {
		StringBuffer buff = new StringBuffer(url);
		for (NameValuePair pair : params) {
			try {
				buff.append("&" + URLEncoder.encode(pair.getName(), "utf-8") + "=" + URLEncoder.encode(pair.getValue(), "utf-8"));
			} catch (UnsupportedEncodingException e) {
			}
		}
		return buff.toString().replaceFirst("&", "?");
	}

	public static Map<String, String> parseNameValuePairEntity(HttpEntity entity) throws IOException {
		String content = EntityUtils.toString(entity);
		return parseNameValuePairString(content);
	}

	public static Map<String, String> parseNameValuePairString(String content) throws IOException {
		content = content.replaceAll("\\p{Cntrl}", "");
		Map<String, String> results = new HashMap<String, String>();
		String[] pairs = content.split("&");
		for (String pair : pairs) {
			String[] items = pair.split("=");
			results.put(items[0], items.length == 1 ? "" : URLDecoder.decode(items[1], "utf-8"));
		}
		return results;
	}
}
