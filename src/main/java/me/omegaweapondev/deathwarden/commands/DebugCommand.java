package me.omegaweapondev.deathwarden.commands;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.ou.library.Utilities;
import me.ou.library.commands.GlobalCommand;
import me.ou.library.menus.MenuCreator;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class DebugCommand extends GlobalCommand {
  private final DeathWarden plugin;
  private final MessageHandler messageHandler;

  public DebugCommand(final DeathWarden plugin) {
    this.plugin = plugin;
    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
  }

  @Override
  protected void execute(CommandSender commandSender, String[] strings) {
    if(commandSender instanceof Player) {
      debugPlayer(((Player) commandSender).getPlayer());
      return;
    }
    debugConsole();
  }

  private void debugPlayer(final Player player) {

    if(!Utilities.checkPermissions(player, true, "deathwarden.debug", "deathwarden.admin")) {
      Utilities.message(player, messageHandler.string("No_Permission", "#570000I'm sorry, but you do not have permission to do that!"));
      return;
    }

    StringBuilder plugins = new StringBuilder();

    for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
      plugins.append("#ff4a4a").append(plugin.getName()).append(" ").append(plugin.getDescription().getVersion()).append("#14abc9, ");
    }

    Utilities.message(player,
      "#14abc9===========================================",
      " #6928f7DeathWarden #ff4a4av" + plugin.getDescription().getVersion() + " #14abc9By OmegaWeaponDev",
      "#14abc9===========================================",
      " #14abc9Server Brand: #ff4a4a" + Bukkit.getName(),
      " #14abc9Server Version: #ff4a4a" + Bukkit.getServer().getVersion(),
      " #14abc9Online Mode: #ff4a4a" + Bukkit.getOnlineMode(),
      " #14abc9Players Online: #ff4a4a" + Bukkit.getOnlinePlayers().size() + " / " + Bukkit.getMaxPlayers(),
      " #14abc9DeathWarden Commands: #ff4a4a" + Utilities.setCommand().size() + " / 7 #14abc9registered",
      " #14abc9Open Inventories: #ff4a4a" + MenuCreator.getOpenInventories().size(),
      " #14abc9Unique Inventories: #ff4a4a" + MenuCreator.getInventoriesByUUID().size(),
      " #14abc9Currently Installed Plugins...",
      " " + plugins.toString(),
      "#14abc9==========================================="
    );
  }

  private void debugConsole() {
    StringBuilder plugins = new StringBuilder();

    for(Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
      plugins.append(plugin.getName()).append(" ").append(plugin.getDescription().getVersion()).append(", ");
    }

    Utilities.logInfo(true,
      "===========================================",
      " DeathWarden v" + plugin.getDescription().getVersion() + " By OmegaWeaponDev",
      "===========================================",
      " Server Brand: " + Bukkit.getName(),
      " Server Version: " + Bukkit.getServer().getVersion(),
      " Online Mode: " + Bukkit.getServer().getOnlineMode(),
      " Players Online: " + Bukkit.getOnlinePlayers().size() + " / " + Bukkit.getMaxPlayers(),
      " DeathWarden Commands: " + Utilities.setCommand().size() + " / 7 registered",
      " Open Inventories: " + MenuCreator.getOpenInventories().size(),
      " Unique Inventories: " + MenuCreator.getInventoriesByUUID().size(),
      " Currently Installed Plugins...",
      " " + plugins.toString(),
      "==========================================="
    );
  }
}
