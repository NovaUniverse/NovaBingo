package net.novauniverse.games.bingo.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Material;

public class BingoItemGenerator {
	public static final int AMOUNT_TO_GENERATE = 9;

	public static List<Material> generateItems() {
		List<Material> potentialItems = new ArrayList<>();

		BingoItems.POSSIBLE_ITEMS.keySet().forEach(m -> potentialItems.add(m));

		Collections.shuffle(potentialItems);

		List<Material> result = new ArrayList<>();

		for (int i = 0; i < AMOUNT_TO_GENERATE; i++) {
			if (potentialItems.size() > 0) {
				result.add(potentialItems.remove(0));
			}
		}

		return result;
	}
}