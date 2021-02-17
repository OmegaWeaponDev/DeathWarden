package me.omegaweapondev.deathwarden.commands;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.menus.DeathEffectsMenu;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.omegaweapondev.deathwarden.utils.StorageManager;
import me.ou.library.Utilities;
import me.ou.library.commands.PlayerCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class DeathEffectsCommand extends PlayerCommand {
  private final DeathWarden plugin;
  private final MessageHandler messageHandler;
  private final FileConfiguration deathEffectsConfig;
  private final FileConfiguration configFile;

  private DeathEffectsMenu deathEffectsMenu;
  private StorageManager storageManager;

  public DeathEffectsCommand(final DeathWarden plugin) {
    this.plugin = plugin;
    messageHandler = new MessageHandler(plugin, plugin.getStorageManager().getMessagesFile().getConfig());
    deathEffectsConfig = plugin.getStorageManager().getDeathEffectMenus().getConfig();
    configFile = plugin.getStorageManager().getConfigFile().getConfig();
  }

  @Override
  protected void execute(Player player, String[] strings) {
    storageManager = new StorageManager(plugin, player, player.getUniqueId());

    if(strings.length == 0) {
      if(!Utilities.checkPermissions(player, true, "deathwarden.deatheffects", "deathwarden.admin")) {
        Utilities.message(player, messageHandler.string("No_Permission", "#ff4a4aSorry, you do not have permission to use that command."));
        return;
      }

      if(storageManager.getUserBoolean("Death_Effects.Enabled")) {
        storageManager.setUserBoolean("Death_Effects.Enabled", false);
        if(configFile.getBoolean("Death_Effects_Login") && Utilities.checkPermissions(player, false, "deathwarden.deatheffects.login", "deathwarden.admin")) {
          storageManager.getDeathEffectsMap().put(player.getUniqueId(), false);
        }
        Utilities.message(player, messageHandler.string("Death_Effects_Removed", "#00D4FFBack to the old boring deaths."));
        return;
      }

      storageManager.setUserBoolean("Death_Effects.Enabled", true);
      if(configFile.getBoolean("Death_Effects_Login") && Utilities.checkPermissions(player, false, "deathwarden.deatheffects.login", "deathwarden.admin")) {
        storageManager.getDeathEffectsMap().put(player.getUniqueId(), true);
      }
      Utilities.message(player, messageHandler.string("Death_Effects_Applied", "#00D4FFEnjoy your new cool death effects!"));
      return;
    }

    if(strings.length == 1 && strings[0].equalsIgnoreCase("edit")) {
      if(!Utilities.checkPermissions(player, true, "deathwarden.deatheffects.edit", "deathwarden.admin")) {
        Utilities.message(player, messageHandler.string("No_Permission", "#ff4a4aSorry, you do not have permission to use that command."));
        return;
      }

      deathEffectsMenu = new DeathEffectsMenu(plugin, 3, deathEffectsConfig.getString("Death_Effects_Menu.Menu_Title"), "#00D4FFDeath Effects Menu");
      deathEffectsMenu.openInventory(player);
    }
  }
}
