package me.omegaweapondev.deathwarden.menus;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.ItemCreator;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.ou.library.Utilities;
import me.ou.library.menus.MenuCreator;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class DeathEffectsMenu extends MenuCreator {
  private final DeathWarden plugin;
  private final MessageHandler messageHandler;
  private final FileConfiguration deathEffectsMenu;

  private SoundMenu soundMenu;
  private ParticleMenu particleMenu;
  private ItemCreator itemCreator;

  public DeathEffectsMenu(final DeathWarden plugin, int inventoryRows, String inventoryName, String defaultInventoryName) {
    super(inventoryRows, inventoryName, defaultInventoryName);
    this.plugin = plugin;
    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
    deathEffectsMenu =  plugin.getSettingsHandler().getDeathEffectMenus().getConfig();

    setItem(11, createItem("Death_Effects_Menu.Death_Sounds_Button"), player -> {
      if(!Utilities.checkPermissions(player, true, "deathwarden.deatheffects.sound", "deathwarden.admin")) {
        Utilities.message(player, messageHandler.string("No_Permission", "#ff4a4aSorry, you do not have permission to use that."));
        return;
      }

      player.closeInventory();
      soundMenu = new SoundMenu(plugin, 4, Utilities.colourise(deathEffectsMenu.getString("Death_Sounds_Menu.Menu_Title")), "#00D4FFDeath Sounds Menu");
      soundMenu.openInventory(player);
    });

    setItem(13, createItem("Death_Effects_Menu.Death_Particles_Button"), player -> {
      if(!Utilities.checkPermissions(player, true, "deathwarden.deatheffects.particle", "deathwarden.admin")) {
        Utilities.message(player, messageHandler.string("No_Permission", "#ff4a4aSorry, you do not have permission to use that."));
        return;
      }

      player.closeInventory();
      particleMenu = new ParticleMenu(plugin, 4, Utilities.colourise(deathEffectsMenu.getString("Death_Particles_Menu.Menu_Title")), "#00D4FFDeath Particles Menu");
      particleMenu.openInventory(player);
    });

    setItem(15, createItemStack("BARRIER", Utilities.colourise("#570000Close"), Utilities.colourise(Arrays.asList("#ff4a4aClick here to close", "#ff4a4athe name colour gui"))), HumanEntity::closeInventory);
  }

  private ItemStack createItem(final String deathEffectItem) {

    if(Material.getMaterial(deathEffectsMenu.getString(deathEffectItem + ".Material").toUpperCase()) == null) {
      itemCreator = new ItemCreator(Material.BARRIER);
      itemCreator.setDisplayName("#ff4a4aInvalid Material");
      itemCreator.setLore("#ff4a4aThis item is invalid.", "#ff4a4aPlease pick another material to use", "#ff4a4athat is supported by your server version");

      return itemCreator.getItem();
    }

    itemCreator = new ItemCreator(Material.getMaterial(deathEffectsMenu.getString(deathEffectItem + ".Material").toUpperCase()));
    itemCreator.setDisplayName(deathEffectsMenu.getString(deathEffectItem + ".Title"));
    itemCreator.setLore(deathEffectsMenu.getStringList(deathEffectsMenu + ".Lore"));

    return itemCreator.getItem();
  }

  private ItemStack createItemStack(final String material, final String name, final List<String> lore) {
    if(Material.getMaterial(material.toUpperCase()) == null) {
      itemCreator = new ItemCreator(Material.BARRIER);
      itemCreator.setDisplayName("#570000Invalid Material");
      itemCreator.setLore("#ff4a4aThis item is invalid.", "#ff4a4aPlease pick another material to use", "#ff4a4athat is supported by your server version");

      return itemCreator.getItem();
    }

    itemCreator = new ItemCreator(Material.getMaterial(material.toUpperCase()));
    itemCreator.setDisplayName(name);

    itemCreator.setLore(lore);

    return itemCreator.getItem();
  }
}
