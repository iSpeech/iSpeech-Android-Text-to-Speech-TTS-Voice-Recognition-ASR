package org.ispeech.marker;

public interface MarkerCallback {

	public void onPlayStart();
	public void onNewWord(int start, int stop);
	public void onPlayFinished();
	public void onMarkerHolderReady();
	public void onPlayStopped();
	public void onPlayCanceled();
	public void onPlayFailed(Exception e);
	public void onPlaySuccessful();
}
