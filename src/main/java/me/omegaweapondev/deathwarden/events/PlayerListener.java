package me.omegaweapondev.deathwarden.events;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.DeathCommands;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.omegaweapondev.deathwarden.utils.UserDataHandler;
import me.ou.library.SpigotUpdater;
import me.ou.library.Utilities;
import me.ou.library.menus.MenuCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginDescriptionFile;

public class PlayerListener implements Listener {
  private final DeathWarden plugin;
  private final FileConfiguration configFile;
  private final MessageHandler messageHandler;

  private UserDataHandler userData;

  public PlayerListener(final DeathWarden plugin) {
    this.plugin = plugin;
    configFile = plugin.getSettingsHandler().getConfigFile().getConfig();
    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
    // Get the player that is joining
    final Player player = playerJoinEvent.getPlayer();

    if(configFile.getBoolean("Disabled_Worlds.Enabled")) {
      for(String world : configFile.getStringList("Disabled_Worlds.Worlds")) {
        if(player.getWorld().getName().equals(world)) {
          return;
        }
      }
    }

    userData = new UserDataHandler(plugin, player, player.getUniqueId());
    userData.createUserFile();

    // Check if the player has permission to receive plugin update messages.
    if(!Utilities.checkPermissions(player, true, "deathwarden.update", "deathwarden.admin")) {
      return;
    }

    if(configFile.getBoolean("Death_Effects_Login") && Utilities.checkPermissions(player, true, "deathwarden.deatheffects.login", "deathwarden.admin")) {
      plugin.getSettingsHandler().getDeathEffectsMap().put(player.getUniqueId(), true);
    } else {
      plugin.getSettingsHandler().getDeathEffectsMap().put(player.getUniqueId(), false);
    }

    new SpigotUpdater(plugin, 73535).getVersion(version -> {
      int spigotVersion = Integer.parseInt(version.replace(".", ""));
      int pluginVersion = Integer.parseInt(plugin.getDescription().getVersion().replace(".", ""));

      if(pluginVersion >= spigotVersion) {
        Utilities.message(player, messageHandler.getPrefix() + "#00D4FFThere are no new updates for the plugin. Enjoy!");
        return;
      }

      PluginDescriptionFile pdf = plugin.getDescription();
      Utilities.message(player,
        "#00D4FFA new version of #FF003E" + pdf.getName() + " #00D4FFis avaliable!",
        "#00D4FFCurrent Version: #FF003E" + pdf.getVersion() + " #00D4FF> New Version: #FF003E" + version,
        "#00D4FFGrab it here: #FF003Ehttps://github.com/OmegaWeaponDev/DeathWarden"
      );
    });
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
    final Player player = playerQuitEvent.getPlayer();

    // Remove the player from the map, if they have an entry stored in it
    plugin.getSettingsHandler().getDeathLocation().remove(player.getUniqueId());
    plugin.getSettingsHandler().getDeathEffectsMap().remove(player.getUniqueId());
    MenuCreator.getOpenInventories().remove(player.getUniqueId());
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerRespawn(PlayerRespawnEvent playerRespawnEvent) {
    final Player player = playerRespawnEvent.getPlayer();

    if(configFile.getBoolean("Disabled_Worlds.Enabled")) {
      for(String world : configFile.getStringList("Disabled_Worlds.Worlds")) {
        if(player.getWorld().getName().equals(world)) {
          return;
        }
      }
    }

    DeathCommands deathCommands = new DeathCommands(plugin, player, null);
    deathCommands.respawnCommands();
  }
}
