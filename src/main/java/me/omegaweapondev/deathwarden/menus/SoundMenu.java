package me.omegaweapondev.deathwarden.menus;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.ItemCreator;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.omegaweapondev.deathwarden.utils.StorageManager;
import me.ou.library.Utilities;
import me.ou.library.menus.MenuCreator;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class SoundMenu extends MenuCreator {
  private final DeathWarden plugin;
  private final FileConfiguration configFile;
  private final FileConfiguration deathEffectsConfig;
  private final MessageHandler messageHandler;

  private StorageManager storageManager;
  private ItemCreator itemCreator;
  private DeathEffectsMenu deathEffectsMenu;

  public SoundMenu(final DeathWarden plugin, int inventoryRows, String inventoryName, String defaultInventoryName) {
    super(inventoryRows, inventoryName, defaultInventoryName);
    this.plugin = plugin;
    storageManager = new StorageManager(plugin);
    configFile = storageManager.getConfigFile().getConfig();
    messageHandler = new MessageHandler(plugin, storageManager.getMessagesFile().getConfig());
    deathEffectsConfig = storageManager.getDeathEffectMenus().getConfig();

    int slot = -2;

    for(String itemName : deathEffectsConfig.getConfigurationSection("Death_Sounds_Menu.Sounds").getKeys(false)) {

      if (slot++ > 33) {
        Utilities.logWarning(true, "You can only have 33 sounds in the Sounds Menu!");
        return;
      }

      setItem(slot + 1, createItem(deathEffectsConfig.getString("Death_Sounds_Menu.Sounds." + itemName)), player -> {
        storageManager = new StorageManager(plugin, player, player.getUniqueId());

        storageManager.setUserString("Death_Effects.Death_Sounds", itemName.toUpperCase());
        storageManager.savePlayerData();
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
    itemCreator.setLore(deathEffectsConfig.getStringList(deathEffectsConfig + ".Lore"));

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
