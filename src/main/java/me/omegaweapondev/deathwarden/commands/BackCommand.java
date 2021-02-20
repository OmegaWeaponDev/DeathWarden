package me.omegaweapondev.deathwarden.commands;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.ou.library.Utilities;
import me.ou.library.commands.PlayerCommand;
import org.bukkit.entity.Player;

public class BackCommand extends PlayerCommand {
  private final DeathWarden plugin;
  private final MessageHandler messageHandler;

  public BackCommand(final DeathWarden plugin) {
    this.plugin = plugin;
    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
  }

  @Override
  protected void execute(Player player, String[] strings) {
    if(!Utilities.checkPermissions(player, true, "deathwarden.back", "deathwarden.admin")) {
      Utilities.message(player, messageHandler.string("No_Permission", "#ff4a4aSorry, you do not have permission to use that command."));
      return;
    }

    if(plugin.getSettingsHandler().getDeathLocation().get(player.getUniqueId()) == null) {
      return;
    }

    player.teleport(plugin.getSettingsHandler().getDeathLocation().get(player.getUniqueId()));
    plugin.getSettingsHandler().getDeathLocation().remove(player.getUniqueId());
    Utilities.message(player, messageHandler.string("Back_On_Death", "#00D4FFYou have returned to your last known death location."));
  }
}
