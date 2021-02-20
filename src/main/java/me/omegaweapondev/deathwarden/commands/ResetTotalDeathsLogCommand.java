package me.omegaweapondev.deathwarden.commands;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.ou.library.Utilities;
import me.ou.library.commands.GlobalCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ResetTotalDeathsLogCommand extends GlobalCommand {
  private final DeathWarden plugin;
  private final MessageHandler messageHandler;

  public ResetTotalDeathsLogCommand(final DeathWarden plugin) {
    this.plugin = plugin;
    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
  }

  @Override
  protected void execute(CommandSender commandSender, String[] strings) {
    if(commandSender instanceof Player) {
      final Player player = ((Player) commandSender).getPlayer();

      if(!Utilities.checkPermissions(player, true, "deathwarden.resettotaldeaths", "deathwarden.admin")) {
        Utilities.message(player, messageHandler.string("No_Permission", "#FF003ESorry, you do not have permission to use that."));
        return;
      }

      for(int i = 0; i < plugin.getSettingsHandler().getTotalDeathsLog().getConfig().getConfigurationSection("Creatures_Killed").getKeys(false).size(); i++) {
        plugin.getSettingsHandler().getTotalDeathsLog().getConfig().set("Creatures_Killed." + i, 0);
      }
      plugin.getSettingsHandler().getTotalDeathsLog().saveConfig();
      Utilities.message(player, messageHandler.string("Reset_Total_Deaths_Log", "#00D4FFTotal Deaths Log has now been reset."));
      return;
    }

    for(int i = 0; i < plugin.getSettingsHandler().getTotalDeathsLog().getConfig().getConfigurationSection("Creatures_Killed").getKeys(false).size(); i++) {
      plugin.getSettingsHandler().getTotalDeathsLog().getConfig().set("Creatures_Killed." + i, 0);
    }

    plugin.getSettingsHandler().getTotalDeathsLog().saveConfig();
    Utilities.logInfo(true, messageHandler.console("Reset_Total_Deaths_Log", "#00D4FFTotal Deaths Log has now been reset."));
  }
}
