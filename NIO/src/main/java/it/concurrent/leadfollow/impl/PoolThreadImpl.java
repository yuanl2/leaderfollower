package it.concurrent.leadfollow.impl;

import it.concurrent.leadfollow.Event;
import it.concurrent.leadfollow.EventHandler;
import it.concurrent.leadfollow.PoolThread;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class PoolThreadImpl<EVENT extends Event> extends Thread implements PoolThread<EVENT> {

	private Object parking;

	private PoolImpl<EVENT> pool;

	private Date lastInvocation = new Date();

	private EventHandler<EVENT> eventHandler;

	private AtomicBoolean isLeader = new AtomicBoolean(false);

	private static final int MAX_EXPIRED_TIME = 30000;

	public PoolThreadImpl(Object parking, PoolImpl<EVENT> pool, EventHandler<EVENT> eventHandler) {
		this.parking = parking;
		this.pool = pool;
		this.eventHandler = eventHandler;
	}

	@Override
	public void run() {
		try {
			while (!pool.isToStop()) {

				while (pool.existLeader() && !pool.isToStop()) {

					synchronized (parking) {

						if (pool.isToStop()) {
							return;
						}
						isLeader.set(false);
						parking.wait();
						Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
					}
					
				}

				isLeader.set(true);
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

				EVENT event = eventHandler.receive();

				if (event == null || event.getUUID() == null) {
					continue;
				}

				if (event.getUUID().equals(eventHandler.endTransmission())) {
					pool.stopReceiving();
					return;
				}

				pool.startLeaderElection();

				heartBeat();
				eventHandler.dispatch(event);
			}
		} catch (Throwable e) {

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.concurrent.leadfollow.IPoolThread#shutdown()
	 */
	@Override
	public void shutdown() {
		synchronized (parking) {
			parking.notify();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.concurrent.leadfollow.IPoolThread#isExpired()
	 */
	@Override
	public boolean isExpired() {
		return isLeader.get() && (new Date().getTime() - lastInvocation.getTime()) > MAX_EXPIRED_TIME;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.concurrent.leadfollow.IPoolThread#heartBeat()
	 */
	@Override
	public void heartBeat() {
		this.lastInvocation = new Date();
	}

}