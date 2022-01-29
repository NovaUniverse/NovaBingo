package net.novauniverse.games.bingo.game.commands.bingo;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;

import net.novauniverse.games.bingo.NovaBingo;
import net.novauniverse.games.bingo.game.BingoItems;
import net.zeeraa.novacore.spigot.abstraction.VersionIndependantUtils;
import net.zeeraa.novacore.spigot.abstraction.enums.ColoredBlockType;
import net.zeeraa.novacore.spigot.command.AllowedSenders;
import net.zeeraa.novacore.spigot.command.NovaCommand;
import net.zeeraa.novacore.spigot.module.modules.gui.holders.GUIReadOnlyHolder;
import net.zeeraa.novacore.spigot.teams.Team;
import net.zeeraa.novacore.spigot.teams.TeamManager;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;

public class BingoCommand extends NovaCommand {

	public BingoCommand(Plugin plugin) {
		super("bingo", plugin);

		setAllowedSenders(AllowedSenders.PLAYERS);
		setPermission("novabingo.commands.bingo");
		setPermissionDefaultValue(PermissionDefault.TRUE);
		setFilterAutocomplete(true);
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		Player target = (Player) sender;

		if (args.length > 0) {
			if (sender.hasPermission("novabingo.commands.bingo.others")) {
				target = Bukkit.getServer().getPlayer(args[0]);

				if (target == null) {
					sender.sendMessage(ChatColor.RED + "Cant find player " + args[0]);
					return true;
				}

				if (!target.isOnline()) {
					sender.sendMessage(ChatColor.RED + "That player is offline");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Only staff can check the items of other players");
				return true;
			}
		}

		Team team = TeamManager.getTeamManager().getPlayerTeam(target);
		if (team == null) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "You are not in a team");
			} else {
				sender.sendMessage(ChatColor.RED + target.getName() + "is not in a team");
			}

			return true;
		}

		if (!NovaBingo.getInstance().getGame().hasStarted()) {
			sender.sendMessage(ChatColor.RED + "The game has not yet started");
			return true;
		}

		if (NovaBingo.getInstance().getGame().hasEnded()) {
			sender.sendMessage(ChatColor.RED + "The game has ended");
			return true;
		}

		Inventory gui = Bukkit.createInventory(new GUIReadOnlyHolder(), 9 * 3, args.length > 0 ? target.getName() + "´s bingo items" : "Bingo items");

		ItemStack background = new ItemBuilder(VersionIndependantUtils.get().getColoredItem(DyeColor.RED, ColoredBlockType.GLASS_PANE)).setAmount(1).setName(ChatColor.RED + "Not completed").build();
		ItemStack backgroundCompleted = new ItemBuilder(VersionIndependantUtils.get().getColoredItem(DyeColor.LIME, ColoredBlockType.GLASS_PANE)).setAmount(1).setName(ChatColor.GREEN + "Completed").build();

		for (int i = 0; i < NovaBingo.getInstance().getGame().getMaterialToFind().size(); i++) {
			Material material = NovaBingo.getInstance().getGame().getMaterialToFind().get(i);
			String itemName = BingoItems.getMaterialDisplayName(material);

			boolean completed = true;

			if (NovaBingo.getInstance().getGame().getTeamMaterialToFind().containsKey(team)) {
				if (NovaBingo.getInstance().getGame().getTeamMaterialToFind().get(team).contains(material)) {
					completed = false;
				}
			}

			ItemBuilder iconBuilder = new ItemBuilder(material);

			if (completed) {
				iconBuilder.addLore(ChatColor.GREEN + "This item has been found");
			} else {
				iconBuilder.addLore(ChatColor.WHITE + "Craft or find this item");
				iconBuilder.addLore(ChatColor.WHITE + "to complete this");
			}

			iconBuilder.setName((completed ? ChatColor.GREEN : ChatColor.RED) + itemName);

			int top = i;
			int middle = 9 + i;
			int bottom = (9 * 2) + i;

			gui.setItem(top, completed ? backgroundCompleted : background);
			gui.setItem(bottom, completed ? backgroundCompleted : background);

			gui.setItem(middle, iconBuilder.build());

		}

		((Player) sender).openInventory(gui);

		return true;
	}
}