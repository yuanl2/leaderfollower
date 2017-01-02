package it.concurrent.leadfollow;

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
