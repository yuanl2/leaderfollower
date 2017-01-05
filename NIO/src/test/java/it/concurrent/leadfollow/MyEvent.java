package it.concurrent.leadfollow;

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