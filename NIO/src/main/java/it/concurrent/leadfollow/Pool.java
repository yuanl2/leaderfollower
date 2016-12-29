package it.concurrent.leadfollow;

public interface Pool<EVENT extends Event> {

	public void stop();

	public void start();

	public void addPoolListener(PoolListener poolListener);

	public boolean isShutDown();

}