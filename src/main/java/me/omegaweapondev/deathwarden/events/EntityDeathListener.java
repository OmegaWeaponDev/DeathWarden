package me.omegaweapondev.deathwarden.events;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.StorageManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityDeathListener implements Listener {
  private final DeathWarden plugin;
  private final StorageManager storageManager;

  public EntityDeathListener(final DeathWarden plugin) {
    this.plugin = plugin;
    storageManager = new StorageManager(plugin);
  }

  @EventHandler(priority = EventPriority.NORMAL)
  public void onEntityDeath(EntityDeathEvent entityDeathEvent) {
    if(entityDeathEvent.getEntity() instanceof Player) {
      return;
    }

    EntityType entityType = entityDeathEvent.getEntityType();

    for(String entity : storageManager.getUserSection("Creatures_Killed")) {
      if(entityType == EntityType.valueOf(entity.toUpperCase())) {
        int timesKilled = storageManager.getUserInt("Creatures_Killed." + entity);
        int totalTimesKilled = storageManager.getTotalDeathsLog().getConfig().getInt("Creatures_Killed." + entity);

        storageManager.setUserInt("Creatures_Killed." + entity, timesKilled + 1);
        storageManager.getTotalDeathsLog().getConfig().set("Creatures_Killed." + entity, totalTimesKilled + 1);
      }
    }
    storageManager.savePlayerData();
    storageManager.getTotalDeathsLog().saveConfig();
  }
}
