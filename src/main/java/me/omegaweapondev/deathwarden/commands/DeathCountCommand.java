package me.omegaweapondev.deathwarden.commands;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.omegaweapondev.deathwarden.utils.UserDataHandler;
import me.ou.library.Utilities;
import me.ou.library.commands.PlayerCommand;
import org.bukkit.entity.Player;

public class DeathCountCommand extends PlayerCommand {
  private final DeathWarden plugin;
  private final MessageHandler messageHandler;

  private UserDataHandler userData;

  public DeathCountCommand(final DeathWarden plugin) {
    this.plugin = plugin;
    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
  }

  @Override
  protected void execute(Player player, String[] strings) {
    userData = new UserDataHandler(plugin, player, player.getUniqueId());
    int deathCount = userData.getPlayerData().getInt("Death_Count");

    if(!Utilities.checkPermissions(player, true, "deathwarden.deathcount", "deathwarden.admin")) {
     Utilities.message(player, messageHandler.string("No_Permission", "#ff4a4aSorry, you do not have permission to use that command."));
     return;
    }

    if(deathCount == 0) {
      Utilities.message(player, messageHandler.string("Death_Count.No_Deaths", "#00D4FFYou have not died yet!"));
      return;
    }

    Utilities.message(player, messageHandler.string("Death_Count.Has_Died", "#00D4FFYou have died #FF003E %deathCount% #00D4FFtimes already!").replace("%deathCount%", String.valueOf(deathCount)));
  }
}
