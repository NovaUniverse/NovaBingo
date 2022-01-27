package net.novauniverse.games.bingo;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.novauniverse.games.bingo.game.Bingo;
import net.novauniverse.games.bingo.game.Debug;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.module.ModuleManager;
import net.zeeraa.novacore.spigot.module.modules.game.GameManager;
import net.zeeraa.novacore.spigot.module.modules.gamelobby.GameLobby;
import net.zeeraa.novacore.spigot.module.modules.multiverse.MultiverseManager;

public class NovaBingo extends JavaPlugin implements Listener {
	private static NovaBingo instance;

	private int gameDuration;
	
	public static NovaBingo getInstance() {
		return instance;
	}

	private Bingo game;

	public Bingo getGame() {
		return game;
	}

	@Override
	public void onLoad() {
		NovaBingo.instance = this;
	}
	
	public int getGameDuration() {
		return gameDuration;
	}

	@Override
	public void onEnable() {
		this.saveDefaultConfig();

		Log.info(getName(), "Loading dependencies");
		ModuleManager.require(GameManager.class);
		ModuleManager.require(MultiverseManager.class);
		ModuleManager.require(GameLobby.class);
		Log.info(getName(), "Dependencies loaded");

		int worldSize = getConfig().getInt("world_size");
		gameDuration = getConfig().getInt("time_minutes");

		Log.debug(getName(), "World size: " + worldSize);
		Log.debug(getName(), "Game duration: " + gameDuration + " minutes");

		Log.info(getName(), "Initialising game");
		this.game = new Bingo(worldSize, gameDuration);
		GameManager.getInstance().loadGame(game);

		Log.info(getName(), "Registering debug commands");
		Debug.registerDebugCommands();

		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Log.success(getName(), "Bingo is ready. Have fun!");
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Plugin) this);
		Bukkit.getScheduler().cancelTasks(this);
	}
}