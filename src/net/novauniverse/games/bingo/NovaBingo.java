package net.novauniverse.games.bingo;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import net.novauniverse.games.bingo.game.Bingo;
import net.novauniverse.games.bingo.game.commands.bingo.BingoCommand;
import net.novauniverse.games.bingo.game.commands.teamtp.TeamTpCommand;
import net.novauniverse.games.bingo.game.debug.Debug;
import net.novauniverse.games.bingo.game.items.BingoBookItem;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.command.CommandRegistry;
import net.zeeraa.novacore.spigot.gameengine.module.modules.game.GameManager;
import net.zeeraa.novacore.spigot.gameengine.module.modules.gamelobby.GameLobby;
import net.zeeraa.novacore.spigot.module.ModuleManager;
import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItemManager;
import net.zeeraa.novacore.spigot.module.modules.gui.GUIManager;
import net.zeeraa.novacore.spigot.module.modules.multiverse.MultiverseManager;
import net.zeeraa.novacore.spigot.permission.PermissionRegistrator;

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
		ModuleManager.require(GUIManager.class);
		ModuleManager.require(CustomItemManager.class);
		Log.info(getName(), "Dependencies loaded");

		try {
			CustomItemManager.getInstance().addCustomItem(BingoBookItem.class);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			Log.fatal("Bingo", "Failed to register custom items. " + e.getClass().getName() + " " + e.getMessage());
			e.printStackTrace();
			Bukkit.getServer().getPluginManager().disablePlugin(this);
			return;
		}

		int worldSize = getConfig().getInt("world_size");
		gameDuration = getConfig().getInt("time_minutes");

		Log.debug(getName(), "World size: " + worldSize);
		Log.debug(getName(), "Game duration: " + gameDuration + " minutes");

		Log.info(getName(), "Initialising game");
		this.game = new Bingo(worldSize, gameDuration);
		GameManager.getInstance().loadGame(game);

		Log.info(getName(), "Registering debug commands");
		Debug.registerDebugCommands();

		Log.info(getName(), "Registering commands and permissions");
		PermissionRegistrator.registerPermission("novabingo.commands.bingo.others", "Check the bingo items of other players", PermissionDefault.OP);
		
		CommandRegistry.registerCommand(new BingoCommand(this));
		CommandRegistry.registerCommand(new TeamTpCommand(this));

		Bukkit.getServer().getPluginManager().registerEvents(this, this);
		Log.success(getName(), "Bingo is ready. Have fun!");
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Plugin) this);
		Bukkit.getScheduler().cancelTasks(this);
	}
}