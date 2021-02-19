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
  private StorageManager storageManager;

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

    storageManager = new StorageManager(plugin, entityDeathEvent.getEntity().getKiller(), entityDeathEvent.getEntity().getKiller().getUniqueId());

    for(String entity : storageManager.getUserSection("Creatures_Killed")) {

      if(entityType == EntityType.valueOf(entity.toUpperCase())) {
        int timesKilled = storageManager.getUserInt("Creatures_Killed." + entity);
        int totalTimesKilled = plugin.getStorageManager().getTotalDeathsLog().getConfig().getInt("Creatures_Killed." + entity);

        storageManager.setUserInt("Creatures_Killed." + entity, timesKilled + 1);
        plugin.getStorageManager().getTotalDeathsLog().getConfig().set("Creatures_Killed." + entity, totalTimesKilled + 1);
      }
    }
    storageManager.savePlayerData();
    plugin.getStorageManager().getTotalDeathsLog().saveConfig();
  }
}
