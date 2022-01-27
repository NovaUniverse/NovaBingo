package net.novauniverse.games.bingo.game.event;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.novauniverse.games.bingo.game.BingoItems;
import net.zeeraa.novacore.spigot.teams.Team;

public class BingoPlayerFindItemEvent extends Event {
	private static final HandlerList HANDLERS_LIST = new HandlerList();

	private Player player;
	private Team team;
	private Material material;
	private List<Material> itemsLeft;
	private String itemDisplayName;

	public BingoPlayerFindItemEvent(Player player, Team team, Material material, List<Material> itemsLeft) {
		this.player = player;
		this.team = team;
		this.material = material;
		this.itemsLeft = itemsLeft;
		this.itemDisplayName = BingoItems.getMaterialDisplayName(material);
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}

	public Player getPlayer() {
		return player;
	}

	public Team getTeam() {
		return team;
	}

	public List<Material> getItemsLeft() {
		return itemsLeft;
	}

	public Material getMaterial() {
		return material;
	}

	public String getItemDisplayName() {
		return itemDisplayName;
	}
}