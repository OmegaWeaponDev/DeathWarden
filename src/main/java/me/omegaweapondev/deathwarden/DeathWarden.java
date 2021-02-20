package me.omegaweapondev.deathwarden;

import me.omegaweapondev.deathwarden.commands.*;
import me.omegaweapondev.deathwarden.events.*;
import me.omegaweapondev.deathwarden.menus.DeathEffectsMenu;
import me.omegaweapondev.deathwarden.menus.ParticleMenu;
import me.omegaweapondev.deathwarden.menus.SoundMenu;
import me.omegaweapondev.deathwarden.utils.Placeholders;
import me.omegaweapondev.deathwarden.utils.SettingsHandler;
import me.omegaweapondev.deathwarden.utils.UserDataHandler;
import me.ou.library.SpigotUpdater;
import me.ou.library.Utilities;
import me.ou.library.menus.MenuCreator;
import net.milkbowl.vault.economy.Economy;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class DeathWarden extends JavaPlugin {
  private DeathWarden plugin;
  private SettingsHandler settingsHandler;
  private UserDataHandler userData;
  private Economy econ = null;

  private ParticleMenu particleMenu;
  private SoundMenu soundMenu;
  private DeathEffectsMenu deathEffectsMenu;

  @Override
  public void onEnable() {
    plugin = this;
    settingsHandler = new SettingsHandler(plugin);

    initialSetup();
    getSettingsHandler().setupConfigs();
    getSettingsHandler().configUpdater();
    setupCommands();
    setupEvents();
    setupEconomy();
    spigotUpdater();
    populateMapOnReload();
  }

  @Override
  public void onDisable() {
    for(Player player : Bukkit.getOnlinePlayers()) {
      player.closeInventory();
    }

    if(!MenuCreator.getOpenInventories().isEmpty()) {
      if(particleMenu != null) {
        particleMenu.deleteInventory();
      }

      if(soundMenu != null) {
        soundMenu.deleteInventory();
      }

      if(deathEffectsMenu != null) {
        deathEffectsMenu.deleteInventory();
      }
    }

    getSettingsHandler().getDeathEffectsMap().clear();
    getSettingsHandler().getDeathLocation().clear();
    getSettingsHandler().getPenaltyMap().clear();

    super.onDisable();
  }

  public void onReload() {
    getSettingsHandler().reloadFiles();
  }

  private void initialSetup() {

    // Setup the instance for OU Library
    Utilities.setInstance(this);

    if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
      new Placeholders(this).register();
    }

    if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
      Utilities.logWarning(true,
        "DeathWarden requires PlaceholderAPI to be installed if you are wanting to use the placeholders",
        "You can install PlaceholderAPI here: https://www.spigotmc.org/resources/placeholderapi.6245/ "
      );
    }

    // Setup bStats
    final int bstatsPluginId = 7492;
    Metrics metrics = new Metrics(this, bstatsPluginId);

    // Logs a message to console, saying that the plugin has enabled correctly.
    Utilities.logInfo(true,

    " _____ _    _",
    "|  _  \\ |  | |",
    "| | | | |  | |  DeathWarden v" + plugin.getDescription().getVersion() + " By OmegaWeaponDev",
    "| | | | |/\\| |  Become the death warden of your server and control how players die!",
    "| |/ /\\  /\\  /  Currently supporting Spigot 1.13 - 1.16",
    "|___/  \\/  \\/"
    );
  }

  private void setupCommands() {
    // Register the commands
    Utilities.logInfo(true, "Registering Commands...");

    Utilities.setCommand().put("deathwarden", new DeathWardenCommnad(plugin));
    Utilities.setCommand().put("back", new BackCommand(plugin));
    Utilities.setCommand().put("deatheffects", new DeathEffectsCommand(plugin));
    Utilities.setCommand().put("deathcount", new DeathCountCommand(plugin));
    Utilities.setCommand().put("deathwardenreset", new ResetPlayerCommand(plugin));
    Utilities.setCommand().put("totaldeathsreset", new ResetTotalDeathsLogCommand(plugin));
    Utilities.setCommand().put("dwdebug", new DebugCommand(plugin));

    Utilities.registerCommands();
    Utilities.logInfo(true, "Commands Registered: " + Utilities.setCommand().size());
  }

  private void setupEvents() {
    // Register the events
    Utilities.registerEvents(new PlayerListener(plugin), new PlayerDeathListener(plugin), new RewardsListener(plugin), new MenuListener(), new EntityDeathListener(plugin)) ;
  }

  private boolean setupEconomy() {
    if (getServer().getPluginManager().getPlugin("Vault") == null) {
      Utilities.logWarning(true,
        "DeathWarden requires vault to be installed if you want to handle giving/taking player's money.",
        "Since Vault is not installed, these actions will not work.",
        "To install vault, please visit: https://www.spigotmc.org/resources/vault.34315/"
      );
      return false;
    }
    RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
    if (rsp == null) {
      return false;
    }
    econ = rsp.getProvider();
    return econ != null;
  }

  private void spigotUpdater() {

    // The Updater
    new SpigotUpdater(this, 74788).getVersion(version -> {
      int spigotVersion = Integer.parseInt(version.replace(".", ""));
      int pluginVersion = Integer.parseInt(this.getDescription().getVersion().replace(".", ""));

      if(pluginVersion >= spigotVersion) {
        Utilities.logInfo(true, "There are no new updates for the plugin. Enjoy!");
        return;
      }

      PluginDescriptionFile pdf = this.getDescription();
      Utilities.logWarning(true,
        "A new version of " + pdf.getName() + " is avaliable!",
        "Current Version: " + pdf.getVersion() + " > New Version: " + version,
        "Grab it here: https://github.com/OmegaWeaponDev/DeathWarden"
      );
    });
  }

  private void populateMapOnReload() {
    if(Bukkit.getOnlinePlayers().size() == 0) {
      return;
    }

    if(!getSettingsHandler().getConfigFile().getConfig().getBoolean("Death_Effects_Restart")) {
      return;
    }

    for(Player player : Bukkit.getOnlinePlayers()) {
      if(Utilities.checkPermissions(player, true, "deathwarden.deatheffects", "deathwarden.admin")) {
        getSettingsHandler().getDeathEffectsMap().put(player.getUniqueId(), getUserData(player, player.getUniqueId()).getPlayerData().getBoolean("Death_Effects.Enabled"));
      }
    }
  }

  public Economy getEconomy() {
    return econ;
  }

  public SettingsHandler getSettingsHandler() {
    return settingsHandler;
  }

  public UserDataHandler getUserData(Player player, UUID playerUUID) {
    userData = new UserDataHandler(plugin, player, playerUUID);
    return userData;
  }
}
