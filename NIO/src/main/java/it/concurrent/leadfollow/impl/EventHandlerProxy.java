package it.concurrent.leadfollow.impl;

import it.concurrent.leadfollow.Event;
import it.concurrent.leadfollow.EventHandler;
import it.concurrent.leadfollow.EventHandlerListener;

public class EventHandlerProxy<EVENT extends Event,HANDLER extends EventHandler<EVENT>> implements EventHandler<EVENT> {

	private EventHandlerListener<EVENT> eventHandlerListener;

	private HANDLER eventHandler;
	
	public EventHandlerProxy(HANDLER eventHandler, EventHandlerListener<EVENT> eventHandlerListener) {
		this.eventHandler = eventHandler;
		this.eventHandlerListener = eventHandlerListener;
	}

	@Override
	public EVENT receive() {
		EVENT event = eventHandler.receive();
		eventHandlerListener.onEventReceived(event);
		return event;
	}

	@Override
	public void dispatch(EVENT e) {
		eventHandlerListener.onPreDispatchEvent(e);
		eventHandler.dispatch(e);
		eventHandlerListener.onPostDispatchEvent(e);
		
	}

	
	@Override
	public String endTransmission() {
		return eventHandler.endTransmission();
	}

}
