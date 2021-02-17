package me.omegaweapondev.deathwarden.commands;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.events.PlayerDeathListener;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.omegaweapondev.deathwarden.utils.StorageManager;
import me.ou.library.Utilities;
import me.ou.library.commands.PlayerCommand;
import org.bukkit.entity.Player;

public class BackCommand extends PlayerCommand {
  private final DeathWarden plugin;
  private StorageManager storageManager;
  private final MessageHandler messageHandler;

  public BackCommand(final DeathWarden plugin) {
    this.plugin = plugin;
    storageManager = new StorageManager(plugin);
    messageHandler = new MessageHandler(plugin, plugin.getStorageManager().getMessagesFile().getConfig());
  }

  @Override
  protected void execute(Player player, String[] strings) {
    storageManager = new StorageManager(plugin, player, player.getUniqueId());

    if(!Utilities.checkPermissions(player, true, "deathwarden.back", "deathwarden.admin")) {
      Utilities.message(player, messageHandler.string("No_Permission", "#ff4a4aSorry, you do not have permission to use that command."));
      return;
    }

    if(PlayerDeathListener.getDeathLocation().get(player.getUniqueId()) == null) {
      return;
    }

    player.teleport(PlayerDeathListener.getDeathLocation().get(player.getUniqueId()));
    PlayerDeathListener.getDeathLocation().remove(player.getUniqueId());
    Utilities.message(player, "Back_On_Death", "#00D4FFYou have returned to your last known death location.");
  }
}
