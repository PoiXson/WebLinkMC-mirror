package com.poixson.weblinkmc;

import static com.poixson.utils.Utils.SafeClose;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.ServicePriority;

import com.poixson.pluginlib.tools.plugin.xJavaPlugin;
import com.poixson.weblinkmc.TopStats.PlayerStatsDAO;
import com.poixson.weblinkmc.sockets.SocketHandler;
import com.poixson.weblinkmc.sockets.SocketListener;


public class WebLinkPlugin extends xJavaPlugin {
	@Override public int getSpigotPluginID() { return 107954; }
	@Override public int getBStatsID() {       return 17698;  }
	public static final String LOG_PREFIX  = "[WebLink] ";
	public static final String CHAT_PREFIX = ChatColor.AQUA + LOG_PREFIX + ChatColor.WHITE;

	public static int API_PORT = 25511;

	protected final AtomicReference<SocketListener> socketListener = new AtomicReference<SocketListener>(null);
	protected final CopyOnWriteArraySet<SocketHandler> connections = new CopyOnWriteArraySet<SocketHandler>();

	protected final AtomicReference<TopStats> topstats = new AtomicReference<TopStats>(null);



	public WebLinkPlugin() {
		super(WebLinkPlugin.class);
	}



	@Override
	public void onEnable() {
		super.onEnable();
		// top stats
		{
			final TopStats topstats = new TopStats(this);
			final TopStats previous = this.topstats.getAndSet(topstats);
			if (previous != null)
				previous.stop();
			topstats.start();
		}
		// socket listener
		{
			final SocketListener listener = new SocketListener(this, API_PORT);
			final SocketListener previous = this.socketListener.getAndSet(listener);
			if (previous != null)
				SafeClose(previous);
			listener.start();
		}
		// api
		Bukkit.getServicesManager()
			.register(WebLinkPlugin.class, this, this, ServicePriority.Normal);
	}

	@Override
	public void onDisable() {
		super.onDisable();
		// stop listening for connections
		{
			final SocketListener listener = this.socketListener.getAndSet(null);
			SafeClose(listener);
		}
		// close existing connections
		for (int i=0; i<5; i++) {
			final Set<SocketHandler> removing = new HashSet<SocketHandler>();
			for (final SocketHandler client : this.connections) {
				SafeClose(client);
				removing.add(client);
			}
			for (final SocketHandler client : removing) {
				this.connections.remove(client);
			}
			if (this.connections.isEmpty())
				break;
		}
		// top stats
		{
			final TopStats topstats = this.topstats.getAndSet(null);
			if (topstats != null)
				topstats.stop();
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
		SafeClose(client);
	}



	public Map<UUID, PlayerStatsDAO> getTopDistance() {
		final TopStats topstats = this.topstats.get();
		if (topstats == null)
			return null;
		return topstats.top_dist.get();
	}



}
