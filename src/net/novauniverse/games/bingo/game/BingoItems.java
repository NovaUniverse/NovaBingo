package net.novauniverse.games.bingo.game;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

import net.zeeraa.novacore.commons.log.Log;

public class BingoItems {
	public static final Map<Material, String> POSSIBLE_ITEMS = new HashMap<>();

	static {
		POSSIBLE_ITEMS.put(Material.BREAD, "Bread");
		POSSIBLE_ITEMS.put(Material.IRON_INGOT, "Iron ingot");
		POSSIBLE_ITEMS.put(Material.COOKED_BEEF, "Cooked beef");
		POSSIBLE_ITEMS.put(Material.WOOL, "Wool");
		POSSIBLE_ITEMS.put(Material.STONE, "Stone");
		POSSIBLE_ITEMS.put(Material.COBBLESTONE, "Cobblestone");
		POSSIBLE_ITEMS.put(Material.GLASS_BOTTLE, "Glass bottle");
		POSSIBLE_ITEMS.put(Material.GLASS, "Glass");
		POSSIBLE_ITEMS.put(Material.BONE, "Bone");
		POSSIBLE_ITEMS.put(Material.GOLD_INGOT, "Gold ingot");
		POSSIBLE_ITEMS.put(Material.PAPER, "Paper");
		POSSIBLE_ITEMS.put(Material.SUGAR, "Sugar");
		POSSIBLE_ITEMS.put(Material.SUGAR_CANE, "Sugarcane");
	}
	
	public static String getMaterialDisplayName(Material material) {
		if (BingoItems.POSSIBLE_ITEMS.containsKey(material)) {
			return BingoItems.POSSIBLE_ITEMS.get(material);
		}

		Log.warn("BingoItemGenerator#getMaterialDisplayName()", "Cant find name for material " + material.name());
		return material.name();
	}
}