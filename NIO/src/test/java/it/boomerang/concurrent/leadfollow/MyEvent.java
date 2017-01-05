package it.boomerang.concurrent.leadfollow;

import it.boomerang.concurrent.leadfollow.Event;

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