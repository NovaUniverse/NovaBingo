package net.novauniverse.games.bingo.game.items;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.yoshiplex.rainbow.RainbowText;

import net.zeeraa.novacore.spigot.module.modules.customitems.CustomItem;
import net.zeeraa.novacore.spigot.utils.ItemBuilder;

public class BingoBookItem extends CustomItem {
	@Override
	protected ItemStack createItemStack(Player player) {
		ItemBuilder builder = new ItemBuilder(Material.WRITTEN_BOOK);
		builder.setName("Bingo Items");
		builder.addLore(ChatColor.WHITE + "Right click to see");
		builder.addLore(ChatColor.WHITE + "all items you need to find");

		ItemStack stack = builder.build();

		BookMeta meta = (BookMeta) stack.getItemMeta();

		meta.setAuthor("NovaUniverse");
		meta.addPage("How did you get in here?");
		meta.setTitle(ChatColor.GREEN + "Bingo Items");

		if (player != null) {
			if (player.getUniqueId().toString().equalsIgnoreCase("5203face-89ca-49b7-a5a0-f2cf0fe230e7")) {
				String specialTitle = new RainbowText("Bingo items").getText();
				meta.setDisplayName(specialTitle);
				meta.setTitle(specialTitle);
			}

			if (player.getUniqueId().toString().equalsIgnoreCase("866a6931-a503-48b1-9d6f-0dde92c05918") || player.getUniqueId().toString().equalsIgnoreCase("45c49c88-950c-4f4b-afe0-55ca5d0593d8")) {
				meta.setDisplayName(ChatColor.DARK_RED + "Bingo Items");
				meta.setAuthor(ChatColor.DARK_RED + "Walter");
				meta.setTitle(ChatColor.DARK_RED + "Bingo Items");
			}
		}

		stack.setItemMeta(meta);

		return stack;
	}

	@Override
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			event.setCancelled(true);
			event.getPlayer().performCommand("bingo");
		}
	}

	@Override
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		event.setCancelled(true);
	}

	@Override
	public void onInventoryClick(InventoryClickEvent event) {
		event.setCancelled(true);
	}
}