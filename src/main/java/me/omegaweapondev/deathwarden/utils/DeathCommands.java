package me.omegaweapondev.deathwarden.utils;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.ou.library.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class DeathCommands {
  private final DeathWarden plugin;
  private final Player player;
  private final Player killer;
  private final FileConfiguration configFile;

  public DeathCommands(final DeathWarden plugin, final Player player, @Nullable final Player killer) {
    this.plugin = plugin;
    this.player = player;
    this.killer = killer;

    configFile = plugin.getSettingsHandler().getConfigFile().getConfig();
  }

  public void respawnCommands() {
    final ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

    if(!configFile.getBoolean("Respawn_Commands.Enabled")) {
      return;
    }

    if(Utilities.checkPermissions(player, true, "deathwarden.commands.respawn.exempt", "deathwarden.admin")) {
      return;
    }

    for(String command : configFile.getStringList("Respawn_Commands.Commands")) {
      Bukkit.dispatchCommand(console, command.replace("%player%", player.getName()));
    }
  }

  public void deathCommands() {
    final CommandSender console = Bukkit.getServer().getConsoleSender();

    if(!configFile.getBoolean("Death_Commands.Enabled")) {
      return;
    }

    if(killer == null) {
      return;
    }

    if(Utilities.checkPermissions(player, true, "deathwarden.commands.death.exempt", "deathwarden.admin")) {
      return;
    }

    for(String command : configFile.getStringList("Death_Commands.Commands")) {
      Bukkit.dispatchCommand(console, command.replace("%player%", player.getName()).replace("%killer%", killer.getName()));
    }
  }

  public void killCommands() {
    final CommandSender console = Bukkit.getServer().getConsoleSender();

    if(!configFile.getBoolean("Kill_Commands.Enabled")) {
      return;
    }

    if(killer == null) {
      return;
    }

    if(Utilities.checkPermissions(killer, true, "deathwarden.commands.kill.exempt", "deathwarden.admin")) {
      return;
    }

    for(String command : configFile.getStringList("Kill_Commands.Commands")) {
      Bukkit.dispatchCommand(console, command.replace("%player%", player.getName()).replace("%killer%", killer.getName()));
    }
  }

  public void deathByNonPlayer() {
    final CommandSender console = Bukkit.getServer().getConsoleSender();

    if(!configFile.getBoolean("Death_By_NonPlayer.Enabled")) {
      return;
    }

    if(Utilities.checkPermissions(player, true, "deathwarden.commands.deathbynonplayer.exempt", "deathwarden.admin")) {
      return;
    }

    for(String command : configFile.getStringList("Death_By_NonPlayer.Commands")) {
      Bukkit.dispatchCommand(console, command.replace("%player%", player.getName()));
    }
  }
}
