package me.omegaweapondev.deathwarden.menus;

import me.omegaweapondev.deathwarden.DeathWarden;
import me.omegaweapondev.deathwarden.utils.MessageHandler;
import me.omegaweapondev.deathwarden.utils.UserDataHandler;
import me.ou.library.Utilities;
import me.ou.library.builders.ItemBuilder;
import me.ou.library.menus.MenuCreator;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class SoundMenu extends MenuCreator {
  private final DeathWarden plugin;
  private final MessageHandler messageHandler;
  private final FileConfiguration deathEffectsConfig;

  private UserDataHandler userData;
  private DeathEffectsMenu deathEffectsMenu;

  public SoundMenu(final DeathWarden plugin, int inventoryRows, String inventoryName, String defaultInventoryName) {
    super(inventoryRows, inventoryName, defaultInventoryName);
    this.plugin = plugin;
    messageHandler = new MessageHandler(plugin, plugin.getSettingsHandler().getMessagesFile().getConfig());
    deathEffectsConfig = plugin.getSettingsHandler().getDeathEffectMenus().getConfig();

    int slot = -2;

    for(String itemName : plugin.getSettingsHandler().getDeathEffectMenus().getConfig().getConfigurationSection("Death_Sounds_Menu.Sounds").getKeys(false)) {

      if (slot++ > 33) {
        Utilities.logWarning(true, "You can only have 33 sounds in the Sounds Menu!");
        return;
      }

      setItem(slot + 1, createItem("Death_Sounds_Menu.Sounds." + itemName), player -> {
        userData = new UserDataHandler(plugin, player, player.getUniqueId());

        userData.getPlayerData().set("Death_Effects.Death_Sound", itemName.toUpperCase());
        userData.savePlayerData();
      });
    }

    setItem(35, createItemStack("BARRIER", Utilities.colourise("#570000Close"), Utilities.colourise(Arrays.asList("#ff4a4aClick here to close", "#ff4a4athe name colour gui"))), HumanEntity::closeInventory);

    setItem(34, createItemStack("OAK_DOOR", Utilities.colourise("#570000Return"), Utilities.colourise(Arrays.asList("#ff4a4aClick here to return", "#ff4a4ato the previous menu"))), player -> {
      player.closeInventory();

      deathEffectsMenu = new DeathEffectsMenu(plugin, 3, plugin.getSettingsHandler().getDeathEffectMenus().getConfig().getString("Death_Effects_Menu.Menu_Title"), "#00D4FFDeath Effects Menu");
      deathEffectsMenu.openInventory(player);
    });
  }

  private ItemStack createItem(final String deathEffectItem) {
    ItemBuilder itemBuilder = new ItemBuilder(Material.getMaterial(deathEffectsConfig.getString(deathEffectItem + ".Material")));
    return itemBuilder.checkInvalidMaterial(deathEffectsConfig.getString(deathEffectItem + ".Material"), deathEffectsConfig.getString(deathEffectItem + ".Title"), deathEffectsConfig.getStringList(deathEffectItem + ".Lore"));
  }

  private ItemStack createItemStack(final String material, final String name, final List<String> lore) {
    ItemBuilder itemBuilder = new ItemBuilder(Material.getMaterial(material));
    return itemBuilder.checkInvalidMaterial(material, name, lore);
  }
}
