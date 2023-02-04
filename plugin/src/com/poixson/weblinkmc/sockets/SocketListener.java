package com.poixson.weblinkmc.sockets;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import com.poixson.utils.Utils;
import com.poixson.weblinkmc.WebLinkPlugin;


public class SocketListener extends Thread implements Closeable {
	protected static final Logger log = WebLinkPlugin.log;
	protected static final String LOG_PREFIX = WebLinkPlugin.LOG_PREFIX;

	protected final WebLinkPlugin plugin;
	protected final AtomicReference<ServerSocket> listener = new AtomicReference<ServerSocket>(null);

	protected final int port;

	protected final AtomicBoolean running  = new AtomicBoolean(false);
	protected final AtomicBoolean stopping = new AtomicBoolean(false);



	public SocketListener(final WebLinkPlugin plugin, final int port) {
		this.plugin = plugin;
		this.port   = port;
	}



	@Override
	public void run() {
		if (!this.running.compareAndSet(false, true))
			throw new RuntimeException("Socket listener thread already running?");
		try {
			final ServerSocket listener = new ServerSocket(this.port);
			final ServerSocket previous = this.listener.getAndSet(listener);
			if (previous != null)
				Utils.SafeClose(previous);
//			listener.bind(null);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		log.info(LOG_PREFIX + "Listening for socket connections..");
		while (true) {
			if (this.stopping.get())
				break;
			try {
				final ServerSocket listener = this.listener.get();
				final Socket client = listener.accept();
				log.info(LOG_PREFIX + "Connection from: " + client.getRemoteSocketAddress().toString());
				this.plugin.register(client);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		log.info(LOG_PREFIX + "Socket listener stopped");
		Utils.SafeClose(this.listener.get());
		this.running.set(false);
	}
	@Override
	public void close() throws IOException {
		this.stopping.set(true);
		Utils.SafeClose(this.listener.get());
	}



	public boolean isRunning() {
		return this.running.get();
	}
	public boolean isStopping() {
		return this.stopping.get();
	}



}
