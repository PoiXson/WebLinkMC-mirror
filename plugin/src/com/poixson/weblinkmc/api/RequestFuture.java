package com.poixson.weblinkmc.api;

import static com.poixson.utils.Utils.GetMS;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.tools.xTime;
import com.poixson.weblinkmc.WebLinkPlugin;


public abstract class RequestFuture extends BukkitRunnable implements Future<String>, Callable<String> {

	protected final WebLinkPlugin plugin;

	protected final AtomicReference<String> result = new AtomicReference<String>(null);
	protected final CopyOnWriteArraySet<Thread> waiters = new CopyOnWriteArraySet<Thread>();




	public RequestFuture(final WebLinkPlugin plugin) {
		this.plugin = plugin;
		this.runTask(plugin);
	}



	@Override
	public void run() {
		try {
			final String result = this.call();
			this.result.set(result);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		// wait waiting threads
		for (int i=0; i<5; i++) {
			final Set<Thread> removing = new HashSet<Thread>();
			for (final Thread t : this.waiters) {
				LockSupport.unpark(t);
				removing.add(t);
			}
			for (final Thread t : removing) {
				this.waiters.remove(t);
			}
			if (this.waiters.isEmpty())
				break;
		}
	}
	@Override
	public abstract String call() throws Exception;



	@Override
	public String get() throws InterruptedException, ExecutionException {
		try {
			return this.get(-1L, null);
		} catch (TimeoutException ignore) {}
		return null;
	}
	@Override
	public String get(final long timeout, final TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		// ready
		{
			final String result = this.result.get();
			if (result != null)
				return result;
		}
		// wait
		this.waiters.add(Thread.currentThread());
		{
			final String result = this.result.get();
			if (result != null)
				return result;
		}
		final long start = GetMS();
		final xTime xtimeout = new xTime(timeout, unit);
		while (true) {
			// timeout
			if (timeout > 0L) {
				if (GetMS() - start >= xtimeout.ms())
					return null;
			}
			final String result = this.result.get();
			if (result != null)
				return result;
			LockSupport.parkNanos(this.waiters, 10000000L); // 10ms
		}
	}



	@Override
	public boolean isDone() {
		return (this.result.get() != null);
	}
	@Override
	public boolean cancel(final boolean mayInterrupt) {
		this.cancel();
		return !this.isDone();
	}



}
