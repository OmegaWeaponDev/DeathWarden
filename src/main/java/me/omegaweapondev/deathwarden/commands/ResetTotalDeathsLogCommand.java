package me.omegaweapondev.deathwarden.commands;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.CreaturesKilledUtil;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.ou.library.Utilities;
import me.ou.library.commands.GlobalCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ResetTotalDeathsLogCommand extends GlobalCommand implements TabCompleter {
  private final DeathWarden plugin;
  private final MessageHandler messageHandler;
  private final CreaturesKilledUtil creaturesKilledUtil;

  public ResetTotalDeathsLogCommand(final DeathWarden plugin) {
    this.plugin = plugin;
    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
    creaturesKilledUtil = new CreaturesKilledUtil();
  }

  @Override
  protected void execute(CommandSender commandSender, String[] strings) {
    if(commandSender instanceof Player) {
      final Player player = ((Player) commandSender).getPlayer();

      if(!Utilities.checkPermissions(player, true, "deathwarden.resettotaldeaths", "deathwarden.admin")) {
        Utilities.message(player, messageHandler.string("No_Permission", "#FF003ESorry, you do not have permission to use that."));
        return;
      }

      for(String creature : creaturesKilledUtil.creaturesKilled()) {
        plugin.getSettingsHandler().getTotalDeathsLog().getConfig().set("Creatures_Killed." + creature, 0);
      }
      plugin.getSettingsHandler().getTotalDeathsLog().saveConfig();
      Utilities.message(player, messageHandler.string("Reset_Total_Deaths_Log", "#00D4FFTotal Deaths Log has now been reset."));
      return;
    }

    for(String creature : creaturesKilledUtil.creaturesKilled()) {
      plugin.getSettingsHandler().getTotalDeathsLog().getConfig().set("Creatures_Killed." + creature, 0);
    }

    plugin.getSettingsHandler().getTotalDeathsLog().saveConfig();
    Utilities.logInfo(true, messageHandler.console("Reset_Total_Deaths_Log", "#00D4FFTotal Deaths Log has now been reset."));
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
    return Collections.emptyList();
  }
}
