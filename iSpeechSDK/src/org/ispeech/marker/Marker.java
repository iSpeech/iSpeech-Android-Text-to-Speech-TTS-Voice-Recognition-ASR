package org.ispeech.marker;

public class Marker 
{
	
	public int _start;
	public int _end;
	public String _text;
	
	public Marker (int start, int end, String text )
	{
		_start = start;
		_end = end;
		_text = text;
	}
	
	public int getStart(){
		return _start;
	}
	public int getEnd(){
		return _end;
	}
	public String getText(){
		return _text;
	}
}
