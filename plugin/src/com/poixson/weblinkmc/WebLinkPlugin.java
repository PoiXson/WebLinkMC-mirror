package com.poixson.weblinkmc;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import com.poixson.commonmc.tools.plugin.xJavaPlugin;
import com.poixson.utils.Utils;
import com.poixson.weblinkmc.sockets.SocketHandler;
import com.poixson.weblinkmc.sockets.SocketListener;


public class WebLinkPlugin extends xJavaPlugin {
	public static final Logger log = Logger.getLogger("Minecraft");
	public static final String LOG_PREFIX  = "[WebLink] ";
	protected static final int SPIGOT_PLUGIN_ID = 0;
	protected static final int BSTATS_PLUGIN_ID = 0;

	public static int API_PORT = 25511;

	protected static final AtomicReference<WebLinkPlugin> instance = new AtomicReference<WebLinkPlugin>(null);

	protected final AtomicReference<SocketListener> socketListener = new AtomicReference<SocketListener>(null);
	protected final CopyOnWriteArraySet<SocketHandler> connections = new CopyOnWriteArraySet<SocketHandler>();



	public WebLinkPlugin() {
		super(WebLinkPlugin.class);
	}



	@Override
	public void onEnable() {
		super.onEnable();
		// socket listener
		{
			final SocketListener listener = new SocketListener(this, API_PORT);
			final SocketListener previous = this.socketListener.getAndSet(listener);
			if (previous != null)
				Utils.SafeClose(previous);
			listener.start();
		}
	}

	@Override
	public void onDisable() {
		super.onDisable();
		// stop listening for connections
		{
			final SocketListener listener = this.socketListener.getAndSet(null);
			Utils.SafeClose(listener);
		}
		// close existing connections
		for (int i=0; i<5; i++) {
			final Set<SocketHandler> removing = new HashSet<SocketHandler>();
			for (final SocketHandler client : this.connections) {
				Utils.SafeClose(client);
				removing.add(client);
			}
			for (final SocketHandler client : removing) {
				this.connections.remove(client);
			}
			if (this.connections.isEmpty())
				break;
		}
	}



	public void register(final Socket socket)
			throws IOException {
		final SocketHandler client = new SocketHandler(this, socket);
		this.register(client);
		client.start();
	}
	public void register(final SocketHandler client) {
		this.connections.add(client);
	}
	public void unregister(final SocketHandler client) {
		this.connections.remove(client);
		Utils.SafeClose(client);
	}



	// -------------------------------------------------------------------------------



	@Override
	protected int getSpigotPluginID() {
		return SPIGOT_PLUGIN_ID;
	}
	@Override
	protected int getBStatsID() {
		return BSTATS_PLUGIN_ID;
	}



}