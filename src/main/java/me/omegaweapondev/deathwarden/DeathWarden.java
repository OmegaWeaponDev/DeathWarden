package me.omegaweapondev.deathwarden;

import me.omegaweapondev.deathwarden.commands.*;
import me.omegaweapondev.deathwarden.events.MenuListener;
import me.omegaweapondev.deathwarden.events.PlayerDeathListener;
import me.omegaweapondev.deathwarden.events.PlayerListener;
import me.omegaweapondev.deathwarden.events.RewardsListener;
import me.omegaweapondev.deathwarden.menus.DeathEffectsMenu;
import me.omegaweapondev.deathwarden.menus.ParticleMenu;
import me.omegaweapondev.deathwarden.menus.SoundMenu;
import me.omegaweapondev.deathwarden.utils.Placeholders;
import me.omegaweapondev.deathwarden.utils.StorageManager;
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

public class DeathWarden extends JavaPlugin {
  private DeathWarden plugin;
  private StorageManager storageManager;
  private Economy econ = null;

  private ParticleMenu particleMenu;
  private SoundMenu soundMenu;
  private DeathEffectsMenu deathEffectsMenu;

  @Override
  public void onEnable() {
    plugin = this;
    storageManager = new StorageManager(plugin);

    initialSetup();
    storageManager.setupConfigs();
    storageManager.configUpdater();
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
      particleMenu.deleteInventory();
      soundMenu.deleteInventory();
      deathEffectsMenu.deleteInventory();
    }

    storageManager.getDeathEffectsMap().clear();
    storageManager.getDeathLocation().clear();
    storageManager.getPenaltyMap().clear();

    super.onDisable();
  }

  public void onReload() {
    getStorageManager().getConfigFile().reloadConfig();
    getStorageManager().getMessagesFile().reloadConfig();
    getStorageManager().getDeathEffectMenus().reloadConfig();
  }

  private void initialSetup() {

    // Setup the instance for OU Library
    Utilities.setInstance(this);

    if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
      new Placeholders(this).register();
    }

    if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
      Utilities.logWarning(true,
        "Stylizer requires PlaceholderAPI to be installed if you are wanting to use the `%stylizer_namecolour%` placeholder",
        "It is also required if you are wanting to use placeholders in any of the chat formats.",
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
    Utilities.registerEvents(new PlayerListener(plugin), new PlayerDeathListener(plugin), new RewardsListener(plugin), new MenuListener()) ;
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

    if(!getStorageManager().getConfigFile().getConfig().getBoolean("Death_Effects_Restart")) {
      return;
    }

    for(Player player : Bukkit.getOnlinePlayers()) {
      if(Utilities.checkPermissions(player, true, "deathwarden.deatheffects", "deathwarden.admin")) {
        StorageManager playerData = new StorageManager(plugin, player, player.getUniqueId());
        storageManager.getDeathEffectsMap().put(player.getUniqueId(), playerData.getUserBoolean("Death_Effects.Enabled"));
      }
    }
  }

  public Economy getEconomy() {
    return econ;
  }

  public StorageManager getStorageManager() {
    return storageManager;
  }
}
