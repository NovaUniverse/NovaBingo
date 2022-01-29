package net.novauniverse.games.bingo.game.debug;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import net.novauniverse.games.bingo.NovaBingo;
import net.zeeraa.novacore.commons.log.Log;
import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.debug.DebugCommandRegistrator;
import net.zeeraa.novacore.spigot.debug.DebugTrigger;
import net.zeeraa.novacore.spigot.teams.Team;
import net.zeeraa.novacore.spigot.teams.TeamManager;

public class Debug {
	private static boolean isRegistered = false;

	public static final void registerDebugCommands() {
		if (isRegistered) {
			Log.warn("Debug#registerDebugCommands() called twice");
			return;
		}

		isRegistered = true;

		DebugCommandRegistrator.getInstance().addDebugTrigger(new DebugTrigger() {
			@Override
			public void onExecute(CommandSender sender, String commandLabel, String[] args) {
				sender.sendMessage(ChatColor.GREEN + "Target items (" + NovaBingo.getInstance().getGame().getMaterialToFind().size() + "):");
				NovaBingo.getInstance().getGame().getMaterialToFind().forEach(m -> sender.sendMessage(ChatColor.AQUA + m.name()));
				sender.sendMessage(ChatColor.GREEN + "END OF ITEMS");
			}

			@Override
			public PermissionDefault getPermissionDefault() {
				return PermissionDefault.OP;
			}

			@Override
			public String getPermission() {
				return "novabingo.debug";
			}

			@Override
			public String getName() {
				return "dumpitems";
			}

			@Override
			public AllowedSenders getAllowedSenders() {
				return AllowedSenders.ALL;
			}
		});

		DebugCommandRegistrator.getInstance().addDebugTrigger(new DebugTrigger() {
			@Override
			public void onExecute(CommandSender sender, String commandLabel, String[] args) {
				Team team = TeamManager.getTeamManager().getPlayerTeam((Player) sender);

				if (team == null) {
					sender.sendMessage(ChatColor.RED + "No team");
					return;
				}

				List<Material> teamItems = NovaBingo.getInstance().getGame().getTeamMaterialToFind().get(team);

				if (teamItems == null) {
					sender.sendMessage(ChatColor.RED + "No assigned items");
					return;
				}

				sender.sendMessage(ChatColor.GREEN + "Target items (" + teamItems.size() + "):");
				teamItems.forEach(m -> sender.sendMessage(ChatColor.AQUA + m.name()));
				sender.sendMessage(ChatColor.GREEN + "END OF ITEMS");
			}

			@Override
			public PermissionDefault getPermissionDefault() {
				return PermissionDefault.OP;
			}

			@Override
			public String getPermission() {
				return "novabingo.debug";
			}

			@Override
			public String getName() {
				return "myitems";
			}

			@Override
			public AllowedSenders getAllowedSenders() {
				return AllowedSenders.PLAYERS;
			}
		});

		Log.trace("Debug", "Registered debug triggers");
	}
}