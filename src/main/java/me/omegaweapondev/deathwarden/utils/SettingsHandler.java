package me.omegaweapondev.deathwarden.utils;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.ou.library.Utilities;
import me.ou.library.configs.ConfigCreator;
import me.ou.library.configs.ConfigUpdater;
import org.bukkit.Location;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SettingsHandler {
  private final DeathWarden plugin;

  private final ConfigCreator configFile = new ConfigCreator("config.yml");
  private final ConfigCreator messagesFile = new ConfigCreator("messages.yml");
  private final ConfigCreator pvpLog = new ConfigCreator("logs" + File.separator + "pvpKills.yml");
  private final ConfigCreator totalDeathsLog = new ConfigCreator("logs" + File.separator + "totalDeathsLog.yml");
  private final ConfigCreator deathEffectMenus = new ConfigCreator("deathEffectMenus.yml");
  private final ConfigCreator rewardsFile = new ConfigCreator("rewards.yml");

  private final Map<UUID, Location> deathLocationMap = new HashMap<>();
  private final Map<UUID, Boolean> penaltyMap = new HashMap<>();
  private final Map<UUID, Boolean> deathEffectsMap = new HashMap<>();
  private final Map<UUID, Integer> rewardLimitsMap = new HashMap<>();

  public SettingsHandler(final DeathWarden plugin) {
    this.plugin = plugin;
  }

  public void setupConfigs() {
    // Setup the files
    getConfigFile().createConfig();
    getMessagesFile().createConfig();
    getPvpLog().createConfig();
    getTotalDeathsLog().createConfig();
    getDeathEffectMenus().createConfig();
    getRewardsFile().createConfig();
  }

  public void configUpdater() {
    Utilities.logInfo(true, "Attempting to update the config files....");

    try {
      if(getConfigFile().getConfig().getDouble("Config_Version") != 2.2) {
        getConfigFile().getConfig().set("Config_Version", 2.2);
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

  public void reloadFiles() {
    getConfigFile().reloadConfig();
    getMessagesFile().reloadConfig();
    getPvpLog().reloadConfig();
    getTotalDeathsLog().reloadConfig();
    getDeathEffectMenus().reloadConfig();
    getRewardsFile().reloadConfig();
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

  public ConfigCreator getRewardsFile() {
    return rewardsFile;
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

  public Map<UUID, Integer> getRewardLimitsMap() {
    return rewardLimitsMap;
  }
}
