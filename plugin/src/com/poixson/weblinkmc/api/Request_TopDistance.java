package com.poixson.weblinkmc.api;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.google.gson.Gson;
import com.poixson.weblinkmc.TopStats.PlayerStatsDAO;
import com.poixson.weblinkmc.WebLinkPlugin;


public class Request_TopDistance extends RequestFuture {



	public Request_TopDistance(final WebLinkPlugin plugin) {
		super(plugin);
	}



	@Override
	public String call() throws Exception {
		final TreeMap<String, Integer> topdist = new TreeMap<String, Integer>();
		final Map<UUID, PlayerStatsDAO> top = this.plugin.getTopDistance();
		final Iterator<Entry<UUID, PlayerStatsDAO>> it = top.entrySet().iterator();
		while (it.hasNext()) {
			final Entry<UUID, PlayerStatsDAO> entry = it.next();
			final OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getKey());
			final String name = player.getName();
			final int distance = entry.getValue().distance;
			topdist.put(name, Integer.valueOf(distance));
		}
		final Map<String, Object> map = new HashMap<String, Object>();
		map.put("topdistance", topdist);
		return (new Gson()).toJson(map);
	}



}
