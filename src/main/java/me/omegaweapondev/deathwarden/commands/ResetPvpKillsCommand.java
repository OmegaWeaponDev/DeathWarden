package me.omegaweapondev.deathwarden.commands;

import me.omegaweapondev.deathwarden.DeathWarden;
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

public class ResetPvpKillsCommand extends GlobalCommand implements TabCompleter {
  private final DeathWarden plugin;
  private final MessageHandler messageHandler;

  public ResetPvpKillsCommand(final DeathWarden plugin) {
    this.plugin = plugin;
    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
  }

  @Override
  protected void execute(CommandSender commandSender, String[] strings) {
    if(!(commandSender instanceof Player)) {
      plugin.getSettingsHandler().getPvpLog().getConfig().set("Pvp_Kills", "none");
      plugin.getSettingsHandler().getPvpLog().saveConfig();
      Utilities.logInfo(true, "The PvpLogs file has been reset.");
    }

    Player player = (Player) commandSender;

    if(!Utilities.checkPermissions(player, true, "deathwarden.reset.pvplog", "deathwarden.admin")) {
      Utilities.message(player, messageHandler.string("No_Permission", "#ff4a4aSorry, you do not have permission to use that command."));
      return;
    }

    plugin.getSettingsHandler().getPvpLog().getConfig().set("Pvp_Kills", "none");
    plugin.getSettingsHandler().getPvpLog().saveConfig();

    Utilities.message(player, messageHandler.string("PvpKill_Reset", "#00D4FFThe PvpLogs file has been reset."));

  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
    return Collections.emptyList();
  }
}
