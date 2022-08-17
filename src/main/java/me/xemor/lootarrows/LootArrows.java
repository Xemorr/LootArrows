package me.xemor.lootarrows;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

public final class LootArrows extends JavaPlugin implements Listener {

    NamespacedKey namespacedKey = new NamespacedKey(this, "ArrowsStuck");
    NamespacedKey infinity = new NamespacedKey(this, "Infinity");

    @Override
    public void onEnable() {
        // Plugin startup logic
        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onDeath(EntityDeathEvent e) {
        World world = e.getEntity().getWorld();
        Location location = e.getEntity().getLocation();
        PersistentDataContainer persistentDataContainer = e.getEntity().getPersistentDataContainer();
        Integer arrowsToDrop = persistentDataContainer.get(namespacedKey, PersistentDataType.INTEGER);
        if (arrowsToDrop == null) {
            return;
        }
        persistentDataContainer.set(namespacedKey, PersistentDataType.INTEGER, 0);
        ItemStack itemStack = new ItemStack(Material.ARROW, arrowsToDrop);
        if (itemStack.getAmount() == 0 || itemStack.getType() == Material.AIR) {
            return;
        }
        world.dropItemNaturally(location, itemStack);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if (e.isCancelled()) return;
        if (e.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getDamager();
            PersistentDataContainer arrowContainer = e.getDamager().getPersistentDataContainer();
            if (!arrowContainer.has(infinity, PersistentDataType.INTEGER)) {
                if (arrow.getPickupStatus() == AbstractArrow.PickupStatus.ALLOWED) {
                    PersistentDataContainer entityContainer = e.getEntity()  .getPersistentDataContainer();
                    Integer currentArrows = entityContainer.get(namespacedKey, PersistentDataType.INTEGER);
                    if (currentArrows == null) {
                        currentArrows = 0;
                    }
                    entityContainer.set(namespacedKey, PersistentDataType.INTEGER, currentArrows + 1);
                }
            }
        }
    }

    @EventHandler
    public void onShoot(ProjectileLaunchEvent e) {
        if (e.getEntity() instanceof Arrow) {
            Arrow arrow = (Arrow) e.getEntity();
            if (arrow.getShooter() instanceof Player) {
                Player player = (Player) arrow.getShooter();
                ItemStack itemStack = player.getInventory().getItemInMainHand();
                if (itemStack.hasItemMeta()) {
                    Integer infinityLevel = itemStack.getItemMeta().getEnchants().get(Enchantment.ARROW_INFINITE);
                    if (infinityLevel == null) {
                        return;
                    }
                    if (infinityLevel > 0) {
                        arrow.getPersistentDataContainer().set(infinity, PersistentDataType.INTEGER, 1);
                    }
                }
            }
        }
    }
}
