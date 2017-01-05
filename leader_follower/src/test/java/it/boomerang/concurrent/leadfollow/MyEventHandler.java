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