package me.omegaweapondev.deathwarden.commands;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.ou.library.Utilities;
import me.ou.library.builders.TabCompleteBuilder;
import me.ou.library.commands.GlobalCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DeathWardenCommnad extends GlobalCommand implements TabCompleter {
  private final DeathWarden plugin;
  private final MessageHandler messageHandler;

  public DeathWardenCommnad(final DeathWarden plugin){
    this.plugin = plugin;
    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
  }

  @Override
  protected void execute(CommandSender commandSender, String[] strings) {
    if(strings.length == 0) {
      invalidArgsCommand(commandSender);
      return;
    }

    if(strings.length == 1) {
      switch(strings[0]) {
        case "help":
          helpCommand(commandSender);
          break;
        case "version":
          versionCommand(commandSender);
          break;
        case "reload":
          reloadCommand(commandSender);
          break;
        default:
          invalidArgsCommand(commandSender);
          break;
      }
    }
  }

  private void reloadCommand(final CommandSender sender) {
    if(sender instanceof Player) {
      Player player = (Player) sender;

      if(!Utilities.checkPermissions(player, true, "deathwarden.reload", "deathwarden.admin")) {
        Utilities.message(player, messageHandler.string("No_Permission", "#ff4a4aSorry, you do not have permission for that."));
        return;
      }

      plugin.onReload();
      Utilities.message(player, messageHandler.string("Reload_Message", "#560900DeathWarden #00D4FFhas successfully been reloaded"));
      return;
    }

    if(sender instanceof ConsoleCommandSender) {
      plugin.onReload();
      Utilities.logInfo(true, messageHandler.console("Reload_Message", "DeathWarden has successfully been reloaded."));
    }
  }

  private void helpCommand(final CommandSender sender) {
    if(sender instanceof Player) {
      Player player = (Player) sender;

      Utilities.message(player,
        "#14abc9===========================================",
        " #6928f7DeathWarden #ff4a4av" + plugin.getDescription().getVersion() + " #14abc9By OmegaWeaponDev",
        "#14abc9===========================================",
        "  #00D4FFReload Command: #ff4a4a/deathwarden reload",
        "  #00D4FFVersion Command: #ff4a4a/deathwarden version",
        "  #00D4FFHelp Command: #ff4a4a/deathwarden help",
        "  #00D4FFDeath Effects command: #ff4a4a/deatheffects",
        "  #00D4FFEdit Death Effects Command: #ff4a4a/deatheffects edit",
        "  #00D4FFDebug Command: #ff4a4a/dwdebug",
        "  #00D4FFBack Command: #ff4a4a/back",
        "  #00D4FFReset Player Command: #ff4a4a/dwreset <player name>",
        "  #00D4FFReset Total Deaths Log Command: #ff4a4a/tdreset",
        "#14abc9==========================================="
      );
      return;
    }

    if(sender instanceof ConsoleCommandSender){
      Utilities.logInfo(true,
        "===========================================",
        " DeathWarden v" + plugin.getDescription().getVersion() + " By OmegaWeaponDev",
        "===========================================",
        "  Reload Command: /deathwarden reload",
        "  Version Command: /deathwarden version",
        "  Help Command: /deathwarden help",
        "  Death Effects command: /deatheffects",
        "  Edit Death Effects Command: /deatheffects edit",
        "  Debug Command: /dwdebug",
        "  Back Command: /back",
        "  Reset Player Command: /dwreset <player name>",
        "  Reset Total Deaths Log Command: /tdreset",
        "==========================================="
      );
    }
  }

  private void versionCommand(final CommandSender sender) {
    if(sender instanceof Player) {
      Player player = (Player) sender;

      Utilities.message(player, messageHandler.getPrefix() + "#00D4ffStylizer #ff4a4av" + plugin.getDescription().getVersion() + "#00D4FF By OmegaWeaponDev");
      return;
    }

    if(sender instanceof ConsoleCommandSender) {
      Utilities.logInfo(true, ChatColor.stripColor(messageHandler.getPrefix() + "Stylizer v" + plugin.getDescription().getVersion() + " By OmegaWeaponDev"));
    }
  }

  private void invalidArgsCommand(final CommandSender sender) {
    if(sender instanceof Player) {
      Player player = (Player) sender;

      Utilities.message(player,
        "#14abc9===========================================",
        " #6928f7DeathWarden #ff4a4av" + plugin.getDescription().getVersion() + " #14abc9By OmegaWeaponDev",
        "#14abc9===========================================",
        "  #00D4FFReload Command: #ff4a4a/deathwarden reload",
        "  #00D4FFVersion Command: #ff4a4a/deathwarden version",
        "  #00D4FFHelp Command: #ff4a4a/deathwarden help",
        "  #00D4FFDeath Effects command: #ff4a4a/deatheffects",
        "  #00D4FFEdit Death Effects Command: #ff4a4a/deatheffects edit",
        "  #00D4FFDebug Command: #ff4a4a/dwdebug",
        "  #00D4FFBack Command: #ff4a4a/back",
        "  #00D4FFReset Player Command: #ff4a4a/dwreset <player name>",
        "  #00D4FFReset Total Deaths Log Command: #ff4a4a/tdreset",
        "#14abc9==========================================="
      );
      return;
    }

    if(sender instanceof ConsoleCommandSender) {
      Utilities.logInfo(true,
        "===========================================",
        " DeathWarden v" + plugin.getDescription().getVersion() + " By OmegaWeaponDev",
        "===========================================",
        "  Reload Command: /deathwarden reload",
        "  Version Command: /deathwarden version",
        "  Help Command: /deathwarden help",
        "  Death Effects command: /deatheffects",
        "  Edit Death Effects Command: /deatheffects edit",
        "  Debug Command: /dwdebug",
        "  Back Command: /back",
        "  Reset Player Command: /dwreset <player name>",
        "  Reset Total Deaths Log Command: /tdreset",
        "==========================================="
      );
    }
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
    if(strings.length <= 1) {
      return new TabCompleteBuilder(commandSender)
        .checkCommand("version", true, "deathwarden.admin")
        .checkCommand("help", true, "deathwarden.admin")
        .checkCommand("reload", true, "deathwarden.reload", "deathwarden.admin")
        .build(strings[0]);
    }

    return Collections.emptyList();
  }
}
