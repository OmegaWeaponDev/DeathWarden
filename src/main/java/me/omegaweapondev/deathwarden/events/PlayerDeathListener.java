package me.omegaweapondev.deathwarden.events;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.DeathCommands;
import me.omegaweapondev.deathwarden.utils.DeathMessages;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.omegaweapondev.deathwarden.utils.UserDataHandler;
import me.ou.library.Utilities;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PlayerDeathListener implements Listener {
  private final DeathWarden plugin;
  private final FileConfiguration configFile;
  private final FileConfiguration pvpLog;
  private final MessageHandler messageHandler;

  private Player player;
  private DeathCommands deathCommands;
  private UserDataHandler userData;
  private DeathMessages deathMessages;

  public PlayerDeathListener(final DeathWarden plugin){
    this.plugin = plugin;

    configFile = plugin.getSettingsHandler().getConfigFile().getConfig();
    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
    pvpLog = plugin.getSettingsHandler().getPvpLog().getConfig();
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerDeath(final PlayerDeathEvent playerDeathEvent) {

    if(configFile.getBoolean("Death_Messages")) {
      playerDeathEvent.setDeathMessage(null);
    }

    player = playerDeathEvent.getEntity();
    userData = new UserDataHandler(plugin, player, player.getUniqueId());
    final Location deathLocation = player.getLocation();

    if(configFile.getBoolean("Disabled_Worlds.Enabled")) {
      for(String world : configFile.getStringList("Disabled_Worlds.Worlds")) {
        if(player.getWorld().getName().equals(world)) {
          return;
        }
      }
    }

    deathEffects(player);

    if(Utilities.checkPermissions(player, true, "deathwarden.back", "deathwarden.admin")) {
      plugin.getSettingsHandler().getDeathLocation().put(player.getUniqueId(), deathLocation);

      userData.getPlayerData().set("Last_Known_Death_Location.World", player.getWorld().getName());
      userData.getPlayerData().set("Last_Known_Death_Location.X", player.getLocation().getX());
      userData.getPlayerData().set("Last_Known_Death_Location.Y", player.getLocation().getY());
      userData.getPlayerData().set("Last_Known_Death_Location.Z", player.getLocation().getZ());
      userData.getPlayerData().set("Last_Known_Death_Location.Yaw", player.getLocation().getYaw());
      userData.getPlayerData().set("Last_Known_Death_Location.Pitch", player.getLocation().getPitch());

      userData.savePlayerData();
    }

    if(Utilities.checkPermissions(player, false, "deathwarden.keepinv", "deathwarden.admin")) {
      keepInventory(playerDeathEvent, player);
    }

    if(Utilities.checkPermissions(player, false, "deathwarden.keepxp", "deathwarden.admin")) {
      keepExperience(playerDeathEvent, player);
    }

    if(player.getKiller() != null) {
      deathCommands = new DeathCommands(plugin, player, player.getKiller());
      deathCommands.deathCommands();
      deathCommands.killCommands();

      deathMessages = new DeathMessages(plugin, player, player.getKiller(), null);
      deathMessages.deathByPlayer();

      withdrawKillMoney(player.getKiller());
      logPvpDeath(player, player.getKiller());
      userData = new UserDataHandler(plugin, player.getKiller(), player.getKiller().getUniqueId());
      int pvpKills = userData.getPlayerData().getInt("Pvp_Kills");

      userData.getPlayerData().set("Pvp_Kills", pvpKills + 1);
      userData.savePlayerData();
    }

    if(player.getKiller() == null) {
      deathCommands = new DeathCommands(plugin, player, null);
      deathCommands.deathByNonPlayer();

      deathMessages = new DeathMessages(plugin, player, null,  player.getLastDamageCause());
      deathMessages.deathByCreatures();
    }

    userData = new UserDataHandler(plugin, player, player.getUniqueId());
    int deathCount = userData.getPlayerData().getInt("Death_Count");

    userData.getPlayerData().set("Death_Count", deathCount + 1);
    userData.savePlayerData();

  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent playerRespawnEvent) {
    Player player = playerRespawnEvent.getPlayer();

    if(configFile.getBoolean("Disabled_Worlds.Enabled")) {
      for(String world : configFile.getStringList("Disabled_Worlds.Worlds")) {
        if(player.getWorld().getName().equals(world)) {
          return;
        }
      }
    }

    new BukkitRunnable() {
      @Override
      public void run() {
        if(!configFile.getBoolean("Death_Penalty")) {
          return;
        }

        if(!configFile.getBoolean("Death_Tax.Percentage.Enabled") && !configFile.getBoolean("Death_Tax.Exact_Amount.Enabled")) {
          return;
        }

        if(Utilities.checkPermissions(player, false, "deathwarden.penalty.bypass", "deathwarden.admin")) {
          return;
        }

        withdrawDeathMoney(player);
      }
    }.runTaskLater(plugin, 1L);
  }

  private void deathEffects(final Player player) {
    userData = new UserDataHandler(plugin, player, player.getUniqueId());
    if(!userData.getPlayerData().getBoolean("Death_Effects.Enabled")) {
      return;
    }

    if(!plugin.getSettingsHandler().getDeathEffectsMap().containsKey(player.getUniqueId())) {
      plugin.getSettingsHandler().getDeathEffectsMap().put(player.getUniqueId(), userData.getPlayerData().getBoolean("Death_Effects.Enabled"));
    }

    if(!plugin.getSettingsHandler().getDeathEffectsMap().get(player.getUniqueId())) {
      return;
    }

    if(!userData.getPlayerData().getString("Death_Effects.Death_Particle").equalsIgnoreCase("none")) {
      player.spawnParticle(Particle.valueOf(userData.getPlayerData().getString("Death_Effects.Death_Particle")), player.getLocation(), 1);
    }

    if(!userData.getPlayerData().getString("Death_Effects.Death_Sound").equalsIgnoreCase("none")) {
      player.playSound(player.getLocation(), Sound.valueOf(userData.getPlayerData().getString("Death_Effects.Death_Sound")), 1, 1);
    }
  }

  private void keepExperience ( final PlayerDeathEvent playerDeathEvent, final Player player){
    playerDeathEvent.setKeepLevel(true);
    playerDeathEvent.setDroppedExp(0);
    Utilities.message(player, messageHandler.string("Experience_Saved", "#00D4FFAll your experience points have been saved!"));
  }

  private void keepInventory ( final PlayerDeathEvent playerDeathEvent, final Player player){
    playerDeathEvent.setKeepInventory(true);
    playerDeathEvent.getDrops().clear();
    Utilities.message(player, messageHandler.string("Inventory_Saved", "#00D4FFAll your inventory items have been saved!"));
  }

  private void logPvpDeath(final Player player, final Player killer) {
    String playerTime;
    String world = player.getLocation().getWorld().getName();

    if(killer == null) {
      return;
    }

    if(!configFile.getBoolean("Log_PvP_Deaths")) {
      return;
    }

    switch(configFile.getString("Time_Format", "dd-MM-yyyy HH:mm:ss")) {
      case "US":
        playerTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss"));
        break;
      case "CHN":
        playerTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        break;
      default:
        playerTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        break;
    }

    // Player Stats
    pvpLog.createSection("Pvp_Kills." + playerTime);
    pvpLog.set("Pvp_Kills." + playerTime + ".Player Killed", player.getName());
    pvpLog.set("Pvp_Kills." + playerTime + ".Killed By", killer.getName());
    pvpLog.set("Pvp_Kills." + playerTime + ".Killer Is Op", killer.isOp());
    pvpLog.set("Pvp_Kills." + playerTime + ".Killer Is Flying", killer.isFlying());
    pvpLog.set("Pvp_Kills." + playerTime + ".Killers Gamemode", killer.getGameMode().name());

    // Weapon Stats
    pvpLog.createSection("Pvp_Kills." + playerTime + ".Weapon");

    if(killer.getInventory().getItemInMainHand().getType().isAir()) {
      pvpLog.set("Pvp_Kills." + playerTime + ".Weapon" + ".Item", "Fists");
      pvpLog.set("Pvp_Kills." + playerTime + ".Weapon" + ".Name", killer.getName() + "'s Fists");
      pvpLog.set("Pvp_Kills." + playerTime + ".Weapon" + ".Enchants", "None");
    } else {
      pvpLog.set("Pvp_Kills." + playerTime + ".Weapon" + ".Item", killer.getInventory().getItemInMainHand().getType().name());
      pvpLog.set("Pvp_Kills." + playerTime + ".Weapon" + ".Name", (killer.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()) ? killer.getInventory().getItemInMainHand().getItemMeta().getDisplayName() : killer.getInventory().getItemInMainHand().getType().name());
      pvpLog.set("Pvp_Kills." + playerTime + ".Weapon" + ".Enchants", killer.getInventory().getItemInMainHand().getItemMeta().hasEnchants());
    }

    // Death Location
    pvpLog.createSection("Pvp_Kills." + playerTime + ".Location");
    pvpLog.set("Pvp_Kills." + playerTime + ".Location" + ".World", world);
    pvpLog.set("Pvp_Kills." + playerTime + ".Location" + ".X", player.getLocation().getBlockX());
    pvpLog.set("Pvp_Kills." + playerTime + ".Location" + ".Y", player.getLocation().getBlockY());
    pvpLog.set("Pvp_Kills." + playerTime + ".Location" + ".Z", player.getLocation().getBlockZ());

    // Save the file
    plugin.getSettingsHandler().getPvpLog().saveConfig();
  }

  private void withdrawDeathMoney(final Player player) {
    final double percentageAmount = configFile.getDouble("Death_Tax.Percentage.Amount");
    final double configAmount = configFile.getDouble("Death_Tax.Exact_Amount.Amount");
    final double playerBalance = plugin.getEconomy().getBalance(player);

    if(!configFile.getBoolean("Death_Tax.Percentage.Enabled")) {

      if(plugin.getEconomy().getBalance(player) < configAmount) {
        Utilities.message(player, messageHandler.string("Death_Tax_Penalty", "#ff4a4aYou have been penalised for not having the required money to pay the death taxes"));

        for(String potionEffect : configFile.getConfigurationSection("Death_Penalty_Effect.Potion_Effect").getKeys(false)) {
          Utilities.addPotionEffect(player, PotionEffectType.getByName(potionEffect), configFile.getInt("Death_Penalty_Effect.Potion_Effect." + potionEffect + ".Timer"), configFile.getInt("Death_Penalty_Effect.Potion_Effect." + potionEffect + ".Amplifier"), true, true, true);
        }
        plugin.getSettingsHandler().getPenaltyMap().put(player.getUniqueId(), true);
        return;
      }

      plugin.getEconomy().withdrawPlayer(player, configAmount);
      Utilities.message(player, messageHandler.string("Death_Tax", "#00D4FFThe amount of #ff4a4a$%money_taken% #00D4FFhas been taken from your account to pay for the death costs").replace("%money_taken%", String.valueOf(configAmount)));
      return;
    }

    if(plugin.getEconomy().getBalance(player) < calculatePercentage(percentageAmount, playerBalance) || plugin.getEconomy().getBalance(player) == 0.0) {
      Utilities.message(player, messageHandler.string("Death_Tax_Penalty", "#00D4FFYou have been penalised for not having the required money to pay the death taxes"));

      for(String potionEffect : configFile.getConfigurationSection("Death_Penalty_Effect.Potion_Effect").getKeys(false)) {
        Utilities.addPotionEffect(player, PotionEffectType.getByName(potionEffect), configFile.getInt("Death_Penalty_Effect.Potion_Effect." + potionEffect + ".Timer"), configFile.getInt("Death_Penalty_Effect.Potion_Effect." + potionEffect + ".Amplifier"), true, true, true);
      }
      plugin.getSettingsHandler().getPenaltyMap().put(player.getUniqueId(), true);
      return;
    }

    DecimalFormat df = new DecimalFormat("###.##");

    plugin.getEconomy().withdrawPlayer(player, calculatePercentage(percentageAmount, playerBalance));
    Utilities.message(player, messageHandler.string("Death_Tax", "#00D4FFThe amount of #ff4a4a$%money_taken% #00D4FFhas been taken from your account to pay for the death costs").replace("%money_taken%", df.format(calculatePercentage(percentageAmount, playerBalance))));
  }

  private void withdrawKillMoney(final Player killer) {
    final double percentageAmount = configFile.getDouble("Kill_Tax.Percentage.Amount");
    final double configAmount = configFile.getDouble("Kill_Tax.Exact_Amount.Amount");
    final double killerBalance = plugin.getEconomy().getBalance(killer);

    if(!configFile.getBoolean("Kill_Tax.Percentage.Enabled")) {

      if(plugin.getEconomy().getBalance(killer) < configAmount) {
        Utilities.message(killer, messageHandler.string("Kill_Tax_Penalty", "#ff4a4aYou have been penalised for not having the required money to pay the kill taxes"));

        for(String potionEffect : configFile.getConfigurationSection("Kill_Penalty_Effect.Potion_Effect").getKeys(false)) {
          Utilities.addPotionEffect(killer, PotionEffectType.getByName(potionEffect), configFile.getInt("Kill_Penalty_Effect.Potion_Effect." + potionEffect + ".Timer"), configFile.getInt("Kill_Penalty_Effect.Potion_Effect." + potionEffect + ".Amplifier"), true, true, true);
        }
        plugin.getSettingsHandler().getPenaltyMap().put(killer.getUniqueId(), true);
        return;
      }

      plugin.getEconomy().withdrawPlayer(killer, configAmount);
      Utilities.message(killer, messageHandler.string("Kill_Tax", "#00D4FFThe amount of #ff4a4a$%money_taken% #00D4FFhas been taken from your account to pay for the kill tax").replace("%money_taken%", String.valueOf(configAmount)));
      return;
    }

    if(plugin.getEconomy().getBalance(killer) < calculatePercentage(percentageAmount, killerBalance) || plugin.getEconomy().getBalance(killer) == 0.0) {
      Utilities.message(killer, messageHandler.string("Kill_Tax_Penalty", "#00D4FFYou have been penalised for not having the required money to pay the kill taxes"));

      for(String potionEffect : configFile.getConfigurationSection("Kill_Penalty_Effect.Potion_Effect").getKeys(false)) {
        Utilities.addPotionEffect(killer, PotionEffectType.getByName(potionEffect), configFile.getInt("Kill_Penalty_Effect.Potion_Effect." + potionEffect + ".Timer"), configFile.getInt("Kill_Penalty_Effect.Potion_Effect." + potionEffect + ".Amplifier"), true, true, true);
      }
      plugin.getSettingsHandler().getPenaltyMap().put(killer.getUniqueId(), true);
      return;
    }

    DecimalFormat df = new DecimalFormat("###.##");

    plugin.getEconomy().withdrawPlayer(killer, calculatePercentage(percentageAmount, killerBalance));
    Utilities.message(killer, messageHandler.string("Death_Tax", "#00D4FFThe amount of #ff4a4a$%money_taken% #00D4FFhas been taken from your account to pay for the kill tax").replace("%money_taken%", df.format(calculatePercentage(percentageAmount, killerBalance))));
  }

  private double calculatePercentage(final double percentageAmount, final double playerBalance) {
    return playerBalance * (percentageAmount / 100);
  }

}
