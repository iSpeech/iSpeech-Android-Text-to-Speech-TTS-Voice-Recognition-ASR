package org.ispeech.viseme;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Vector;

public class MouthMovements
{
	private String _urlBaseVisme = "http://api.ispeech.org/api/rest/?apikey=";
	
	private String _text;
	private boolean _valid = false;
	public VisemeHolder _holder;
	
	final String TAG="MouthMovements";
	
	public MouthMovements(String text)
	{
		_text = text;		
		getVisimes();
	}
	
	public MouthMovements(String text, String apiKey)
	{
		_urlBaseVisme = _urlBaseVisme+apiKey;
		_urlBaseVisme = _urlBaseVisme + "&action=viseme&";
		_text = text;		
		getVisimes();
	}
	
	public MouthMovements(String text, String apiKey, String voice, String speed,String format)
	{
		_urlBaseVisme = _urlBaseVisme+apiKey;
		_urlBaseVisme = _urlBaseVisme + "&action=viseme";
		_urlBaseVisme = _urlBaseVisme + "&voice=" + voice;
		_urlBaseVisme = _urlBaseVisme + "&speed=" + speed;
		_urlBaseVisme = _urlBaseVisme + "&format=" + format + "&";
		_text = text;		
		getVisimes();
	}
	
	
	private void getVisimes()
	{
		VisemeConnection conn = new VisemeConnection(getURL(),this);
		conn.run();
		
		_holder = conn.getHolder();
	}
	
	public void gotVisimes(VisemeHolder holder)
	{
		_holder = holder;
		_holder.addFrame();
		_holder.getViseme().addElement(new Viseme(holder.getTotalLength()+1, holder.getTotalLength()+1000, 0));
		Vector<Viseme> mouthMovements = holder._viseme;
		
		if(mouthMovements != null && mouthMovements.size() > 0)
			_valid = true;
	}
	
	public VisemeHolder getVisemeHolder(){
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
