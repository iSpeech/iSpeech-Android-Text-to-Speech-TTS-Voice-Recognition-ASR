package org.ispeech.marker;

import java.util.Vector;

/***
 * Object to hold information for a viseme animation
 */
public class MarkerHolder 
{
	private String _text;
	private String _voice;
	private int _len;
	private int _words;
	
	public Vector<Marker> _marker = new Vector<Marker>();
	
	public MarkerHolder(String text, String voice, int len, int words)
	{
		_text = text;
		_voice = voice;
		_len = len;
		_words = words;
	}
	
	public void addMarker(Marker marker)
	{
		_marker.addElement(marker);
	}
	
	public Vector<Marker> getMarker ()
	{
		return _marker;
	}
	
	/***
	 * Returns an int representing the start time for frame specified in milliseconds 
	 * 
	 * @param frame
	 *            index for the frame to get information on
	 */
	public int getStart(int frame){
		return ((Marker)_marker.get(frame)).getStart();
	}
	/***
	 * Returns an int representing the end time for frame specified in milliseconds 
	 * 
	 * @param frame
	 *            index for the frame to get information on
	 */
	public int getEnd(int frame){
		return ((Marker)_marker.get(frame)).getEnd();
	}
	/***
	 * Returns an int representing mouth viseme for current frame 
	 * 
	 * @param frame
	 *            index for the frame to get information on
	 */
	public String getText(int frame){
		return ((Marker)_marker.get(frame)).getText();
	}
	/***
	 * Returns an int representing the duration in milliseconds for specified frame
	 * 
	 * @param frame
	 *            index for the frame to get information on
	 */
	public int getLength(int frame){
		return ((Marker)_marker.get(frame)).getEnd()-((Marker)_marker.get(frame)).getStart();
	}
	/***
	 * Returns an int representing total time for entire viseme animation in milliseconds 
	 * 
	 */
	public int getTotalLength(){
		return _len;
	}
	/***
	 * Returns an int representing number of frames in animation
	 * 
	 */
	public int getWords(){
		return (_words-1);
	}
	
	public void addFrame(){
		_words++;
	}

}
