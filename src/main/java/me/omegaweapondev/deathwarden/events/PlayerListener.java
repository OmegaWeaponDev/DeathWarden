package me.omegaweapondev.deathwarden.events;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.DeathCommands;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.omegaweapondev.deathwarden.utils.StorageManager;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {
  private final DeathWarden plugin;
  private final FileConfiguration configFile;
  private final MessageHandler messageHandler;
  private static final Map<UUID, Boolean> deathEffectsMap = new HashMap<>();

  private StorageManager storageManager;

  public PlayerListener(final DeathWarden plugin) {
    this.plugin = plugin;
    configFile = plugin.getStorageManager().getConfigFile().getConfig();
    messageHandler = new MessageHandler(plugin, plugin.getStorageManager().getMessagesFile().getConfig());
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
    // Get the player that is joining
    final Player player = playerJoinEvent.getPlayer();

    storageManager = new StorageManager(plugin, player, player.getUniqueId());
    storageManager.createUserFile();

    // Check if the player has permission to receive plugin update messages.
    if(!Utilities.checkPermissions(player, true, "deathwarden.update", "deathwarden.admin")) {
      return;
    }

    if(configFile.getBoolean("Death_Effects_Login") && Utilities.checkPermissions(player, true, "deathwarden.deatheffects.login", "deathwarden.admin")) {
      storageManager.getDeathEffectsMap().put(player.getUniqueId(), true);
    } else {
      storageManager.getDeathEffectsMap().put(player.getUniqueId(), false);
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
    storageManager.getDeathLocation().remove(player.getUniqueId());
    storageManager.getDeathEffectsMap().remove(player.getUniqueId());
    MenuCreator.getOpenInventories().remove(player.getUniqueId());
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onPlayerRespawn(PlayerRespawnEvent playerRespawnEvent) {
    final Player player = playerRespawnEvent.getPlayer();

    DeathCommands deathCommands = new DeathCommands(plugin, player, null);
    deathCommands.respawnCommands();
  }

}
