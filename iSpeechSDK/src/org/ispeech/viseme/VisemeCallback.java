package org.ispeech.viseme;

public interface VisemeCallback {

	public void onPlayStart();
	public void onNewViseme(int viseme);
	public void onPlayFinished();
	public void onVisemeHolderReady();
	public void onPlayStopped();
	public void onPlayCanceled();
	public void onPlayFailed(Exception e);
	public void onPlaySuccessful();
}
