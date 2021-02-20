package me.omegaweapondev.deathwarden.commands;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.omegaweapondev.deathwarden.utils.UserDataHandler;
import me.ou.library.Utilities;
import me.ou.library.commands.GlobalCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetPlayerCommand extends GlobalCommand {
  private final DeathWarden plugin;
  private final MessageHandler messageHandler;

  private UserDataHandler userData;

  public ResetPlayerCommand(final DeathWarden plugin) {
    this.plugin = plugin;
    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
  }

  @Override
  protected void execute(CommandSender commandSender, String[] strings) {
    if(!(commandSender instanceof Player)) {
      if(strings.length == 0) {
        Utilities.logInfo(true, "Please provide either the players name, or the word `all` to reset all player data");
        return;
      }

      Player player = Bukkit.getPlayer(strings[0]);

      if(player == null) {
        return;
      }
      userData = new UserDataHandler(plugin, player, player.getUniqueId());
      userData.resetPlayerData();

      Utilities.logInfo(true, "The player data for " + player.getName() + " has been reset!");
      return;
    }

    final Player player = (Player) commandSender;

    if(strings.length == 0) {
      Utilities.message(player, "#ff4a4aPlease provide either the players name, or the word `all` to reset all player data");
      return;
    }

    if(!Utilities.checkPermissions(player, true, "deathwarden.reset.player", "deathwarden.admin")) {
      Utilities.message(player, messageHandler.string("No_Permission", "#ff4a4aSorry, you do not have permission to use that command."));
      return;
    }

    Player resetPlayer = Bukkit.getPlayer(strings[0]);

    if(resetPlayer == null) {
      return;
    }
    userData = new UserDataHandler(plugin, resetPlayer, resetPlayer.getUniqueId());
    userData.resetPlayerData();

    Utilities.message(player, messageHandler.string("Reset_Player_Data", "#00D4FFThe player data for #FF003E%player% #00D4FFhas been reset!").replace("%player%", resetPlayer.getName()));
  }
}
