package me.omegaweapondev.deathwarden.commands;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.menus.DeathEffectsMenu;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.omegaweapondev.deathwarden.utils.UserDataHandler;
import me.ou.library.Utilities;
import me.ou.library.builders.TabCompleteBuilder;
import me.ou.library.commands.PlayerCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class DeathEffectsCommand extends PlayerCommand implements TabCompleter {
  private final DeathWarden plugin;
  private final MessageHandler messageHandler;
  private final FileConfiguration deathEffectsConfig;
  private final FileConfiguration configFile;

  private UserDataHandler userData;
  private DeathEffectsMenu deathEffectsMenu;

  public DeathEffectsCommand(final DeathWarden plugin) {
    this.plugin = plugin;
    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
    deathEffectsConfig = plugin.getSettingsHandler().getDeathEffectMenus().getConfig();
    configFile = plugin.getSettingsHandler().getConfigFile().getConfig();
  }

  @Override
  protected void execute(Player player, String[] strings) {
    userData = new UserDataHandler(plugin, player, player.getUniqueId());

    if(strings.length == 0) {
      if(!Utilities.checkPermissions(player, true, "deathwarden.deatheffects", "deathwarden.admin")) {
        Utilities.message(player, messageHandler.string("No_Permission", "#ff4a4aSorry, you do not have permission to use that command."));
        return;
      }

      if(userData.getPlayerData().getBoolean("Death_Effects.Enabled")) {
        userData.getPlayerData().set("Death_Effects.Enabled", false);
        userData.savePlayerData();

        if(configFile.getBoolean("Death_Effects_Login") && Utilities.checkPermissions(player, false, "deathwarden.deatheffects.login", "deathwarden.admin")) {
          plugin.getSettingsHandler().getDeathEffectsMap().put(player.getUniqueId(), false);
        }
        Utilities.message(player, messageHandler.string("Death_Effects_Removed", "#00D4FFBack to the old boring deaths."));
        return;
      }

      userData.getPlayerData().set("Death_Effects.Enabled", true);
      userData.savePlayerData();
      if(configFile.getBoolean("Death_Effects_Login") && Utilities.checkPermissions(player, false, "deathwarden.deatheffects.login", "deathwarden.admin")) {
        plugin.getSettingsHandler().getDeathEffectsMap().put(player.getUniqueId(), true);
      }
      Utilities.message(player, messageHandler.string("Death_Effects_Applied", "#00D4FFEnjoy your new cool death effects!"));
      return;
    }

    if(strings.length == 1 && strings[0].equalsIgnoreCase("edit")) {
      if(!Utilities.checkPermissions(player, true, "deathwarden.deatheffects.edit", "deathwarden.admin")) {
        Utilities.message(player, messageHandler.string("No_Permission", "#ff4a4aSorry, you do not have permission to use that command."));
        return;
      }

      deathEffectsMenu = new DeathEffectsMenu(plugin, 3, deathEffectsConfig.getString("Death_Effects_Menu.Menu_Title"), "#00D4FFDeath Effects Menu");
      deathEffectsMenu.openInventory(player);
    }
  }

  @Override
  public List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
    if(strings.length <= 1) {
      return new TabCompleteBuilder(commandSender)
        .checkCommand("edit", true, "deathwarden.deatheffects.edit", "deathwarden.admin")
        .build(strings[0]);
    }

    return Collections.emptyList();
  }
}
