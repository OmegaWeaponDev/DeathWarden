package me.omegaweapondev.deathwarden.utils;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.ou.library.Utilities;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class UserDataHandler {
  private final DeathWarden plugin;
  private final Player player;
  private final UUID playerUUID;
  private final CreaturesKilledUtil creaturesKilledUtil;

  private final File tempUserFile;
  private final File userFile;
  private final FileConfiguration userData;

  public UserDataHandler(final DeathWarden plugin, final Player player, final UUID playerUUID) {
    this.plugin = plugin;
    this.player = player;
    this.playerUUID = playerUUID;

    creaturesKilledUtil = new CreaturesKilledUtil();
    tempUserFile = new File(plugin.getDataFolder() + File.separator + "userData", "tempUserFile.yml");
    userFile = new File(plugin.getDataFolder() + File.separator + "userData", player.getUniqueId().toString() + ".yml");
    userData = YamlConfiguration.loadConfiguration(userFile);
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
    userData.set("Death_Effects.Death_Sound", "none");
    userData.set("Death_Effects.Death_Particle", "none");

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
    userData.set("Last_Known_Death_Location.World", "world");
    userData.set("Last_Known_Death_Location.X", 0);
    userData.set("Last_Known_Death_Location.Y", 0);
    userData.set("Last_Known_Death_Location.Z", 0);
    userData.set("Last_Known_Death_Location.Yaw", 0);
    userData.set("Last_Known_Death_Location.Pitch", 0);
    savePlayerData();

    for(String creature : userData.getConfigurationSection("Creatures_Killed").getKeys(false)) {
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

  public FileConfiguration getPlayerData() {
    return userData;
  }
}
