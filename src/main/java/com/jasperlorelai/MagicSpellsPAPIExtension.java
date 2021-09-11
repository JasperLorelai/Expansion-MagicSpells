package com.jasperlorelai;

import org.jetbrains.annotations.NotNull;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.LivingEntity;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import com.nisovin.magicspells.Spell;
import com.nisovin.magicspells.MagicSpells;
import com.nisovin.magicspells.mana.ManaHandler;
import com.nisovin.magicspells.spells.BuffSpell;
import com.nisovin.magicspells.variables.Variable;
import com.nisovin.magicspells.util.managers.VariableManager;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class MagicSpellsPAPIExtension extends PlaceholderExpansion {

	private String identifier = "magicspells";

	private static final String AUTHOR = "JasperLorelai";
	private static final String PLUGIN = "MagicSpells";
	private static final String NAME = "magicspells";
	private static final String VERSION = "5.0";
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
		String key = "expansions." + getName();
		FileConfiguration config = getPlaceholderAPI().getConfig();
		ConfigurationSection section = config.getConfigurationSection(key);
		if (section == null) {
			identifier = "magicspells";
			section = config.createSection(key);
			section.set("identifier", "magicspells");
			getPlaceholderAPI().saveConfig();
			getPlaceholderAPI().reloadConfig();
		}
		else identifier = section.getString("identifier");
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
		return identifier;
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
		if (offlinePlayer == null) return null;
		Player player = offlinePlayer.getPlayer();
		String[] args = identifier.split("_");

		Spell spell;
		String value;
		String[] splits;
		String precision = null;

		switch (args[0]) {
			case "variable": {
				if (args.length < 2) return null;
				Variable variable;
				VariableManager manager = MagicSpells.getVariableManager();
				if (manager == null) return null;
				switch (args[1]) {
					// %magicspells_variable_max_[varname],(precision)%
					case "max": {
						if (args.length < 3) return null;
						identifier = Util.joinArgs(args, 2);
						if (identifier.contains(",")) {
							splits = identifier.split(",");
							identifier = splits[0];
							precision = splits[1];
						}
						variable = manager.getVariable(identifier);
						if (variable == null) return plugin.getName() + ": Player/Global variable '" + identifier + "' wasn't found.";
						value = variable.getMaxValue() + "";
						break;
					}

					// %magicspells_variable_min_[varname],(precision)%
					case "min": {
						if (args.length < 3) return null;
						identifier = Util.joinArgs(args, 2);
						if (identifier.contains(",")) {
							splits = identifier.split(",");
							identifier = splits[0];
							precision = splits[1];
						}
						variable = manager.getVariable(identifier);
						if (variable == null) return plugin.getName() + ": Player/Global variable '" + identifier + "' wasn't found.";
						value = variable.getMinValue() + "";
						break;
					}

					// %magicspells_variable_[varname],(precision)%
					default: {
						identifier = Util.joinArgs(args, 1);
						if (identifier.contains(",")) {
							splits = identifier.split(",");
							identifier = splits[0];
							precision = splits[1];
						}
						variable = manager.getVariable(identifier);
						if (variable == null) return plugin.getName() + ": Variable '" + identifier + "' wasn't found.";
						value = variable.getStringValue(offlinePlayer.getName());
						break;
					}
				}
				return precision == null ? value : Util.setPrecision(value, precision);
			}

			case "cooldown": {
				if (args.length < 2) return null;
				// %magicspells_cooldown_now_[spellname],(precision)%
				if (args[1].equals("now")) identifier = Util.joinArgs(args, 2);
				// %magicspells_cooldown_[spellname],(precision)%
				else identifier = Util.joinArgs(args, 1);

				if (identifier.contains(",")) {
					splits = identifier.split(",");
					identifier = splits[0];
					precision = splits[1];
				}
				spell = MagicSpells.getSpellByInternalName(identifier);
				if (spell == null) return plugin.getName() + ": Spell '" + identifier + "' wasn't found.";

				if (args[1].equals("now")) value = spell.getCooldown((LivingEntity) offlinePlayer) + "";
				else value = spell.getCooldown() + "";
				return precision == null ? value : Util.setPrecision(value, precision);
			}

			case "charges": {
				if (args.length < 2) return null;
				// %magicspells_charges_consumed_[spellname]%
				if (args[1].equals("consumed")) identifier = Util.joinArgs(args, 2);
				// %magicspells_charges_[spellname]%
				else identifier = Util.joinArgs(args, 1);
				spell = MagicSpells.getSpellByInternalName(identifier);
				if (spell == null) return plugin.getName() + ": Spell '" + identifier + "' wasn't found.";
				return (args[1].equals("consumed") ? spell.getCharges((LivingEntity) offlinePlayer) : spell.getCharges()) + "";
			}

			case "mana": {
				if (player == null) return null;
				ManaHandler handler = MagicSpells.getManaHandler();
				return "" + (args.length > 1 && args[1].equals("max") ? handler.getMaxMana(player) : handler.getMana(player));
			}

			case "buff": {
				if (args.length < 2) return null;
				if (player == null) return null;
				// %magicspells_buff_now_[spellname],(precision)%
				if (args[1].equals("now")) identifier = Util.joinArgs(args, 2);
				// %magicspells_buff_[spellname],(precision)%
				else identifier = Util.joinArgs(args, 1);

				if (identifier.contains(",")) {
					splits = identifier.split(",");
					identifier = splits[0];
					precision = splits[1];
				}
				BuffSpell buffSpell = (BuffSpell) MagicSpells.getSpellByInternalName(identifier);
				if (buffSpell == null) return plugin.getName() + ": Buff spell '" + args[0] + "' wasn't found.";

				if (args[1].equals("now")) value = buffSpell.getDuration(player) + "";
				else value = buffSpell.getDuration() + "";
				return precision == null ? value : Util.setPrecision(value, precision);
			}

			case "selectedspell": {
				if (player == null) return null;
				spell = MagicSpells.getSpellbook(player).getActiveSpell(player.getInventory().getItemInMainHand());
				return spell == null ? "" : spell.getInternalName();
			}

			case "int2hex": {
				try {
					return Integer.toHexString(Integer.parseInt(Util.joinArgs(args, 1)));
				}
				catch (NumberFormatException ignored) {
					return null;
				}
			}

			case "unicodea": {
				return Util.unescapeUnicode(PlaceholderAPI.setBracketPlaceholders(offlinePlayer, Util.joinArgs(args, 1)));
			}
		}
		return null;
	}

}