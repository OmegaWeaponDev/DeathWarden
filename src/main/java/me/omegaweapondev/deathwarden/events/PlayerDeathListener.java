package me.omegaweapondev.deathwarden.events;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.DeathCommands;
import me.omegaweapondev.deathwarden.utils.DeathMessages;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.omegaweapondev.deathwarden.utils.StorageManager;
import me.ou.library.Utilities;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDeathListener implements Listener {
  private final DeathWarden plugin;
  private final FileConfiguration configFile;
  private final FileConfiguration pvpLog;
  private final MessageHandler messageHandler;
  private static final Map<UUID, Location> deathLocationMap = new HashMap<>();

  private Player player;
  private DeathCommands deathCommands;
  private StorageManager storageManager;
  private DeathMessages deathMessages;

  public PlayerDeathListener(final DeathWarden plugin){
    this.plugin = plugin;
    configFile = plugin.getStorageManager().getConfigFile().getConfig();
    messageHandler = new MessageHandler(plugin, plugin.getStorageManager().getMessagesFile().getConfig());
    pvpLog = plugin.getStorageManager().getPvpLog().getConfig();
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerDeath(final PlayerDeathEvent playerDeathEvent) {
    playerDeathEvent.setDeathMessage(null);
    player = playerDeathEvent.getEntity();
    storageManager = new StorageManager(plugin, player, player.getUniqueId());
    final Location deathLocation = player.getLocation();

    deathEffects(player);

    if(Utilities.checkPermissions(player, true, "deathwarden.back", "deathwarden.admin")) {
      deathLocationMap.put(player.getUniqueId(), deathLocation);

      storageManager.setUserString("Last_Known_Death_Location.World", player.getWorld().getName());
      storageManager.setUserDouble("Last_Known_Death_Location.X", player.getLocation().getX());
      storageManager.setUserDouble("Last_Known_Death_Location.Y", player.getLocation().getY());
      storageManager.setUserDouble("Last_Known_Death_Location.Z", player.getLocation().getZ());
      storageManager.setUserDouble("Last_Known_Death_Location.Yaw", player.getLocation().getYaw());
      storageManager.setUserDouble("Last_Known_Death_Location.Pitch", player.getLocation().getPitch());

      storageManager.savePlayerData();
    }

    if(Utilities.checkPermissions(player, true, "deathwarden.keepinv", "deathwarden.admin")) {
      keepInventory(playerDeathEvent, player);
    }

    if(Utilities.checkPermissions(player, true, "deathwarden.keepxp", "deathwarden.admin")) {
      keepExperience(playerDeathEvent, player);
    }

    if(player.getKiller() != null) {
      deathCommands = new DeathCommands(plugin, player, player.getKiller());
      deathCommands.deathCommands();
      deathCommands.killCommands();

      deathMessages = new DeathMessages(plugin, player, player.getKiller(), null);
      deathMessages.deathByPlayer();

      logPvpDeath(player, player.getKiller());
    }

    if(player.getKiller() == null) {
      deathCommands = new DeathCommands(plugin, player, null);
      deathCommands.deathByNonPlayer();

      if(!(player.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
        return;
      }

      deathMessages = new DeathMessages(plugin, player, null, (EntityDamageByEntityEvent) player.getLastDamageCause());
      deathMessages.deathByCreatures();
    }
  }

  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent playerRespawnEvent) {
    Player player = playerRespawnEvent.getPlayer();

    new BukkitRunnable() {
      @Override
      public void run() {
        if(!configFile.getBoolean("Death_Penalty")) {
          return;
        }

        if(!configFile.getBoolean("Death_Money.Percentage.Enabled") && !configFile.getBoolean("Death_Money.Exact_Amount.Enabled")) {
          return;
        }

        if(Utilities.checkPermissions(player, true, "omegadeath.penalty.bypass", "omegadeath.admin")) {
          return;
        }

        withdrawDeathMoney(player);
      }
    }.runTaskLater(plugin, 1L);
  }

  private void deathEffects(final Player player) {
    storageManager = new StorageManager(plugin, player, player.getUniqueId());
    if(!storageManager.getUserBoolean("Death_Effects.Enabled")) {
      return;
    }

    if(!PlayerListener.getDeathEffectsMap().get(player.getUniqueId())) {
      return;
    }

    String particles = storageManager.getUserString("Death_Effects.Death_Particle");
    String sound = storageManager.getUserString("Death_Effects.Death_Sound");

    player.spawnParticle(Particle.valueOf(particles), player.getLocation(), 1);
    player.playSound(player.getLocation(), Sound.valueOf(sound), 1, 1);
  }

  private void keepExperience ( final PlayerDeathEvent playerDeathEvent, final Player player){
    playerDeathEvent.setKeepLevel(true);
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
    pvpLog.createSection(playerTime);
    pvpLog.set(playerTime + ".Player Killed", player.getName());
    pvpLog.set(playerTime + ".Killed By", killer.getName());
    pvpLog.set(playerTime + ".Killer Is Op", killer.isOp());
    pvpLog.set(playerTime + ".Killer Is Flying", killer.isFlying());
    pvpLog.set(playerTime + ".Killers Gamemode", killer.getGameMode().name());

    // Weapon Stats
    pvpLog.createSection(playerTime + ".Weapon");
    pvpLog.set(playerTime + ".Weapon" + ".Item", killer.getInventory().getItemInMainHand().getType().name());
    pvpLog.set(playerTime + ".Weapon" + ".Name", (killer.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()) ? killer.getInventory().getItemInMainHand().getItemMeta().getDisplayName() : killer.getInventory().getItemInMainHand().getType().name());
    pvpLog.set(playerTime + ".Weapon" + ".Enchants", killer.getInventory().getItemInMainHand().getItemMeta().hasEnchants());

    // Death Location
    pvpLog.createSection(playerTime + ".Location");
    pvpLog.set(playerTime + ".Location" + ".World", world);
    pvpLog.set(playerTime + ".Location" + ".X", player.getLocation().getBlockX());
    pvpLog.set(playerTime + ".Location" + ".Y", player.getLocation().getBlockY());
    pvpLog.set(playerTime + ".Location" + ".Z", player.getLocation().getBlockZ());

    // Save the file
    plugin.getStorageManager().getPvpLog().saveConfig();
  }

  private void withdrawDeathMoney(final Player player) {
    final double percentageAmount = configFile.getDouble("Death_Money.Percentage.Amount");
    final double configAmount = configFile.getDouble("Death_Money.Exact_Amount.Amount");
    final double playerBalance = plugin.getEconomy().getBalance(player);

    if(!configFile.getBoolean("Death_Money.Percentage.Enabled")) {

      if(!(plugin.getEconomy().getBalance(player) >= configAmount)) {
        Utilities.message(player, messageHandler.string("Death_Penalty", "&cYou have been penalised for not having the required money to pay the death taxes"));

        for(String potionEffect : configFile.getConfigurationSection("Death_Penalty_Effect.Potion_Effect").getKeys(false)) {
          Utilities.addPotionEffect(player, PotionEffectType.getByName(potionEffect), configFile.getInt("Death_Penalty_Effect." + potionEffect + ".Timer") * 20, configFile.getInt("Death_Penalty_Effect." + potionEffect + ".Amplifier"), true, true, true);
        }
        storageManager.getPenaltyMap().put(player.getUniqueId(), true);
        return;
      }

      plugin.getEconomy().withdrawPlayer(player, configAmount);
      Utilities.message(player, messageHandler.string("Death_Money", "&cThe amount of $%money_taken% has been taken from your account to pay for the death costs").replace("%money_taken%", String.valueOf(configAmount)));
      return;
    }

    if(!(plugin.getEconomy().getBalance(player) >= calculatePercentage(percentageAmount, playerBalance))) {
      Utilities.message(player, messageHandler.string("Death_Penalty", "&cYou have been penalised for not having the required money to pay the death taxes"));

      for(String potionEffect : configFile.getConfigurationSection("Death_Penalty_Effect.Potion_Effect").getKeys(false)) {
        Utilities.addPotionEffect(player, PotionEffectType.getByName(potionEffect), configFile.getInt("Death_Penalty_Effect." + potionEffect + ".Timer") * 20, configFile.getInt("Death_Penalty_Effect." + potionEffect + ".Amplifier"), true, true, true);
      }
      storageManager.getPenaltyMap().put(player.getUniqueId(), true);
      return;
    }

    DecimalFormat df = new DecimalFormat("###.##");

    plugin.getEconomy().withdrawPlayer(player, calculatePercentage(percentageAmount, playerBalance));
    Utilities.message(player, messageHandler.string("Death_Money", "&cThe amount of $%money_taken% has been taken from your account to pay for the death costs").replace("%money_taken%", df.format(calculatePercentage(percentageAmount, playerBalance))));
  }

  private double calculatePercentage(final double percentageAmount, final double playerBalance) {
    return playerBalance * (percentageAmount / 100);
  }

}
