package it.boomerang.concurrent.leadfollow;

import it.boomerang.concurrent.leadfollow.Pool;
import it.boomerang.concurrent.leadfollow.PoolFactory;

import org.junit.Assert;
import org.junit.Test;

public class PoolTest {
	
	
	@Test
	public void testStart() throws InterruptedException {
		final Pool<MyEvent> pool = new PoolFactory().newInstance(5, MyEventHandler.class);
		pool.addPoolListener(new PoolListenerImpl());
		pool.start();
		pool.join();
		pool.stop();
		Assert.assertTrue(pool.isShutDown());
	}

}
