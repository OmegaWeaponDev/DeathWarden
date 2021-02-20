package me.omegaweapondev.deathwarden.events;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.UserDataHandler;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {
  private final DeathWarden plugin;

  private UserDataHandler userData;

  public EntityDeathListener(final DeathWarden plugin) {
    this.plugin = plugin;
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onEntityDeath(EntityDeathEvent entityDeathEvent) {
    if(entityDeathEvent.getEntity() instanceof Player) {
      return;
    }

    EntityType entityType = entityDeathEvent.getEntityType();

    if(entityDeathEvent.getEntity().getKiller() == null) {
      return;
    }

    userData = plugin.getUserData(entityDeathEvent.getEntity().getKiller(), entityDeathEvent.getEntity().getKiller().getUniqueId());

    for(String entity : userData.getPlayerData().getConfigurationSection("Creatures_Killed").getKeys(false)) {

      if(entityType == EntityType.valueOf(entity.toUpperCase())) {
        int timesKilled = userData.getPlayerData().getInt("Creatures_Killed." + entity);
        int totalTimesKilled = plugin.getSettingsHandler().getTotalDeathsLog().getConfig().getInt("Creatures_Killed." + entity);

        userData.getPlayerData().getInt("Creatures_Killed." + entity, timesKilled + 1);
        plugin.getSettingsHandler().getTotalDeathsLog().getConfig().set("Creatures_Killed." + entity, totalTimesKilled + 1);
      }
    }
    userData.savePlayerData();
    plugin.getSettingsHandler().getTotalDeathsLog().saveConfig();
  }
}
