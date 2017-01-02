package it.concurrent.leadfollow.impl;

import it.concurrent.leadfollow.Event;
import it.concurrent.leadfollow.EventHandler;
import it.concurrent.leadfollow.Pool;
import it.concurrent.leadfollow.PoolThread;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class PoolThreadImpl<EVENT extends Event> extends Thread implements PoolThread<EVENT> {

	private static final Logger LOGGER = Logger.getLogger(Pool.class.getCanonicalName());

	private Object parking;

	private PoolImpl<EVENT> pool;

	private Date lastInvocation = new Date();

	private EventHandler<EVENT> eventHandler;

	private AtomicBoolean isLeader = new AtomicBoolean(false);

	private static final int MAX_EXPIRED_TIME = 30000;

	private volatile boolean stopped = false;

	public PoolThreadImpl(Object parking, PoolImpl<EVENT> pool, EventHandler<EVENT> eventHandler) {
		this.parking = parking;
		this.pool = pool;
		this.eventHandler = eventHandler;
	}

	@Override
	public void run() {

		while (!this.isStopped()) {
			try {
				while (pool.existLeader() && !this.isStopped()) {

					synchronized (parking) {

						if (this.isStopped()) {
							return;
						}
						isLeader.set(false);
						parking.wait();
						setThreadPriority(Thread.NORM_PRIORITY);
					}

				}

				isLeader.set(true);
				setThreadPriority(Thread.MAX_PRIORITY);

				EVENT event = eventHandler.receive();

				if (event == null || event.getUUID() == null) {
					continue;
				}

				if (event.getUUID().equals(eventHandler.endTransmission())) {
					pool.stopReceiving();
					return;
				}
				LOGGER.log(Level.FINE,"starting leader election");
				pool.startLeaderElection();

				heartBeat();
				LOGGER.log(Level.FINE,"starting dispatching thread={}",Thread.currentThread().getName());
				eventHandler.dispatch(event);
			} catch (Throwable e) {
				LOGGER.log(Level.WARNING,e.getMessage(), e);
			}
		}
	}

	private void setThreadPriority(int priority) {
		Thread.currentThread().setPriority(priority);
	}

	private boolean isStopped() {
		return pool.isToStop() || stopped;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.concurrent.leadfollow.IPoolThread#shutdown()
	 */
	@Override
	public void shutdown() {
		LOGGER.log(Level.FINE,"shutdown thread={}",getName());
		this.stopped = true;
		synchronized (this.parking) {
			this.parking.notify();
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