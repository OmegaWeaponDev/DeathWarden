package me.omegaweapondev.deathwarden.events;

import me.omegaweapondev.deathwarden.DeathWarden;
import org.bukkit.event.Listener;

public class RewardsListener implements Listener {
  private final DeathWarden plugin;

  public RewardsListener(final DeathWarden plugin) {
    this.plugin = plugin;
  }
}
