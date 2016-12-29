package it.concurrent.leadfollow.impl;

import it.concurrent.leadfollow.Event;
import it.concurrent.leadfollow.EventHandler;
import it.concurrent.leadfollow.EventHandlerListener;
import it.concurrent.leadfollow.Pool;
import it.concurrent.leadfollow.PoolListener;
import it.concurrent.leadfollow.PoolThread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class PoolImpl<EVENT extends Event> implements Pool<EVENT> {

	private Object parking = new Object();

	private Collection<PoolListener> poolListeners = Collections.synchronizedList(new ArrayList<PoolListener>());

	private volatile boolean stop = false;

	private volatile boolean stopReceiving = false;

	private AtomicBoolean existLeader = new AtomicBoolean(true);

	private List<PoolThreadImpl<EVENT>> threadPool = new ArrayList<PoolThreadImpl<EVENT>>();

	private Evictor evictor = new Evictor();

	private int size;

	private class Evictor extends Thread {
		@Override
		public void run() {
			while (!isShutDown()) {

				try {

					Thread.sleep(10000);

					Iterator<PoolThreadImpl<EVENT>> poolIterator = threadPool.iterator();

					while (poolIterator.hasNext()) {
						PoolThreadImpl<EVENT> poolThread = poolIterator.next();
						if (poolThread.isExpired()) {
							try {
								poolThread.interrupt();
							} catch (Throwable t) {
							}
							poolIterator.remove();
						}
					}

					int difference = size - threadPool.size();

					if (difference > 0) {
						for (int i = 1; i <= difference; i++) {
							PoolThreadImpl<EVENT> newThread = new PoolThreadImpl<EVENT>(parking, PoolImpl.this, null);
							threadPool.add(newThread);
						}
						if (!existLeader()) {
							try {
								startLeaderElection();
							} catch (Throwable t) {
							}
						}
					}

				} catch (Throwable t) {
				}

			}
		}
	}

	private EventHandlerListener<EVENT> eventHandlerListener = new EventHandlerListener<EVENT>() {
		@Override
		public void onEventReceived(EVENT event) {
			notifyEventReceived(Thread.currentThread().getName());
		}

		@Override
		public void onPreDispatchEvent(EVENT event) {
			notifyPreDispatchEventListener(Thread.currentThread().getName());
		}

		@Override
		public void onPostDispatchEvent(EVENT event) {
			notifyPostDispatchEventListener(Thread.currentThread().getName());
		}

	};

	public <HANDLER extends EventHandler<EVENT>> PoolImpl(int size, Class<HANDLER> eventHandler) {

		try {
			this.size = size;
			HANDLER newInstance = eventHandler.newInstance();

			EventHandlerProxy<EVENT, HANDLER> eventHandlerProxy = new EventHandlerProxy<EVENT, HANDLER>(newInstance, eventHandlerListener);

			for (int i = 0; i < size; i++) {
				PoolThreadImpl<EVENT> poolThread = new PoolThreadImpl<EVENT>(parking, this, eventHandlerProxy);
				threadPool.add(poolThread);
			}

		} catch (InstantiationException e1) {
			throw new RuntimeException(e1);
		} catch (IllegalAccessException e1) {
			throw new RuntimeException(e1);
		}

	}

	void startLeaderElection() {
		existLeader.compareAndSet(true, false);
		synchronized (parking) {
			parking.notify();
		}
	}

	boolean existLeader() {
		return existLeader.compareAndSet(false, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.concurrent.leadfollow.Pool#stop()
	 */
	@Override
	public void stop() {
		this.stop = true;
		for (PoolThread<EVENT> poolThread : this.threadPool) {
			poolThread.shutdown();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.concurrent.leadfollow.Pool#start()
	 */
	@Override
	public void start() {
		this.stop = false;
		for (PoolThreadImpl<EVENT> poolThread : this.threadPool) {
			poolThread.start();
		}

		startLeaderElection();

		evictor.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * it.concurrent.leadfollow.Pool#addPoolListener(it.concurrent.leadfollow
	 * .PoolListener)
	 */
	@Override
	public void addPoolListener(PoolListener poolListener) {
		this.poolListeners.add(poolListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see it.concurrent.leadfollow.Pool#isShutDown()
	 */
	@Override
	public boolean isShutDown() {
		return stop;
	}

	void stopReceiving() {
		this.stopReceiving = true;
	}

	boolean isStopReceiving() {
		return stopReceiving;
	}

	boolean isToStop() {
		return isShutDown() || isStopReceiving();
	}

	private void notifyEventReceived(String threadName) {
		for (PoolListener p : getPoolListeners()) {
			p.onEventReceived(threadName);
		}
	}

	private void notifyPreDispatchEventListener(String threadName) {
		for (PoolListener p : getPoolListeners()) {
			p.onPreDispatchEvent(threadName);
		}
	}

	private void notifyPostDispatchEventListener(String threadName) {
		for (PoolListener p : getPoolListeners()) {
			p.onPostDispatchEvent(threadName);
		}
	}

	private Collection<PoolListener> getPoolListeners() {
		Collection<PoolListener> listeners;
		synchronized (poolListeners) {
			listeners = Collections.unmodifiableCollection(poolListeners);
		}
		return listeners;
	}

}