# Expansion-MagicSpells
[**Downloads**](https://github.com/JasperLorelai/Expansion-MagicSpells/releases): ![Releases](https://img.shields.io/github/downloads/JasperLorelai/Expansion-MagicSpells/total.svg)

[PlaceholderAPI](https://www.spigotmc.org/resources/6245/) expansion for the [MagicSpells](https://github.com/TheComputerGeek2/MagicSpells/) plugin.

Drop the downloaded jar file in `./plugins/PlaceholderAPI/expansions`.

## Placeholder List:
Arguments in `[]` brackets are required, while arguments in `()` brackets are optional - the placeholders will work without them.
Versions:
- [1.0](#version-10)
- [2.0](#version-20)
- [3.0](#version-30)
- [4.0](#version-40)
- [5.0](#version-51)

#### Version `1.0`:
- `%magicspells_variable_[varname]_(precision)%` - Returns MS variable value, string or floating point - the latter can have decimal precision.
- `%magicspells_cooldown_[spellname]_(precision)%` - Returns MS spell cooldown, which can have decimal precision.
- `%magicspells_mana%`
- `%magicspells_maxmana%`

#### Version `2.0`:
- `%magicspells_variable_[varname]_(precision)%` - Returns MS variable value, string or floating point - the latter can have decimal precision.
- `%magicspells_cooldown_[spellname]_(precision)%` - Returns MS spell cooldown, which can have decimal precision.
- `%magicspells_mana%`
- `%magicspells_maxmana%`
- `%magicspells_buff_[spellname]_(precision)%` - Returns buff spell duration, which can have decimal precision.
- `%magicspells_selectedspell%` - Returns currently selected spell on the held item (cast item).

#### Version `3.0`:
- `%magicspells_variable_[varname],(precision)%` - Returns MS variable value of the target, string or floating point.
- `%magicspells_variable_max_[varname],(precision)%` - Returns maximal value of MS variable value of the target, floating point.
- `%magicspells_variable_min_[varname],(precision)%` - Returns minimal value of MS variable value of the target, floating point.
- `%magicspells_cooldown_[spellname],(precision)%` - Returns MS initial spell cooldown.
- `%magicspells_cooldown_now_[spellname],(precision)%` - Returns current MS spell cooldown of the target.
- `%magicspells_mana%`
- `%magicspells_mana_max%`
- `%magicspells_buff_[spellname],(precision)%` - Returns initial buff spell duration.
- `%magicspells_buff_now_[spellname],(precision)%` - Returns current buff spell duration of the target.
- `%magicspells_selectedspell%` - Returns currently selected spell of the target, on the held item (cast item).

#### Version `4.0`:
- `%magicspells_variable_[varname],(precision)%` - Returns MS variable value of the target, string or floating point.
- `%magicspells_variable_max_[varname],(precision)%` - Returns maximal value of MS variable value of the target, floating point.
- `%magicspells_variable_min_[varname],(precision)%` - Returns minimal value of MS variable value of the target, floating point.
- `%magicspells_cooldown_[spellname],(precision)%` - Returns MS initial spell cooldown.
- `%magicspells_cooldown_now_[spellname],(precision)%` - Returns current MS spell cooldown of the target.
- `%magicspells_charges_[spellname]%` - Returns MS initial spell charges.
- `%magicspells_charges_consumed_[spellname]%` - Returns consumed charges of the target.
- `%magicspells_mana%`
- `%magicspells_mana_max%`
- `%magicspells_buff_[spellname],(precision)%` - Returns initial buff spell duration.
- `%magicspells_buff_now_[spellname],(precision)%` - Returns current buff spell duration of the target.
- `%magicspells_selectedspell%` - Returns currently selected spell of the target, on the held item (cast item).

#### Version `5.1`:
- `%magicspells_variable_[varname],(precision)%` - Returns MS variable value of the target, string or floating point.
- `%magicspells_variable_max_[varname],(precision)%` - Returns maximal value of MS variable value of the target, floating point.
- `%magicspells_variable_min_[varname],(precision)%` - Returns minimal value of MS variable value of the target, floating point.
- `%magicspells_cooldown_[spellname],(precision)%` - Returns MS initial spell cooldown.
- `%magicspells_cooldown_now_[spellname],(precision)%` - Returns current MS spell cooldown of the target.
- `%magicspells_charges_[spellname]%` - Returns MS initial spell charges.
- `%magicspells_charges_consumed_[spellname]%` - Returns consumed charges of the target.
- `%magicspells_mana%`
- `%magicspells_mana_max%`
- `%magicspells_buff_[spellname],(precision)%` - Returns initial buff spell duration.
- `%magicspells_buff_now_[spellname],(precision)%` - Returns current buff spell duration of the target.
- `%magicspells_selectedspell%` - Returns currently selected spell of the target, on the held item (cast item).
- `%magicspells_int2hex_[int],(empty)%` - Converts integers to hex. Useful for translating MS number variables to hex, combined with the unicode placeholder. Optional `empty` parameter should be a number of zeros to prepend. Optional because 4 is the default value. 
- `%magicspells_unicode_[string]%` - Parses anything matching the unicode format to its character. (Format: `\uXXXX`)
