package me.omegaweapondev.deathwarden.utils;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.omegaweapondev.deathwarden.DeathWarden;
import org.bukkit.entity.Player;

public class Placeholders extends PlaceholderExpansion {
  private final DeathWarden plugin;

  private UserDataHandler userData;

  public Placeholders(final DeathWarden plugin) {
    this.plugin = plugin;
  }

  /**
   * This method should always return true unless we
   * have a dependency we need to make sure is on the server
   * for our placeholders to work!
   *
   * @return always true since we do not have any dependencies.
   */
  @Override
  public boolean canRegister(){
    return true;
  }

  /**
   * The name of the person who created this expansion should go here.
   *
   * @return The name of the author as a String.
   */
  @Override
  public String getAuthor(){
    return "omegaweapondev";
  }

  /**
   * The placeholder identifier should go here.
   * <br>This is what tells PlaceholderAPI to call our onRequest
   * method to obtain a value if a placeholder starts with our
   * identifier.
   * <br>The identifier has to be lowercase and can't contain _ or %
   *
   * @return The identifier in {@code %<identifier>_<value>%} as String.
   */
  @Override
  public String getIdentifier(){
    return "deathwarden";
  }

  /**
   * This is the version of this expansion.
   * <br>You don't have to use numbers, since it is set as a String.
   *
   * @return The version as a String.
   */
  @Override
  public String getVersion(){
    return "1.0.0";
  }

  /**
   * This is the method called when a placeholder with our identifier
   * is found and needs a value.
   * <br>We specify the value identifier in this method.
   * <br>Since version 2.9.1 can you use OfflinePlayers in your requests.
   *
   * @param  player
   *         A {@link org.bukkit.OfflinePlayer OfflinePlayer}.
   * @param  identifier
   *         A String containing the identifier/value.
   *
   * @return Possibly-null String of the requested identifier.
   */
  @Override
  public String onPlaceholderRequest(Player player, String identifier) {
    userData = new UserDataHandler(plugin, player, player.getUniqueId());

    if(player == null) {
      return "";
    }

    // %deathwarden_hasDeathEffects%
    if(identifier.equalsIgnoreCase("hasdeatheffects")) {
      return String.valueOf(userData.getPlayerData().getBoolean("Death_Effects.Enabled"));
    }

    // %deathwarden_deaths% OR %deathwarden_deathcount%
    if(identifier.equalsIgnoreCase("deaths") || identifier.equalsIgnoreCase("deathcount")){
      return String.valueOf(userData.getPlayerData().getInt("Death_Count"));
    }

    // %deathwarden_pvpkills%
    if(identifier.equalsIgnoreCase("pvpkills")) {
      return String.valueOf(userData.getPlayerData().getInt("Pvp_Kills"));
    }

    // %deathwarden_player_totalKills%
    if(identifier.equalsIgnoreCase("kills_totalKills")) {
      int totalKills = 0;

      for(int i = 0; i < userData.getPlayerData().getConfigurationSection("Creatures_Killed").getKeys(false).size(); i++) {
        totalKills = totalKills + userData.getPlayerData().getInt("Creatures_Killed." + i);
      }

      return String.valueOf(totalKills);
    }

    // %deathwarden_totalkills
    if(identifier.equalsIgnoreCase("totalkills")) {
      int totalKills = 0;

      for(int i = 0; i < plugin.getSettingsHandler().getTotalDeathsLog().getConfig().getConfigurationSection("Creatures_Killed").getKeys(false).size(); i++) {
        totalKills = totalKills + i;
      }
      return String.valueOf(totalKills);
    }

    // %deathwarden_kills_<mobname>% i.e %deathwarden_kills_skeleton%
    if(identifier.contains("kills_")) {

      for(String creature : userData.getPlayerData().getConfigurationSection("Creatures_Killed").getKeys(false)) {
        if(identifier.equalsIgnoreCase("kills_" + creature.toLowerCase())) {
          return String.valueOf(userData.getPlayerData().getInt("Creatures_Killed." + creature));
        }
      }
    }

    // %deathwarden_totalkills_creature_<creature>% i.e %deathwarden_totalkills_creature_skeleton%
    if(identifier.contains("totalKills_creature_")) {

      for(String creature : plugin.getSettingsHandler().getTotalDeathsLog().getConfig().getConfigurationSection("Creatures_Killed").getKeys(false)) {
        if(identifier.equalsIgnoreCase("totalkills_creature_" + creature)) {
          return String.valueOf(creature);
        }
      }
    }
    return null;
  }
}
