package it.boomerang.concurrent.leadfollow;

public interface EventHandler<EVENT extends Event> {
	
	public EVENT receive();

	public void dispatch(EVENT event);
	
	public String endTransmission();
	
}