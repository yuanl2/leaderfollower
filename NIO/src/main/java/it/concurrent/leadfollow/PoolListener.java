package it.concurrent.leadfollow;

public interface PoolListener {

	public void onEventReceived(String threadName);

	public void onPreDispatchEvent(String threadName);

	public void onPostDispatchEvent(String threadName);
	
}