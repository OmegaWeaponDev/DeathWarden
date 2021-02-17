package me.omegaweapondev.deathwarden.utils;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.ou.library.Utilities;
import me.ou.library.configs.ConfigCreator;
import me.ou.library.configs.ConfigUpdater;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class StorageManager {
  private final DeathWarden plugin;

  private final ConfigCreator configFile;
  private final ConfigCreator messagesFile;
  private final ConfigCreator pvpLog;
  private final ConfigCreator totalDeathsLog;
  private final ConfigCreator deathEffectMenus;
  private final File tempUserFile;
  private final File userFile;
  private final FileConfiguration userData;
  private final Player player;
  private final UUID playerUUID;
  private final Map<UUID, Location> deathLocationMap;
  private final Map<UUID, Boolean> penaltyMap;
  private final Map<UUID, Boolean> deathEffectsMap;

  private CreaturesKilledUtil creaturesKilledUtil;

  public StorageManager(final DeathWarden plugin) {
    this.plugin = plugin;
    player = null;
    playerUUID = null;

    configFile = new ConfigCreator("config.yml");
    messagesFile = new ConfigCreator("messages.yml");
    pvpLog = new ConfigCreator("logs" + File.separator + "pvpKills.yml");
    totalDeathsLog = new ConfigCreator("logs" + File.separator + "totalDeathsLog.yml");
    deathEffectMenus = new ConfigCreator("deathEffectMenus.yml");
    deathLocationMap = new HashMap<>();
    penaltyMap = new HashMap<>();
    deathEffectsMap = new HashMap<>();

    creaturesKilledUtil = new CreaturesKilledUtil();
    tempUserFile = new File(plugin.getDataFolder() + File.separator + "userData", "tempUserFile.yml");
    userFile = null;
    userData = null;

  }

  public StorageManager(final DeathWarden plugin, final Player player, final UUID playerUUID) {
    this.plugin = plugin;
    this.player = player;
    this.playerUUID = playerUUID;

    configFile = new ConfigCreator("config.yml");
    messagesFile = new ConfigCreator("messages.yml");
    pvpLog = new ConfigCreator("logs" + File.separator + "pvpKills.yml");
    totalDeathsLog = new ConfigCreator("logs" + File.separator + "totalDeathsLog.yml");
    deathEffectMenus = new ConfigCreator("deathEffectMenus.yml");
    deathLocationMap = new HashMap<>();
    penaltyMap = new HashMap<>();
    deathEffectsMap = new HashMap<>();

    creaturesKilledUtil = new CreaturesKilledUtil();
    tempUserFile = new File(plugin.getDataFolder() + File.separator + "userData", "tempUserFile.yml");
    userFile = new File(plugin.getDataFolder() + File.separator + "userData", player.getUniqueId().toString() + ".yml");
    userData = YamlConfiguration.loadConfiguration(userFile);
  }

  public void setupConfigs() {
    // Setup the files
    getConfigFile().createConfig();
    getMessagesFile().createConfig();
    getPvpLog().createConfig();
    getTotalDeathsLog().createConfig();
    getDeathEffectMenus().createConfig();

  }

  public void configUpdater() {
    Utilities.logInfo(true, "Attempting to update the config files....");

    try {
      if(getConfigFile().getConfig().getDouble("Config_Version") != 2.0) {
        getConfigFile().getConfig().set("Config_Version", 2.0);
        getConfigFile().saveConfig();
        ConfigUpdater.update(plugin, "config.yml", getConfigFile().getFile(), Arrays.asList("none"));
      }

      if(getMessagesFile().getConfig().getDouble("Config_Version") != 2.0) {
        getMessagesFile().getConfig().set("Config_Version", 2.0);
        getMessagesFile().saveConfig();
        ConfigUpdater.update(plugin, "messages.yml", getMessagesFile().getFile(), Arrays.asList("none"));
      }
      plugin.onReload();
      Utilities.logInfo(true, "Config Files have successfully been updated!");
    } catch(IOException ex) {
      ex.printStackTrace();
    }
  }

  public void createUserFile() {

    if(userFile.exists()) {
      return;
    }

    renameFile(tempUserFile, userFile);

    userData.options().header(
      " -------------------------------------------------------------------------------------------\n" +
        "\n" +
        "  Welcome to DeathWarden's user file.\n" +
        "\n" +
        "  This file contains all the information that is stored about a specific user.\n" +
        "  There is generally no need to to touch this file, as everything can be altered in-game\n" +
        "\n" +
        "  You can find all the sounds here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html\n" +
        "  You can find all the particles here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Particle.html\n" +
        "\n" +
        " -------------------------------------------------------------------------------------------\n"
    );

    userData.createSection("Death_Effects");
    userData.set("Death_Effects.Enabled", false);
    userData.set("Death_Effects.Death_Sound", "ENTITY_CAT_DEATH");
    userData.set("Death_Effects.Death_Particle", "EXPLOSION_NORMAL");

    userData.createSection("Death_Count");
    userData.set("Death_Count", 0);

    userData.createSection("Pvp_Kills");
    userData.set("Pvp_Kills", 0);

    userData.createSection("Last_Known_Death_Location");
    userData.set("Last_Known_Death_Location.World", "");
    userData.set("Last_Known_Death_Location.X", 0.0);
    userData.set("Last_Known_Death_Location.Y", 0.0);
    userData.set("Last_Known_Death_Location.Z", 0.0);
    userData.set("Last_Known_Death_Location.Yaw", 0.0);
    userData.set("Last_Known_Death_Location.Pitch", 0.0);

    userData.createSection("Creatures_Killed");

    for(String creature : creaturesKilledUtil.creaturesKilled()) {
      userData.set("Creatures_Killed." + creature, 0);
    }
    savePlayerData();
    Utilities.logInfo(true, "A user file has been created for: " + player.getName());
  }

  public void savePlayerData() {
    try {
      userData.save(userFile);
    } catch(IOException exception) {
      exception.printStackTrace();
    }
  }

  public void resetPlayerData() {
    userData.set("Death_Effects.Enabled", false);
    userData.set("Death_Sound", "ENTITY_CAT_DEATH");
    userData.set("Death_Particles", "EXPLOSION_NORMAL");
    userData.set("Death_Count", 0);
    userData.set("Pvp_Kills", 0);
    userData.set("Last_Known_Death_Location.World", "''");
    userData.set("Last_Known_Death_Location.X", 0);
    userData.set("Last_Known_Death_Location.Y", 0);
    userData.set("Last_Known_Death_Location.Z", 0);
    userData.set("Last_Known_Death_Location.Yaw", 0);
    userData.set("Last_Known_Death_Location.Pitch", 0);
    savePlayerData();

    for(String creature : getUserSection("Creatures_Killed")) {
      userData.set("Creatures_Killed." + creature, 0);
      savePlayerData();
    }
  }

  private void renameFile(final File templateFile, final File playerFile) {
    boolean rename = templateFile.renameTo(playerFile);

    if(rename) {
      Utilities.logInfo(true, "A player warp file has been created for " + player.getName());
    }
  }

  public void setUserBoolean(final String key, final boolean value) {
    userData.set(key, value);
    savePlayerData();
  }

  public void setUserString(final String key, final String value) {
    userData.set(key, value);
    savePlayerData();
  }

  public void setUserStringList(final String key, final List<String> value) {
    userData.set(key, value);
    savePlayerData();
  }

  public void setUserInt(final String key, final int value) {
    userData.set(key, value);
    savePlayerData();
  }

  public void setUserDouble(final String key, final double value) {
    userData.set(key, value);
    savePlayerData();
  }

  public void setUserLong(final String key, final long value) {
    userData.set(key, value);
    savePlayerData();
  }
  
  public Boolean getUserBoolean(final String key) {
    return userData.getBoolean(key);
  }

  public String getUserString(final String key) {
    return userData.getString(key);
  }

  public List<String> getUserStringList(final String key) {
    return userData.getStringList(key);
  }

  public int getUserInt(final String key) {
    return userData.getInt(key);
  }

  public long getUserLong(final String key) {
    return userData.getLong(key);
  }
  
  public Set<String> getUserSection(final String key) {
    return userData.getConfigurationSection(key).getKeys(false);
  }

  public ConfigCreator getPvpLog() {
    return pvpLog;
  }

  public ConfigCreator getTotalDeathsLog() {
    return totalDeathsLog;
  }

  public ConfigCreator getConfigFile() {
    return configFile;
  }

  public ConfigCreator getMessagesFile() {
    return messagesFile;
  }

  public ConfigCreator getDeathEffectMenus() {
    return deathEffectMenus;
  }

  public Map<UUID, Location> getDeathLocation () {
    return deathLocationMap;
  }

  public Map<UUID, Boolean> getPenaltyMap() {
    return penaltyMap;
  }

  public Map<UUID, Boolean> getDeathEffectsMap() {
    return deathEffectsMap;
  }
}
