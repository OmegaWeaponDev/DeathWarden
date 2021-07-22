package me.omegaweapondev.deathwarden.utils;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.ou.library.Utilities;
import me.ou.library.chat.ChatComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

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
    if(!configFile.getBoolean("Death_Messages")) {
      return;
    }

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
        Utilities.broadcast(false,
          messageHandler.string(
            "Death_By.Creatures.Evoker",
            "&c%player% needs to leave the evil evokers alone from now on"
          ).replace("%player%", player.getDisplayName()).replace("%coords%", ("X: " + player.getLocation().getX() + ", Y: " + player.getLocation().getY() + ", Z: " + player.getLocation().getZ()))
        );
        return;
      }
    }

    if(((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager() instanceof DragonFireball) {
      DragonFireball dragonFireball = (DragonFireball) ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();

      if(dragonFireball.getShooter() instanceof EnderDragon) {
        Utilities.broadcast(false,
          messageHandler.string(
            "Death_By.Creatures.Enderdragon",
            "&b%player% died trying to steal the dragons egg..."
          ).replace("%player%", player.getDisplayName()).replace("%coords%", ("X: " + player.getLocation().getX() + ", Y: " + player.getLocation().getY() + ", Z: " + player.getLocation().getZ()))
        );
        return;
      }
    }

    if(((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager() instanceof Arrow) {
      Arrow arrow = (Arrow) ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();

      if(arrow.getShooter() instanceof Skeleton) {
        Utilities.broadcast(false,
          messageHandler.string(
            "Death_By.Creatures.Skeleton",
            "&b%player% just took an arrow to the knee by a skeleton"
          ).replace("%player%", player.getDisplayName()).replace("%coords%", ("X: " + player.getLocation().getX() + ", Y: " + player.getLocation().getY() + ", Z: " + player.getLocation().getZ()))
        );
        return;
      }

      if(arrow.getShooter() instanceof Pillager) {
        Utilities.broadcast(false,
          messageHandler.string(
            "Death_By.Creatures.Pillager",
            "&bYou never know what you're going to get with pillagers... right %player%?"
          ).replace("%player%", player.getDisplayName()).replace("%coords%", ("X: " + player.getLocation().getX() + ", Y: " + player.getLocation().getY() + ", Z: " + player.getLocation().getZ()))
        );
        return;
      }
    }

    if(((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager() instanceof Fireball) {
      Fireball fireball = (Fireball) ((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager();

      if(!(fireball.getShooter() instanceof Ghast)) {
        return;
      }

      Utilities.broadcast(false,
        messageHandler.string(
          "Death_By.Creatures.Ghast",
          "&b%player% just tried to play dodgeball with a ghast and lost"
        ).replace("%player%", player.getDisplayName()).replace("%coords%", ("X: " + player.getLocation().getX() + ", Y: " + player.getLocation().getY() + ", Z: " + player.getLocation().getZ()))
      );
      return;
    }

    for(String creature : messageHandler.configSection("Death_By.Creatures").getKeys(false)) {
      if(((EntityDamageByEntityEvent) player.getLastDamageCause()).getDamager().getType() == EntityType.valueOf(creature.toUpperCase())) {
        Utilities.broadcast(false, messageHandler.string("Death_By.Creatures." + creature, "#ff4a4aYou have been killed by a " + creature).replace("%player%", player.getDisplayName()).replace("%coords%", ("X: " + player.getLocation().getX() + ", Y: " + player.getLocation().getY() + ", Z: " + player.getLocation().getZ())));
        return;
      }
    }
  }

  public void deathByEnvironment() {
    if(!configFile.getBoolean("Death_Messages")) {
      return;
    }

    if(!player.isDead()) {
      return;
    }

    for(String deathCause : messageHandler.configSection("Death_By.Environmental").getKeys(false)) {
      if(player.getLastDamageCause().getCause() == EntityDamageEvent.DamageCause.valueOf(deathCause.toUpperCase())) {
        Utilities.broadcast(false, messageHandler.string("Death_By.Environmental." + deathCause, "#ff4a4aYou were killed by " + deathCause).replace("%player%", player.getDisplayName()).replace("%coords%", ("X: " + player.getLocation().getX() + ", Y: " + player.getLocation().getY() + ", Z: " + player.getLocation().getZ())));
        return;
      }
    }
  }

  public void deathByPlayer() {
    if(!configFile.getBoolean("Death_Messages")) {
      return;
    }

    if(killer == null) {
      return;
    }

    if(!configFile.getBoolean("Death_By_Player_Messages")) {
      return;
    }

    if(killer.getInventory().getItemInMainHand().getType().isAir()) {
      String weaponText = Utilities.colourise(
        "#00D4FFWeapon Name: #FF4A4A" + killer.getDisplayName() + "'s own two fists" +
          "\n#00D4FFWeapon Type: #FF4A4A" + "Hands" +
          "\n#00D4FFHas Enchants: #FF4A4A" + killer.getDisplayName() + " wish they had enchants" +
          "\n#00D4FFDurability: #FF4A4A" + "bruised and bleeding" +
          "\n#00D4FFLore: " + Arrays.asList("#FF4A4AWho needs weapons", "\n#FF4A4Awhen you have fists", "\n#FF4A4Alike these!")
      );

      for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        new ChatComponent(
          Utilities.colourise(
            messageHandler.string("PvP_Death_Message", "#FF003E%killer% #00D4FFhas just killed #FF003E%player% #00D4FFusing #FF003E%weapon% #00D4FFand they only had #FF003E%hearts_remaining% #00D4FFhearts left!")
            .replace("%killer%", killer.getDisplayName())
            .replace("%player%", player.getDisplayName())
            .replace("%weapon%", killer.getDisplayName() + "#FF4A4A's own two fists")
            .replace("%hearts_remaining%", String.valueOf(killer.getHealth() / 2))
            .replace("%coords%", ("X: " + player.getLocation().getX() + ", Y: " + player.getLocation().getY() + ", Z: " + player.getLocation().getZ()))
          )
        ).onHover(HoverEvent.Action.SHOW_TEXT, weaponText).send(onlinePlayer);
      }
      return;
    }

    Damageable damageable = (Damageable) killer.getInventory().getItemInMainHand().getItemMeta();

    String weaponName;

    if(killer.getInventory().getItemInMainHand().hasItemMeta() && killer.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()) {
      weaponName = killer.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
    } else {
      weaponName = killer.getInventory().getItemInMainHand().getType().toString();
    }

    String weaponText = "&9Weapon Name: &c" + weaponName +
      "\n&9Weapon Type: &c" + killer.getInventory().getItemInMainHand().getType().name() +
      "\n&9Has Enchants: &c" + killer.getInventory().getItemInMainHand().getItemMeta().hasEnchants() +
      "\n&9Durability: &c" + (killer.getInventory().getItemInMainHand().getType().getMaxDurability() - damageable.getDamage()) + " / " + killer.getInventory().getItemInMainHand().getType().getMaxDurability() +
      "\n&9Lore: &c" + (killer.getInventory().getItemInMainHand().getItemMeta().getLore() == null ? "none" : killer.getInventory().getItemInMainHand().getItemMeta().getLore());

    for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {


      new ChatComponent(
        Utilities.colourise(
          messageHandler.string("PvP_Death_Messages", "#FF003E%killer% #00D4FFhas just killed #FF003E%player% #00D4FFusing #FF003E%weapon% #00D4FFand they only had #FF003E%hearts_remaining% #00D4FFhearts left!")
            .replace("%killer%", killer.getDisplayName())
            .replace("%player%", player.getDisplayName())
            .replace("%weapon%", weaponName)
            .replace("%hearts_remaining%", String.valueOf(killer.getHealth() / 2))
            .replace("%coords%", ("X: " + player.getLocation().getX() + ", Y: " + player.getLocation().getY() + ", Z: " + player.getLocation().getZ()))
        )
      ).onHover(weaponText).send(onlinePlayer);
    }
  }
}
