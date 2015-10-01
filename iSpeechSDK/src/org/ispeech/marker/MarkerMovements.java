package org.ispeech.marker;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Vector;

public class MarkerMovements
{
	private String _urlBaseVisme = "http://api.ispeech.org/api/rest/?apikey=";
	
	private String _text;
	private boolean _valid = false;
	public MarkerHolder _holder;
	
	final String TAG="MouthMovements";
	
	public MarkerMovements(String text)
	{
		_text = text;		
		getVisimes();
	}
	
	public MarkerMovements(String text, String apiKey)
	{
		_urlBaseVisme = _urlBaseVisme+apiKey;
		_urlBaseVisme = _urlBaseVisme + "&action=markers&";
		_text = text;		
		getVisimes();
	}
	
	public MarkerMovements(String text, String apiKey, String voice, String speed,String format)
	{
		_urlBaseVisme = _urlBaseVisme+apiKey;
		_urlBaseVisme = _urlBaseVisme + "&action=markers";
		_urlBaseVisme = _urlBaseVisme + "&voice=" + voice;
		_urlBaseVisme = _urlBaseVisme + "&speed=" + speed;
		_urlBaseVisme = _urlBaseVisme + "&format=" + format + "&";
		_text = text;
		getVisimes();
	}
	
	
	private void getVisimes()
	{
		MarkerConnection conn = new MarkerConnection(getURL(),this);
		conn.run();
		
		
		_holder = conn.getHolder();
	}
	
	public void gotMarkers(MarkerHolder holder)
	{
		_holder = holder;
		_holder.addFrame();
		_holder.getMarker().addElement(new Marker(holder.getTotalLength()+1, holder.getTotalLength()+1000, ""));
		Vector<Marker> markerMovements = holder._marker;
		
		if(markerMovements != null && markerMovements.size() > 0)
			_valid = true;
		
	}
	
	public MarkerHolder getMarkerHolder(){
		return _holder;
	}

	public String getText()
	{
		return _text;
	}
	
	private String getURL ()
	{
		String encodedText="";
		try {
			encodedText = URLEncoder.encode(_text, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String finalString = _urlBaseVisme + "text=" + encodedText +"&deviceType=Android";
		
		return finalString;
	}
	public boolean validVismes()
	{
		return _valid;
	}
	
}
