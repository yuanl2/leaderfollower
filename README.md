# Leader/ Follower pattern java implementation

This implementation helps you to submit a task (EventHandler) to a Pool managed
with a Leader/Follower approach.

This type of Pool can perform better than a common Thread Pool in those situations
you need to minimize Thread Switching.


```java

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


```


```java

public class MyEvent implements Event {

	private String id;

	public MyEvent(String id) {
		super();
		this.id = id;
	}

	@Override
	public String getUUID() {
		return id;
	}

}


```

```java

package it.boomerang.concurrent.leadfollow;

import it.boomerang.concurrent.leadfollow.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MyEventHandler implements EventHandler<MyEvent> {

	private static Map<String, String> getMap() {
		HashMap<String, String> hashMap = new HashMap<String, String>();
		for (int i = 0; i < 1000; i++) {
			hashMap.put(new String(i + ""), new String(i + "_" + UUID.randomUUID().toString()));
		}
		return hashMap;
	}
	
	private static volatile int counter=0;
	
	public MyEventHandler(){
	}
	
	@Override
	public MyEvent receive() {
		if (counter < 30) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
			}
			MyEvent myEvent = new MyEvent(UUID.randomUUID().toString());
			counter++;
			return myEvent;
		}
		return new MyEvent("");
	}

	@Override
	public void dispatch(MyEvent e) {
		try {
			System.out.println(getMap());
		} catch (Exception exception) {
		}
	}


	@Override
	public String endTransmission() {
		return "";
	}

}


```


