package it.boomerang.concurrent.leadfollow;

import it.boomerang.concurrent.leadfollow.impl.PoolImpl;

public class PoolFactory {
	
	public <EVENT extends Event,HANDLER extends EventHandler<EVENT>> Pool<EVENT> newInstance(int poolsize, Class<HANDLER> eventHandler) {
		Pool<EVENT> pool = new PoolImpl<EVENT>(poolsize, eventHandler);
		return pool;
	}
}
