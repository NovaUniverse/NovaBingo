package net.novauniverse.games.bingo.game;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import net.novauniverse.games.bingo.NovaBingo;
import net.novauniverse.games.bingo.game.event.BingoPlayerFindItemEvent;
import net.novauniverse.games.bingo.game.event.BingoTeamCompleteEvent;
import net.novauniverse.games.bingo.game.items.BingoBookItem;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.commons.tasks.Task;
import net.zeeraa.novacore.commons.utils.Callback;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.Game;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameEndReason;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.elimination.PlayerQuitEliminationAction;
import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItemManager;
import net.zeeraa.novacore.spigot.module.modules.multiverse.MultiverseManager;
import net.zeeraa.novacore.spigot.module.modules.multiverse.MultiverseWorld;
import net.zeeraa.novacore.spigot.module.modules.multiverse.PlayerUnloadOption;
import net.zeeraa.novacore.spigot.module.modules.multiverse.WorldOptions;
import net.zeeraa.novacore.spigot.tasks.SimpleTask;
import net.zeeraa.novacore.spigot.teams.Team;
import net.zeeraa.novacore.spigot.teams.TeamManager;
import net.zeeraa.novacore.spigot.utils.LocationUtils;
import net.zeeraa.novacore.spigot.utils.PlayerUtils;
import net.zeeraa.novacore.spigot.utils.RandomFireworkEffect;
import net.zeeraa.novacore.spigot.world.worldgenerator.worldpregenerator.WorldPreGenerator;

public class Bingo extends Game implements Listener {
	private boolean started;
	private boolean ended;

	private int timeMinutes;

	private MultiverseWorld multiverseWorld;
	private WorldPreGenerator worldPreGenerator;

	private Task checkTask;

	private int worldSizeChunks;

	private List<Material> materialToFind;
	private Map<Team, List<Material>> teamMaterialToFind;

	private int teamsCompleted;

	private LocalDateTime end;

	/**
	 * Init the game object
	 * 
	 * @param worldSizeChunks The size of the world
	 * @param timeMinutes     Time in munutes that the game should run
	 */
	public Bingo(int worldSizeChunks, int timeMinutes) {
		super(NovaBingo.getInstance());
		
		this.worldSizeChunks = worldSizeChunks;
		this.timeMinutes = timeMinutes;
	}

	public Map<Team, List<Material>> getTeamMaterialToFind() {
		return teamMaterialToFind;
	}

	public List<Material> getMaterialToFind() {
		return materialToFind;
	}

	public LocalDateTime getEnd() {
		return end;
	}

	public int getTeamsLeft() {
		int count = 0;

		for (Team key : teamMaterialToFind.keySet()) {
			if (teamMaterialToFind.get(key).size() > 0) {
				count++;
			}
		}

		return count;
	}

	/**
	 * Get time left in seconds
	 * 
	 * @return seconds left
	 */
	public long getTimeLeft() {
		if (!started) {
			return 0L;
		}

		return ChronoUnit.SECONDS.between(LocalDateTime.now(), end);
	}

	public Location tryGetSpawnLocation() {
		int max = (worldSizeChunks * 16) - 100;

		Random random = new Random();
		int x = max - random.nextInt(max * 2);
		int z = max - random.nextInt(max * 2);

		Log.trace("Bingo#tryGetSpawnLocation()", "Trying location X: " + x + " Z: " + z);

		Location location = new Location(world, x, 256, z);

		for (int i = 256; i > 2; i++) {
			location.setY(location.getY() - 1);

			Block b = location.clone().add(0, -1, 0).getBlock();

			if (b.getType() != Material.AIR) {
				if (b.getType() == Material.LAVA || b.getType() == Material.STATIONARY_LAVA || b.getType() == Material.STATIONARY_WATER || b.getType() == Material.WATER) {
					break;
				}

				if (b.getType().isSolid()) {
					return location;
				}
			}
		}

		return null;
	}

	@Override
	public void onLoad() {
		started = false;
		ended = false;
		teamsCompleted = 0;

		materialToFind = new ArrayList<Material>();
		teamMaterialToFind = new HashMap<Team, List<Material>>();

		WorldOptions options = new WorldOptions("bingo");

		options.generateStructures(true);
		options.setPlayerUnloadOption(PlayerUnloadOption.KICK);
		options.withEnvironment(Environment.NORMAL);

		multiverseWorld = MultiverseManager.getInstance().createWorld(options);
		this.world = multiverseWorld.getWorld();

		worldPreGenerator = new WorldPreGenerator(world, worldSizeChunks + 10, 32, 1, new Callback() {
			@Override
			public void execute() {
				Bukkit.getServer().broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "The world has been generated");

				world.getWorldBorder().setSize((worldSizeChunks * 16) * 2);
				world.getWorldBorder().setCenter(0.5, 0.5);
				world.getWorldBorder().setWarningDistance(20);
				world.getWorldBorder().setDamageBuffer(5);
				world.getWorldBorder().setDamageAmount(5);

				Log.debug("World name: " + world.getName());
				Log.debug("Border size: " + world.getWorldBorder().getSize());
			}
		});
		worldPreGenerator.start();

		// Game rules
		world.setGameRuleValue("keepInventory", "true");

		checkTask = new SimpleTask(NovaBingo.getInstance(), new Runnable() {
			@Override
			public void run() {
				check();
			}
		}, 20L);
	}

	private void check() {
		if (!started || ended) {
			Log.warn("Bing", "Bingo#check() called before start or after end");
			return;
		}

		if (LocalDateTime.now().isAfter(end)) {
			Task.tryStopTask(checkTask);

			this.endGame(GameEndReason.TIME);

			return;
		}

		Bukkit.getServer().getOnlinePlayers().forEach(player -> {
			player.setFoodLevel(20);
			player.setSaturation(20);
		});

		Bukkit.getServer().getOnlinePlayers().forEach(player -> {
			if (players.contains(player.getUniqueId())) {
				Team team = TeamManager.getTeamManager().getPlayerTeam(player);
				if (team != null) {
					if (teamMaterialToFind.containsKey(team)) {
						List<Material> toFind = teamMaterialToFind.get(team);

						if (toFind.size() > 0) {
							for (ItemStack item : player.getInventory().getContents()) {
								if (item != null) {
									if (toFind.contains(item.getType())) {
										// Completed

										Log.debug("Bingo", "Team " + team.getDisplayName() + " item " + item.getType().name());

										toFind.remove(item.getType());

										Event e = new BingoPlayerFindItemEvent(player, team, item.getType(), toFind);
										Bukkit.getServer().getPluginManager().callEvent(e);
									}
								}
							}
						}
					}
				}
			}
		});

		List<Team> completed = new ArrayList<>();

		teamMaterialToFind.keySet().forEach(team -> {
			if (teamMaterialToFind.get(team).size() == 0) {
				completed.add(team);

				team.getMembers().forEach(uuid -> {
					Player pl = Bukkit.getServer().getPlayer(uuid);
					if (pl != null) {
						if (pl.isOnline()) {
							pl.setGameMode(GameMode.SPECTATOR);

							this.spawnFirework(pl.getLocation());
						}
					}
				});

				Log.debug("Bingo", "Team " + team.getDisplayName() + " finished all items");

				teamsCompleted++;
				Event e = new BingoTeamCompleteEvent(team, teamsCompleted);
				Bukkit.getServer().getPluginManager().callEvent(e);
			}
		});

		completed.forEach(team -> teamMaterialToFind.remove(team));

		if (teamMaterialToFind.size() == 0) {
			Task.tryStartTask(checkTask);
			Bukkit.getServer().broadcastMessage(ChatColor.BOLD + "" + ChatColor.GREEN + "Game over. All players finished");
			this.endGame(GameEndReason.ALL_FINISHED);
		}
	}

	private void spawnFirework(Location location) {
		Firework fw = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
		FireworkMeta fwm = fw.getFireworkMeta();

		fwm.setPower(2);
		fwm.addEffect(RandomFireworkEffect.randomFireworkEffect());

		fw.setFireworkMeta(fwm);
	}

	@Override
	public String getName() {
		return "bingo";
	}

	@Override
	public String getDisplayName() {
		return "Bingo";
	}

	@Override
	public PlayerQuitEliminationAction getPlayerQuitEliminationAction() {
		return PlayerQuitEliminationAction.NONE;
	}

	@Override
	public boolean eliminatePlayerOnDeath(Player player) {
		return false;
	}

	@Override
	public boolean isPVPEnabled() {
		return false;
	}

	@Override
	public boolean autoEndGame() {
		return false;
	}

	@Override
	public boolean hasStarted() {
		return started;
	}

	@Override
	public boolean hasEnded() {
		return ended;
	}

	@Override
	public boolean isFriendlyFireAllowed() {
		return false;
	}

	@Override
	public boolean canAttack(LivingEntity attacker, LivingEntity target) {
		return true;
	}

	@Override
	public boolean canStart() {
		return worldPreGenerator.isFinished();
	}

	@Override
	public void onStart() {
		if (started) {
			return;
		}

		end = LocalDateTime.now().plusMinutes(timeMinutes);

		this.materialToFind = BingoItemGenerator.generateItems();
		
		Bukkit.getServer().getOnlinePlayers().forEach(player -> {
			player.getInventory().clear();

			if (!players.contains(player.getUniqueId())) {
				player.setGameMode(GameMode.SPECTATOR);
				player.teleport(this.getWorld().getSpawnLocation());
				return;
			}

			Team team = TeamManager.getTeamManager().getPlayerTeam(player);

			if (team == null) {
				Log.error("Bingo", "Cant add player " + player.getName() + " since they dont have a team");
				player.teleport(world.getSpawnLocation());
				player.setGameMode(GameMode.SPECTATOR);
				player.sendMessage(ChatColor.RED + "You are not in a team. Cant assign item set for you");
				return;
			}

			if (!teamMaterialToFind.containsKey(team)) {
				List<Material> toFind = new ArrayList<>();

				materialToFind.forEach(material -> toFind.add(material));

				teamMaterialToFind.put(team, toFind);
			}

			Location spawnLocation = null;

			for (int i = 0; i < 20000; i++) {
				spawnLocation = this.tryGetSpawnLocation();
				if (spawnLocation != null) {
					break;
				}
			}

			if (spawnLocation == null) {
				player.teleport(world.getSpawnLocation());
				player.setGameMode(GameMode.SPECTATOR);
				player.sendMessage(ChatColor.RED + "Failed to find a spawn location for you. Please tell staff about this and they will respawn you");
				return;
			}

			ItemStack bingoBook = CustomItemManager.getInstance().getCustomItemStack(BingoBookItem.class, player);

			final Location finalSpawnLocation = LocationUtils.centerLocation(spawnLocation);

			PlayerUtils.resetMaxHealth(player);
			PlayerUtils.resetPlayerXP(player);

			player.setBedSpawnLocation(finalSpawnLocation, true);
			player.teleport(finalSpawnLocation);
			player.setGameMode(GameMode.SURVIVAL);
			player.setFoodLevel(20);
			player.setFallDistance(0F);
			player.setHealth(20D);
			player.setSaturation(20F);
			player.getInventory().clear();
			player.getInventory().setItem(8, bingoBook);
			
			player.sendMessage(ChatColor.GREEN + "Gather all 9 Items to Win!");
			player.sendMessage(ChatColor.GREEN + "Use /teamtp to teleport to your teammates");
			player.sendMessage(ChatColor.GREEN + "Good luck!");

			new BukkitRunnable() {
				@Override
				public void run() {
					player.teleport(finalSpawnLocation);
				}
			}.runTaskLater(NovaBingo.getInstance(), 10L);

		});

		world.setThundering(false);
		world.setTime(1000L);

		Task.tryStartTask(checkTask);

		started = true;

		this.sendBeginEvent();
	}

	@Override
	public void onEnd(GameEndReason reason) {
		if (!started) {
			return;
		}

		if (ended) {
			return;
		}

		Bukkit.getServer().getOnlinePlayers().forEach(p -> {
			p.setGameMode(GameMode.SPECTATOR);
			p.getInventory().clear();

			Team team = TeamManager.getTeamManager().getPlayerTeam(p);
			if (team != null) {
				if (!teamMaterialToFind.containsKey(team)) {
					this.spawnFirework(p.getLocation());
					p.playSound(p.getLocation(), Sound.LEVEL_UP, 1F, 1F);
				} else {
					p.playSound(p.getLocation(), Sound.BLAZE_HIT, 1F, 1F);
				}
			}
		});

		Task.tryStopTask(checkTask);

		ended = true;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Team team = TeamManager.getTeamManager().getPlayerTeam(e.getPlayer());
		boolean isInGame = false;

		if (team != null) {
			if (teamMaterialToFind.containsKey(team)) {
				isInGame = true;
			}
		}

		if (!isInGame) {
			e.getPlayer().setGameMode(GameMode.SPECTATOR);
			e.getPlayer().teleport(this.getWorld().getSpawnLocation());
		}
	}
}