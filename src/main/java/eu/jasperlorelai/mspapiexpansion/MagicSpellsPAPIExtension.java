package eu.jasperlorelai.mspapiexpansion;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import org.jetbrains.annotations.NotNull;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.mana.ManaHandler;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.variables.Variable;
import com.nisovin.magicspells.util.magicitems.MagicItem;
import com.nisovin.magicspells.util.magicitems.MagicItems;
import com.nisovin.magicspells.util.data.DataLivingEntity;
import com.nisovin.magicspells.util.managers.VariableManager;
import com.nisovin.magicspells.variables.variabletypes.GlobalVariable;
import com.nisovin.magicspells.variables.variabletypes.GlobalStringVariable;

public class MagicSpellsPAPIExtension extends PlaceholderExpansion {

	private static final String AUTHOR = "JasperLorelai";
	private static final String IDENTIFIER = "ms";
	private static final String PLUGIN = "MagicSpells";
	private static final String NAME = "magicspells";
	private static final String VERSION = "2.0.2";

	private MagicSpells plugin;

	@Override
	public boolean canRegister() {
		return Bukkit.getPluginManager().getPlugin(getRequiredPlugin()) != null;
	}

	@Override
	public boolean register() {
		if (!canRegister()) return false;
		plugin = (MagicSpells) Bukkit.getPluginManager().getPlugin(getRequiredPlugin());
		if (plugin == null) return false;
		return super.register();
	}

	@NotNull
	@Override
	public String getAuthor() {
		return AUTHOR;
	}

	@NotNull
	@Override
	public String getName() {
		return NAME;
	}

	@NotNull
	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@NotNull
	@Override
	public String getRequiredPlugin() {
		return PLUGIN;
	}

	@NotNull
	@Override
	public String getVersion() {
		return VERSION;
	}

	public String onRequest(OfflinePlayer offlinePlayer, @NotNull String identifier) {
		Player player = offlinePlayer != null && offlinePlayer.isOnline() ? (Player) offlinePlayer : null;

		String[] splits = identifier.split("_", 2);
		String name = splits[0];
		String args = splits.length > 1 ? splits[1] : null;

		return switch (name) {
			case "variable" -> processVariable(player, args);
			case "cooldown" -> processCooldown(player, args);
			case "charges" -> processCharges(player, args);
			case "mana" -> processMana(player, args);
			case "buff" -> processBuff(player, args);
			case "selectedspell" -> processSelectedSpell(player, args);
			case "data" -> processDataElement(player, args);
			case "item" -> processMagicItem(args);
			default -> null;
		};
	}

	private String error(String string) {
		return plugin.getName() + ": " + string;
	}

	/**
	 * variable     [varname],(precision)
	 * variable max [varname],(precision)
	 * variable min [varname],(precision)
	 */
	private String processVariable(Player player, String args) {
		if (args == null) return null;

		String[] splits = args.split("_", 2);
		VariableValue type = switch (splits[0]) {
			case "max" -> VariableValue.MAX;
			case "min" -> VariableValue.MIN;
			default -> VariableValue.CURRENT;
		};

		String varName;
		if (type == VariableValue.CURRENT) varName = args;
		else {
			if (splits.length < 2) return null;
			varName = splits[1];
		}

		String precision = null;
		if (varName.contains(",")) {
			splits = varName.split(",", 2);
			varName = splits[0];
			precision = splits[1];
		}

		VariableManager manager = MagicSpells.getVariableManager();
		if (manager == null) return null;
		varName = PlaceholderAPI.setBracketPlaceholders(player, varName).trim();
		Variable variable = manager.getVariable(varName);
		if (variable == null) return error("Variable '" + varName + "' wasn't found.");

		String value = null;
		switch (type) {
			case MAX -> value = String.valueOf(variable.getMaxValue());
			case MIN -> value = String.valueOf(variable.getMinValue());
			case CURRENT -> {
				if (variable instanceof GlobalVariable || variable instanceof GlobalStringVariable) {
					value = variable.getStringValue((String) null);
					break;
				}
				if (player == null) return error("Player target not found.");
				value = variable.getStringValue(player);
			}
		}
		if (value == null) return null;

		return Util.setPrecision(value, precision);
	}

	private enum VariableValue {
		MAX,
		MIN,
		CURRENT
	}

	/**
	 * cooldown     [spellname],(precision)
	 * cooldown now [spellname],(precision)
	 */
	private String processCooldown(Player player, String args) {
		if (args == null) return null;

		String[] splits = args.split("_", 2);
		boolean isNow = splits[0].equals("now");
		String spellName;
		if (isNow) {
			if (splits.length < 2) return null;
			spellName = splits[1];
		}
		else spellName = args;

		String precision = null;
		if (spellName.contains(",")) {
			splits = spellName.split(",", 2);
			spellName = splits[0];
			precision = splits[1];
		}

		spellName = PlaceholderAPI.setBracketPlaceholders(player, spellName).trim();
		Spell spell = MagicSpells.getSpellByInternalName(spellName);
		if (spell == null) return error("Spell '" + spellName + "' wasn't found.");

		float cooldown;
		if (isNow) {
			if (player == null) return error("Player target not found.");
			else cooldown = spell.getCooldown(player);
		}
		else cooldown = Util.getFloatData(spell, "getCooldown", Spell.class);

		return Util.setPrecision(String.valueOf(cooldown), precision);
	}

	/**
	 * charges          [spellname]
	 * charges consumed [spellname]
	 */
	private String processCharges(Player player, String args) {
		if (args == null) return null;

		String[] splits = args.split("_", 2);
		boolean isConsumed = splits[0].equals("consumed");
		String spellName;
		if (isConsumed) {
			if (splits.length < 2) return null;
			spellName = splits[1];
		}
		else spellName = args;
		spellName = PlaceholderAPI.setBracketPlaceholders(player, spellName).trim();
		Spell spell = MagicSpells.getSpellByInternalName(spellName);
		if (spell == null) return error("Spell '" + spellName + "' wasn't found.");

		if (isConsumed) {
			if (player == null) return error("Player target not found.");
			else return String.valueOf(spell.getCharges(player));
		}
		return String.valueOf(spell.getCharges());
	}

	/**
	 * mana
	 * mana max
	 */
	private String processMana(Player player, String args) {
		if (player == null) return error("Player target not found.");
		boolean isMax = args != null && args.equals("max");

		ManaHandler handler = MagicSpells.getManaHandler();
		return String.valueOf(isMax ? handler.getMaxMana(player) : handler.getMana(player));
	}

	/**
	 * buff     [spellname],(precision)
	 * buff now [spellname],(precision)
	 */
	private String processBuff(Player player, String args) {
		if (args == null) return null;

		String[] splits = args.split("_", 2);
		boolean isNow = splits[0].equals("now");
		String spellName;
		if (isNow) {
			if (splits.length < 2) return null;
			spellName = splits[1];
		}
		else spellName = args;

		String precision = null;
		if (spellName.contains(",")) {
			splits = spellName.split(",", 2);
			spellName = splits[0];
			precision = splits[1];
		}

		spellName = PlaceholderAPI.setBracketPlaceholders(player, spellName).trim();
		Spell spell = MagicSpells.getSpellByInternalName(spellName);
		if (!(spell instanceof BuffSpell buff)) return error("Buff spell '" + spellName + "' not found.");

		float duration;
		if (isNow) {
			if (player == null) return error("Player target not found.");
			else duration = buff.getDuration(player);
		}
		else duration = Util.getFloatData(buff, "getDuration", BuffSpell.class);

		return Util.setPrecision(String.valueOf(duration), precision);
	}

	/**
	 * selectedspell
	 * selectedspell displayed
	 */
	private String processSelectedSpell(Player player, String args) {
		if (player == null) return error("Player target not found.");
		boolean isDisplayed = args != null && args.equals("displayed");

		Spell spell = MagicSpells.getSpellbook(player).getActiveSpell(player.getInventory().getItemInMainHand());
		if (spell == null) return "";
		return isDisplayed ? Util.miniMessage(spell.getName()) : spell.getInternalName();
	}

	/**
	 * data [dataElement]
	 */
	private String processDataElement(Player player, String args) {
		if (args == null) return null;
		try {
			return DataLivingEntity.getDataFunction(PlaceholderAPI.setBracketPlaceholders(player, args)).apply(player);
		} catch (NullPointerException exception) {
			return error(exception.getMessage());
		}
	}

	/**
	 * item ms        [magicItemName]
	 * item nbt       [magicItemName]
	 * item component [magicItemName]
	 * item amount    [magicItemName]
	 */
	private String processMagicItem(String args) {
		if (args == null) return null;
		String[] splits = args.split("_", 2);
		if (splits.length < 2) return null;

		String type = splits[0];
		String itemName = splits[1];
		MagicItem magicItem = MagicItems.getMagicItemByInternalName(itemName);
		if (magicItem == null) return error("Item '" + itemName + "' not found.");
		ItemStack item = magicItem.getItemStack();

		return switch (type) {
			case "ms" -> magicItem.getMagicItemData().toString();
			case "nbt" -> {
				String data = item.getType().getKey().toString();
				if (item.hasItemMeta()) data += item.getItemMeta().getAsString();
				yield data;
			}
			case "component" -> {
				String data = item.getType().getKey().toString();
				if (item.hasItemMeta()) data += item.getItemMeta().getAsComponentString();
				yield data;
			}
			case "amount" -> String.valueOf(item.getAmount());
			default -> null;
		};
	}

}
