package net.novauniverse.games.bingo.game.commands.teamtp;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import net.zeeraa.novacore.commons.utils.UUIDUtils;
import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.command.NovaCommand;
import net.zeeraa.novacore.spigot.teams.Team;
import net.zeeraa.novacore.spigot.teams.TeamManager;

public class TeamTpCommand extends NovaCommand {

	public TeamTpCommand(Plugin plugin) {
		super("teamtp", plugin);

		setAllowedSenders(AllowedSenders.PLAYERS);
		setPermission("novabingo.commands.teamtp");
		setPermissionDefaultValue(PermissionDefault.TRUE);
		setFilterAutocomplete(true);
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		Player player = (Player) sender;
		Team team = TeamManager.getTeamManager().getPlayerTeam(player);

		if (team == null) {
			sender.sendMessage(ChatColor.RED + "You are not in a team");
			return true;
		}

		List<Player> otherPlayers = new ArrayList<>();

		Bukkit.getServer().getOnlinePlayers().forEach(p2 -> {
			Team team2 = TeamManager.getTeamManager().getPlayerTeam(p2);

			if (team2 != null) {
				if (team2.equals(team)) {
					if (!UUIDUtils.isSame(player.getUniqueId(), p2.getUniqueId())) {
						otherPlayers.add(p2);
					}
				}
			}
		});

		if (otherPlayers.size() == 0 && args.length == 0) {
			sender.sendMessage(ChatColor.RED + "There are no other online players in you team");
		} else if (otherPlayers.size() == 1 && args.length == 0) {
			Player target = otherPlayers.get(0);

			sender.sendMessage(ChatColor.GREEN + "Teleporting to " + target.getName());
			player.teleport(target);
		} else {
			if (args.length > 0) {
				Player target = null;
				for (Player p2 : otherPlayers) {
					if (p2.getName().equalsIgnoreCase(args[0])) {
						target = p2;
					}
				}

				if (target == null) {
					sender.sendMessage(ChatColor.RED + "That player is either not online or not in your team");
				} else {
					sender.sendMessage(ChatColor.GREEN + "Teleporting to " + target.getName());
					player.teleport(target);
				}
			} else {
				sender.sendMessage(ChatColor.RED + "There are multiple players in your team online. Please provide the player you want to teleport to");
			}
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
		List<String> result = new ArrayList<>();

		Player player = (Player) sender;
		Team team = TeamManager.getTeamManager().getPlayerTeam(player);

		if (team != null) {
			Bukkit.getServer().getOnlinePlayers().forEach(p2 -> {
				Team team2 = TeamManager.getTeamManager().getPlayerTeam(p2);

				if (team2 != null) {
					if (team2.equals(team)) {
						if (!UUIDUtils.isSame(player.getUniqueId(), p2.getUniqueId())) {
							result.add(p2.getName());
						}
					}
				}
			});
		}

		return result;
	}
}