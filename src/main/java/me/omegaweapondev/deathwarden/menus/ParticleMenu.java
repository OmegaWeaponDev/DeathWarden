package me.omegaweapondev.deathwarden.menus;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.ItemCreator;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.omegaweapondev.deathwarden.utils.UserDataHandler;
import me.ou.library.Utilities;
import me.ou.library.menus.MenuCreator;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class ParticleMenu extends MenuCreator {
  private final DeathWarden plugin;
  private final FileConfiguration configFile;
  private final FileConfiguration deathEffectsConfig;
  private final MessageHandler messageHandler;

  private UserDataHandler userData;
  private ItemCreator itemCreator;
  private DeathEffectsMenu deathEffectsMenu;

  public ParticleMenu(final DeathWarden plugin, int inventoryRows, String inventoryName, String defaultInventoryName) {
    super(inventoryRows, inventoryName, defaultInventoryName);
    this.plugin = plugin;
    configFile = plugin.getSettingsHandler().getConfigFile().getConfig();
    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
    deathEffectsConfig = plugin.getSettingsHandler().getDeathEffectMenus().getConfig();

    int slot = -2;

    for(String itemName : deathEffectsConfig.getConfigurationSection("Death_Particles_Menu.Particles").getKeys(false)) {

      if(slot++ > 33) {
        Utilities.logWarning(true, "You can only have 33 particles in the Particles Menu!");
        return;
      }

      setItem(slot + 1, createItem("Death_Particles_Menu.Particles." + itemName), player -> {
        userData = new UserDataHandler(plugin, player, player.getUniqueId());

        userData.getPlayerData().set("Death_Effects.Death_Particle", itemName.toUpperCase());
        userData.savePlayerData();
      });
    }

    setItem(35, createItemStack("BARRIER", Utilities.colourise("#570000Close"), Utilities.colourise(Arrays.asList("#ff4a4aClick here to close", "#ff4a4athe name colour gui"))), HumanEntity::closeInventory);

    setItem(34, createItemStack("OAK_DOOR", Utilities.colourise("#570000Return"), Utilities.colourise(Arrays.asList("#ff4a4aClick here to return", "#ff4a4ato the previous menu"))), player -> {
      player.closeInventory();

      deathEffectsMenu = new DeathEffectsMenu(plugin, 3, deathEffectsConfig.getString("Death_Effects_Menu.Menu_Title"), "#00D4FFDeath Effects Menu");
      deathEffectsMenu.openInventory(player);
    });
  }

  private ItemStack createItem(final String deathEffectItem) {

    if(Material.getMaterial(deathEffectsConfig.getString(deathEffectItem + ".Material").toUpperCase()) == null) {
      itemCreator = new ItemCreator(Material.BARRIER);
      itemCreator.setDisplayName("#ff4a4aInvalid Material");
      itemCreator.setLore("#ff4a4aThis item is invalid.", "#ff4a4aPlease pick another material to use", "#ff4a4athat is supported by your server version");

      return itemCreator.getItem();
    }

    itemCreator = new ItemCreator(Material.getMaterial(deathEffectsConfig.getString(deathEffectItem + ".Material").toUpperCase()));
    itemCreator.setDisplayName(deathEffectsConfig.getString(deathEffectItem + ".Title"));
    itemCreator.setLore(deathEffectsConfig.getStringList(deathEffectItem + ".Lore"));

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
