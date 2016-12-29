package it.concurrent.leadfollow;

public interface EventHandlerListener<EVENT extends Event> {
	
	public void onEventReceived(EVENT event);
	
	public void onPreDispatchEvent(EVENT event);
	
	public void onPostDispatchEvent(EVENT event);

}
