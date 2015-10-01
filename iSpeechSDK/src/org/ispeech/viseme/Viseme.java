package org.ispeech.viseme;

public class Viseme 
{
	
	public int _start;
	public int _end;
	public int _mouth;
	
	public Viseme (int start, int end, int mouth )
	{
		_start = start;
		_end = end;
		_mouth = mouth;
	}
	
	public int getStart(){
		return _start;
	}
	public int getEnd(){
		return _end;
	}
	public int getMouth(){
		return _mouth;
	}

}
