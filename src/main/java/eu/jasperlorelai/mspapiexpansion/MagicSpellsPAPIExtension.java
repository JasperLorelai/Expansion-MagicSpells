package eu.jasperlorelai.mspapiexpansion;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.Collectors;
import java.util.function.BiFunction;
import java.nio.charset.StandardCharsets;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.Cacheable;
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
import com.nisovin.magicspells.variables.variabletypes.PlayerVariable;
import com.nisovin.magicspells.variables.variabletypes.GlobalStringVariable;

public class MagicSpellsPAPIExtension extends PlaceholderExpansion implements Listener, Cacheable {

	private static final Pattern VARIABLE_FILE_PATTERN = Pattern.compile("PLAYER_(?<id1>\\w{8})(?<id2>\\w{4})(?<id3>\\w{4})(?<id4>\\w{4})(?<id5>\\w{12})\\.txt");

	private static final String AUTHOR = "JasperLorelai";
	private static final String IDENTIFIER = "ms";
	private static final String PLUGIN = "MagicSpells";
	private static final String NAME = "magicspells";
	private static final String VERSION = "2.0.3";

	private MagicSpells plugin;

	private File variableDir;
	private final Map<UUID, Map<String, String>> variableCache = new HashMap<>();

	@Override
	public boolean canRegister() {
		return Bukkit.getPluginManager().getPlugin(getRequiredPlugin()) != null;
	}

	@Override
	public boolean register() {
		if (!canRegister()) return false;

		plugin = (MagicSpells) Bukkit.getPluginManager().getPlugin(getRequiredPlugin());
		if (plugin == null) return false;

		variableDir = new File(plugin.getDataFolder(), "vars");

		// Cache variable files of all offline players.
		Bukkit.getScheduler().runTask(plugin, () -> {
			File[] variableFiles = variableDir.listFiles();
			if (variableFiles == null) return;
			var onlinePlayers = Bukkit.getOnlinePlayers();

			varLoop:
			for (File variableFile : variableFiles) {
				if (variableFile.isDirectory()) continue;

				Matcher matcher = VARIABLE_FILE_PATTERN.matcher(variableFile.getName());
				if (!matcher.matches()) continue;

				try {
					UUID uuid = UUID.fromString(Stream.of("id1", "id2", "id3", "id4", "id5").map(matcher::group).collect(Collectors.joining("-")));
					for (Player player : onlinePlayers)
						if (player.getUniqueId().equals(uuid))
							continue varLoop;
					cacheVariableFile(uuid, variableFile);
				} catch (IllegalArgumentException ignored) {}
			}
		});

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

	@Override
	@Nullable
	public String onRequest(OfflinePlayer offlinePlayer, @NotNull String identifier) {
		Player player = offlinePlayer != null && offlinePlayer.isOnline() ? (Player) offlinePlayer : null;

		String[] splits = identifier.split("_", 2);
		String name = splits[0];
		String args = splits.length > 1 ? splits[1] : null;

		return switch (name) {
			case "variable" -> processVariable(player, args);
			case "offlinevar" -> processOfflineVariable(offlinePlayer, args);
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

	@Override
	public void clear() {
		plugin = null;
		variableDir = null;
		variableCache.clear();
	}

	private String error(String string) {
		return plugin.getName() + ": " + string;
	}

	private enum VariableValue {
		MAX,
		MIN,
		CURRENT
	}

	private String processVariableCommon(OfflinePlayer player, String args, BiFunction<Variable, String, String> function) {
		if (args == null) return null;
		VariableManager manager = MagicSpells.getVariableManager();
		if (manager == null) return error("Variable manager not loaded.");

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
				value = function.apply(variable, varName);
			}
		}
		if (value == null) return null;

		return Util.setPrecision(value, precision);
	}


	/**
	 * variable     [varname],(precision)
	 * variable max [varname],(precision)
	 * variable min [varname],(precision)
	 */
	private String processVariable(Player player, String args) {
		return processVariableCommon(player, args, (variable, varName) -> variable.getStringValue(player));
	}

	/**
	 * offlinevar     [varname],(precision)
	 * offlinevar max [varname],(precision)
	 * offlinevar min [varname],(precision)
	 */
	private String processOfflineVariable(OfflinePlayer player, String args) {
		return processVariableCommon(player, args, (variable, varName) -> {
			if (player.isOnline()) return variable.getStringValue((Player) player);
			return variableCache.getOrDefault(player.getUniqueId(), Map.of()).getOrDefault(varName, "");
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		variableCache.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerQuit(PlayerQuitEvent event) {
		Bukkit.getScheduler().runTask(plugin, () -> {
			if (!variableDir.exists()) return;

			Player player = event.getPlayer();
			String uniqueId = com.nisovin.magicspells.util.Util.getUniqueId(player);
			File file = new File(variableDir, "PLAYER_" + uniqueId + ".txt");
			if (!file.exists() || file.isDirectory()) return;
			cacheVariableFile(player.getUniqueId(), file);
		});
	}

	private void cacheVariableFile(UUID uuid, File file) {
		VariableManager manager = MagicSpells.getVariableManager();
		if (manager == null) return;

		try {
			Scanner scanner = new Scanner(file, StandardCharsets.UTF_8);
			while (scanner.hasNext()) {
				String line = scanner.nextLine().trim();
				if (line.isEmpty()) continue;
				String[] splits = line.split("=", 2);
				String variableName = splits[0];
				String value = splits[1];

				Variable variable = manager.getVariable(variableName);
				if (!(variable instanceof PlayerVariable) || !variable.isPermanent()) continue;

				variableCache.computeIfAbsent(uuid, k -> new HashMap<>());
				variableCache.get(uuid).put(variableName, value);
			}
			scanner.close();
		} catch (Exception ignored) {}
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
