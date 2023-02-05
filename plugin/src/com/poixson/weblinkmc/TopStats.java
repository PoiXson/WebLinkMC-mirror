package com.poixson.weblinkmc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.poixson.tools.abstractions.xStartStop;


public class TopStats extends BukkitRunnable implements xStartStop {

	public static final int UPDATE_SECONDS = 60;

	protected final WebLinkPlugin plugin;

	public final ConcurrentHashMap<UUID, PlayerStatsDAO> stats = new ConcurrentHashMap<UUID, PlayerStatsDAO>();
	public final AtomicReference<Map<UUID, PlayerStatsDAO>> top_dist = new AtomicReference<Map<UUID, PlayerStatsDAO>>(null);



	public class PlayerStatsDAO {
		public final int distance;
		public final int timeplayed;

		public PlayerStatsDAO(final int distance, final int timeplayed) {
			this.distance   = distance;
			this.timeplayed = timeplayed;
		}

	}



	public TopStats(final WebLinkPlugin plugin) {
		this.plugin = plugin;
		for (final OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			this.update(player);
		}
		this.updateTop();
	}



	@Override
	public void start() {
		this.runTaskTimer(this.plugin, UPDATE_SECONDS * 20, UPDATE_SECONDS * 20);
	}
	@Override
	public void stop() {
		try {
			this.cancel();
		} catch (IllegalStateException ignore) {}
	}



	@Override
	public void run() {
		for (final Player player : Bukkit.getOnlinePlayers()) {
			this.update(player);
		}
		this.updateTop();
	}



	public void update(final OfflinePlayer player) {
		final UUID uuid = player.getUniqueId();
		final int distance =
			player.getStatistic(Statistic.WALK_ONE_CM            ) +
			player.getStatistic(Statistic.SPRINT_ONE_CM          ) +
			player.getStatistic(Statistic.CROUCH_ONE_CM          ) +
			player.getStatistic(Statistic.CLIMB_ONE_CM           ) +
			player.getStatistic(Statistic.SWIM_ONE_CM            ) +
			player.getStatistic(Statistic.WALK_ON_WATER_ONE_CM   ) +
			player.getStatistic(Statistic.WALK_UNDER_WATER_ONE_CM);
		final int timeplayed = player.getStatistic(Statistic.PLAY_ONE_MINUTE) / 20;
		// compare to existing
		{
			final PlayerStatsDAO stats = this.stats.get(uuid);
			if (stats != null) {
				if (stats.distance   == distance
				&&  stats.timeplayed == timeplayed)
					return;
			}
		}
		// update stats
		{
			final PlayerStatsDAO stats = new PlayerStatsDAO(distance, timeplayed);
			this.stats.put(uuid, stats);
		}
	}

	public void updateTop() {
		(new BukkitRunnable() {
			@Override
			public void run() {
				final int topcount = 5;
				int  lowest = Integer.MAX_VALUE;
				UUID lowest_uuid = null;
				final HashMap<UUID, PlayerStatsDAO> top = new HashMap<UUID, PlayerStatsDAO>();
				final Iterator<Entry<UUID, PlayerStatsDAO>> it = TopStats.this.stats.entrySet().iterator();
				while (it.hasNext()) {
					final Entry<UUID, PlayerStatsDAO> entry = it.next();
					final UUID          uuid = entry.getKey();
					final PlayerStatsDAO dao = entry.getValue();
					// fill first few
					if (top.size() < topcount) {
						top.put(uuid, dao);
						if (top.size() == topcount) {
							// find the lowest
							final Iterator<Entry<UUID, PlayerStatsDAO>> itL = top.entrySet().iterator();
							while (itL.hasNext()) {
								final Entry<UUID, PlayerStatsDAO> entryL = itL.next();
								if (lowest > entryL.getValue().distance) {
									lowest = entryL.getValue().distance;
									lowest_uuid = entryL.getKey();
								}
							}
						}
						continue;
					}
					// new top found
					if (lowest < dao.distance) {
						top.put(uuid, dao);
						// find the lowest to remove
						lowest = Integer.MAX_VALUE;
						lowest_uuid = null;
						final Iterator<Entry<UUID, PlayerStatsDAO>> itR = top.entrySet().iterator();
						while (itR.hasNext()) {
							final Entry<UUID, PlayerStatsDAO> entryR = itR.next();
							if (lowest > entryR.getValue().distance) {
								lowest = entryR.getValue().distance;
								lowest_uuid = entryR.getKey();
							}
						}
						top.remove(lowest_uuid);
						// find the lowest again
						lowest = Integer.MAX_VALUE;
						lowest_uuid = null;
						final Iterator<Entry<UUID, PlayerStatsDAO>> itL = top.entrySet().iterator();
						while (itL.hasNext()) {
							final Entry<UUID, PlayerStatsDAO> entryL = itL.next();
							if (lowest > entryL.getValue().distance) {
								lowest = entryL.getValue().distance;
								lowest_uuid = entryL.getKey();
							}
						}
					} // end new top found
				} // end loop
				TopStats.this.top_dist.set(top);
			}
		}).runTaskAsynchronously(this.plugin);
	}



}
