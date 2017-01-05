package it.boomerang.concurrent.leadfollow;

public interface PoolThread<EVENT extends Event> {

	public void shutdown();

	public boolean isExpired();

	public void heartBeat();

}