package me.omegaweapondev.deathwarden.utils;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.ou.library.Utilities;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import javax.annotation.Nullable;

public class DeathMessages {
  private final DeathWarden plugin;
  private final MessageHandler messageHandler;
  private final FileConfiguration configFile;
  private final Player player;
  private final Player killer;
  private final EntityDamageEvent damageByEntityEvent;

  public DeathMessages(final DeathWarden plugin, final Player player, @Nullable final Player killer, @Nullable final EntityDamageEvent damageByEntityEvent) {
    this.plugin = plugin;
    this.player = player;
    this.damageByEntityEvent = damageByEntityEvent;
    this.killer = killer;

    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
    configFile = plugin.getSettingsHandler().getConfigFile().getConfig();
  }

  public void deathByCreatures() {
    if(!player.isDead()) {
      return;
    }

    if(!(player.getLastDamageCause() instanceof EntityDamageByEntityEvent)) {
      deathByEnvironment();
      return;
    }

    if((((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager() instanceof Player)) {
      return;
    }

    if(((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager() instanceof EvokerFangs) {
      EvokerFangs fangs = (EvokerFangs) ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();

      if(fangs.getOwner() instanceof Evoker) {
        Utilities.broadcast(
          messageHandler.string(
            "Death_By.Creatures.Evoker",
            "&c%player% needs to leave the evil evokers alone from now on"
          ).replace("%player%", player.getDisplayName())
        );
        return;
      }
    }

    if(((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager() instanceof DragonFireball) {
      DragonFireball dragonFireball = (DragonFireball) ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();

      if(dragonFireball.getShooter() instanceof EnderDragon) {
        Utilities.broadcast(
          messageHandler.string(
            "Death_By.Creatures.Enderdragon",
            "&b%player% died trying to steal the dragons egg..."
          ).replace("%player%", player.getDisplayName())
        );
        return;
      }
    }

    if(((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager() instanceof Arrow) {
      Arrow arrow = (Arrow) ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();

      if(arrow.getShooter() instanceof Skeleton) {
        Utilities.broadcast(
          messageHandler.string(
            "Death_By.Creatures.Skeleton",
            "&b%player% just took an arrow to the knee by a skeleton"
          ).replace("%player%", player.getDisplayName())
        );
        return;
      }

      if(arrow.getShooter() instanceof Pillager) {
        Utilities.broadcast(
          messageHandler.string(
            "Death_By.Creatures.Pillager",
            "&bYou never know what you're going to get with pillagers... right %player%?"
          ).replace("%player%", player.getDisplayName())
        );
        return;
      }
    }

    if(((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager() instanceof Fireball) {
      Fireball fireball = (Fireball) ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();

      if(!(fireball.getShooter() instanceof Ghast)) {
        return;
      }

      Utilities.broadcast(
        messageHandler.string(
          "Death_By.Creatures.Ghast",
          "&b%player% just tried to play dodgeball with a ghast and lost"
        ).replace("%player%", player.getDisplayName())
      );
      return;
    }

    for(String creature : messageHandler.configSection("Death_By.Creatures").getKeys(false)) {
      if(((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager().getType() == EntityType.valueOf(creature.toUpperCase())) {
        Utilities.broadcast(messageHandler.string("Death_By.Creatures." + creature, "#ff4a4aYou have been killed by a " + creature).replace("%player%", player.getDisplayName()));
        return;
      }
    }
  }

  public void deathByEnvironment() {
    if(!player.isDead()) {
      return;
    }

    for(String deathCause : messageHandler.configSection("Death_By.Environmental").getKeys(false)) {
      if(player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.valueOf(deathCause.toUpperCase())) {
        Utilities.broadcast(messageHandler.string("Death_By.Environmental." + deathCause, "#ff4a4aYou were killed by " + deathCause).replace("%player%", player.getDisplayName()));
        return;
      }
    }
  }

  public void deathByPlayer() {
    if(killer == null) {
      return;
    }

    if(configFile.getBoolean("Death_By_Player_Messages")) {
      String weapon;

      if(killer.getInventory().getItemInMainHand().getType().isAir()) {
        weapon = "their own fists";
      } else {
        weapon = (killer.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()) ? killer.getInventory().getItemInMainHand().getItemMeta().getDisplayName() : killer.getInventory().getItemInMainHand().getType().name();
      }

      Utilities.broadcast(messageHandler.string("PvP_Death_Messages.Killed_By_Player", "#ff4a4a%killer% &bhas just killed #ff4a4a%player% &busing #ff4a4a%weapon% &band they only had #ff4a4a%hearts_remaining% &bhearts left!")
        .replace("%player%", player.getName())
        .replace("%killer%", killer.getName())
        .replace("%weapon%", weapon)
        .replace("%hearts_remaining%", String.valueOf(killer.getHealth() / 2))
      );
    }
  }
}
