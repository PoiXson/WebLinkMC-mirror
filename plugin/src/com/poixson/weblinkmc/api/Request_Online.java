package com.poixson.weblinkmc.api;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.poixson.weblinkmc.WebLinkPlugin;


public class Request_Online extends RequestFuture {



	public Request_Online(final WebLinkPlugin plugin) {
		super(plugin);
	}



	@Override
	public String call() throws Exception {
		final LinkedList<String> online = new LinkedList<String>();
		for (final Player player : Bukkit.getOnlinePlayers()) {
			online.add(player.getName());
		}
		final Map<String, Object> map = new HashMap<String, Object>();
		map.put("online", online);
		return (new Gson()).toJson(map);
	}



}
