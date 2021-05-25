package me.omegaweapondev.deathwarden.events;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.ou.library.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Random;

public class RewardsListener implements Listener {
  private final DeathWarden plugin;
  private final FileConfiguration configFile;

  public RewardsListener(final DeathWarden plugin) {
    this.plugin = plugin;
    configFile = plugin.getSettingsHandler().getConfigFile().getConfig();
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onEntityDeath(EntityDeathEvent entityDeathEvent) {
    if(entityDeathEvent.getEntity() instanceof Player) {
      return;
    }

    EntityType entityType = entityDeathEvent.getEntity().getType();
    Player player = entityDeathEvent.getEntity().getKiller();

    if(player == null) {
      return;
    }

    if(configFile.getBoolean("Disabled_Worlds.Enabled")) {
      for(String world : configFile.getStringList("Disabled_Worlds.Worlds")) {
        if(player.getWorld().getName().equals(world)) {
          return;
        }
      }
    }

    rewardLimits(player, entityType);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
    Player player = playerJoinEvent.getPlayer();

    if(configFile.getBoolean("Disabled_Worlds.Enabled")) {
      for(String world : configFile.getStringList("Disabled_Worlds.Worlds")) {
        if(player.getWorld().getName().equals(world)) {
          return;
        }
      }
    }

    if(!plugin.getSettingsHandler().getRewardsFile().getConfig().getBoolean("Reward_Limits.Enabled")) {
      return;
    }

    if(!Utilities.checkPermissions(player, true, "deathwarden.rewards.limit")) {
      return;
    }

    int configTimer = plugin.getSettingsHandler().getRewardsFile().getConfig().getInt("Reward_Limits.Limit_Cooldown");

    Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
      if(!plugin.getSettingsHandler().getRewardLimitsMap().containsKey(player.getUniqueId())) {
        return;
      }

      if(plugin.getSettingsHandler().getRewardLimitsMap().get(player.getUniqueId()) == plugin.getSettingsHandler().getRewardsFile().getConfig().getInt("Reward_Limits.Limit_Amount")) {
        plugin.getSettingsHandler().getRewardLimitsMap().put(player.getUniqueId(), 0);
        Utilities.message(player, plugin.getSettingsHandler().getRewardsFile().getConfig().getString("Reward_Limits.Limit_Reset", "&bYour rewards limit has been reset."));
      }
    }, 20L * 5L, 20L * configTimer * 60);
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerQuit(PlayerQuitEvent playerQuitEvent) {
    Player player = playerQuitEvent.getPlayer();
    plugin.getSettingsHandler().getRewardLimitsMap().remove(player.getUniqueId());
  }

  private void giveReward(final Player player, final String entity) {
    if(configFile.getBoolean("Disabled_Worlds.Enabled")) {
      for(String world : configFile.getStringList("Disabled_Worlds.Worlds")) {
        if(player.getWorld().getName().equals(world)) {
          return;
        }
      }
    }

    if(!plugin.getSettingsHandler().getRewardsFile().getConfig().getBoolean("Kill_Rewards")) {
      return;
    }

    if(Utilities.checkPermissions(player, true, "omegadeath.rewards.bypass", "omegadeath.admin")) {
      return;
    }

    for(String command : plugin.getSettingsHandler().getRewardsFile().getConfig().getStringList("Rewards.Creatures." + entity + ".Rewards.Command_Rewards.Commands")) {
      executeRewardCommands(player, command, entity);
    }

    calculateMoneyReward(player, entity);

    if(plugin.getSettingsHandler().getConfigFile().getConfig().getBoolean("Rewards.Creatures." + entity + ".Rewards.Reward_Message.Enabled")) {
      Utilities.message(player, plugin.getSettingsHandler().getRewardsFile().getConfig().getString("Rewards.Creatures." + entity + ".Rewards.Reward_Message.Message", "&bEnjoy all these amazing rewards for that awesome kill."));
    }
  }

  private void executeRewardCommands(final Player player, final String rewardCommand, final String entity) {
    final ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();

    if(!plugin.getSettingsHandler().getRewardsFile().getConfig().getBoolean("Rewards.Creatures." + entity + ".Rewards.Command_Rewards.Enabled")) {
      return;
    }

    Bukkit.dispatchCommand(console, rewardCommand.replace("%player%", player.getName()));
  }

  private void calculateMoneyReward(final Player player, final String entity) {
    final double min = plugin.getSettingsHandler().getRewardsFile().getConfig().getDouble("Rewards.Creatures." + entity + ".Rewards.Money.Min");
    final double max = plugin.getSettingsHandler().getRewardsFile().getConfig().getDouble("Rewards.Creatures." + entity + ".Rewards.Money.Max");

    if(!plugin.getSettingsHandler().getRewardsFile().getConfig().getBoolean("Rewards.Creatures." + entity + ".Rewards.Money.Enabled")) {
      return;
    }

    Random r = new Random();
    double calculatedMoney = min + (max - min) * r.nextDouble();

    plugin.getEconomy().depositPlayer(player, calculatedMoney);
  }

  private void rewardLimits(final Player player, final EntityType entity) {
    if(configFile.getBoolean("Disabled_Worlds.Enabled")) {
      for(String world : configFile.getStringList("Disabled_Worlds.Worlds")) {
        if(player.getWorld().getName().equals(world)) {
          return;
        }
      }
    }

    final Integer limitAmount = plugin.getSettingsHandler().getRewardLimitsMap().get(player.getUniqueId());

    if(!plugin.getSettingsHandler().getRewardsFile().getConfig().getBoolean("Reward_Limits.Enabled")) {
      for(String configEntity : plugin.getSettingsHandler().getRewardsFile().getConfig().getConfigurationSection("Rewards.Creatures").getKeys(false)) {

        if(entity.equals(EntityType.valueOf(configEntity.toUpperCase()))) {
          giveReward(player, configEntity);
          return;
        }
      }
      return;
    }

    if(!Utilities.checkPermission(player, false, "omegadeath.rewards.limit")) {
      for(String configEntity : plugin.getSettingsHandler().getRewardsFile().getConfig().getConfigurationSection("Rewards.Creatures").getKeys(false)) {

        if(entity.equals(EntityType.valueOf(configEntity.toUpperCase()))) {
          giveReward(player, configEntity);
          return;
        }
      }
      return;
    }

    if(!plugin.getSettingsHandler().getRewardLimitsMap().containsKey(player.getUniqueId())) {
      return;
    }

    if(plugin.getSettingsHandler().getRewardLimitsMap().get(player.getUniqueId()) == plugin.getSettingsHandler().getRewardsFile().getConfig().getInt("Reward_Limits.Limit_Amount")) {
      return;
    }

    plugin.getSettingsHandler().getRewardLimitsMap().put(player.getUniqueId(), limitAmount + 1);

    for(String configEntity : plugin.getSettingsHandler().getRewardsFile().getConfig().getConfigurationSection("Rewards.Creatures").getKeys(false)) {

      if(entity.equals(EntityType.valueOf(configEntity.toUpperCase()))) {
        giveReward(player, configEntity);
        return;
      }
    }
  }
}
