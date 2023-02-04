package com.poixson.weblinkmc;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import com.poixson.commonmc.tools.plugin.xJavaPlugin;


public class WebLinkPlugin extends xJavaPlugin {
	public static final Logger log = Logger.getLogger("Minecraft");
	public static final String LOG_PREFIX  = "[WebLink] ";
	protected static final int SPIGOT_PLUGIN_ID = 0;
	protected static final int BSTATS_PLUGIN_ID = 0;

	protected static final AtomicReference<WebLinkPlugin> instance = new AtomicReference<WebLinkPlugin>(null);



	public WebLinkPlugin() {
		super(WebLinkPlugin.class);
	}



	@Override
	public void onEnable() {
		super.onEnable();
	}

	@Override
	public void onDisable() {
		super.onDisable();
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