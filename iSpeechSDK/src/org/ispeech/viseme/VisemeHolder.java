package org.ispeech.viseme;

import java.util.Vector;

import org.ispeech.error.InvalidApiKeyException;

/***
 * Object to hold information for a viseme animation
 */
public class VisemeHolder {
	private int _len;
	private int _frames;

	public Vector<Viseme> _viseme = new Vector<Viseme>();

	public VisemeHolder(int len, int frames) {
		_len = len;
		_frames = frames;
	}

	public void addViseme(Viseme viseme) {
		_viseme.addElement(viseme);
	}

	public Vector<Viseme> getViseme() {
		return _viseme;
	}

	/***
	 * Returns an int representing the start time for frame specified in
	 * milliseconds
	 * 
	 * @param frame
	 *            index for the frame to get information on
	 */
	public int getStart(int frame) {
		return ((Viseme) _viseme.get(frame)).getStart();
	}

	/***
	 * Returns an int representing the end time for frame specified in
	 * milliseconds
	 * 
	 * @param frame
	 *            index for the frame to get information on
	 */
	public int getEnd(int frame) {
		return ((Viseme) _viseme.get(frame)).getEnd();
	}

	/***
	 * Returns an int representing mouth viseme for current frame
	 * 
	 * @param frame
	 *            index for the frame to get information on
	 */
	public int getMouth(int frame) {
		return ((Viseme) _viseme.get(frame)).getMouth();
	}

	/***
	 * Returns an int representing the duration in milliseconds for specified
	 * frame
	 * 
	 * @param frame
	 *            index for the frame to get information on
	 */
	public int getLength(int frame) {
		return ((Viseme) _viseme.get(frame)).getEnd()
				- ((Viseme) _viseme.get(frame)).getStart();
	}

	/***
	 * Returns an int representing total time for entire viseme animation in
	 * milliseconds
	 * 
	 */
	public int getTotalLength() {
		return _len;
	}

	/***
	 * Returns an int representing number of frames in animation
	 * 
	 */
	public int getFrames() {
		return (_frames - 1);
	}

	public void addFrame() {
		_frames++;
	}

}
