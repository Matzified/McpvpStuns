package club.mcpvp.stuns.listener;

import club.mcpvp.stuns.McPvpStuns;
import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Combat listener handling 1:1 mcpvp.club stun & 1-tick knockback mechanics.
 */
public final class StunCombatListener implements Listener {

    private static final Set<Material> AXE_MATERIALS = EnumSet.of(
        Material.WOODEN_AXE,
        Material.STONE_AXE,
        Material.IRON_AXE,
        Material.GOLDEN_AXE,
        Material.DIAMOND_AXE,
        Material.NETHERITE_AXE
    );

    private final McPvpStuns plugin;
    private final Map<UUID, StunRecord> activeStuns = new ConcurrentHashMap<>();

    public record StunRecord(UUID attackerId, long breakTimeMillis) {}

    public StunCombatListener(McPvpStuns plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onShieldDisable(PlayerShieldDisableEvent event) {
        if (!plugin.getConfig().getBoolean("runtime-stun-override", true)) {
            return;
        }

        Player defender = event.getPlayer();
        int cooldownTicks = Math.max(0, plugin.getConfig().getInt("shield-cooldown-ticks", 100));
        event.setCooldown(cooldownTicks);

        if (plugin.getConfig().getBoolean("reset-invulnerability-on-break", true)) {
            // Immediate zeroing of invulnerability frames on the next tick
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (defender.isValid()) {
                    defender.setNoDamageTicks(0);
                }
            });
        }

        if (plugin.getConfig().getBoolean("play-stun-sound", true)) {
            defender.getWorld().playSound(defender.getLocation(), Sound.ITEM_SHIELD_BREAK, 1.0F, 0.9F);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!plugin.getConfig().getBoolean("runtime-stun-override", true)) {
            return;
        }

        if (!(event.getEntity() instanceof Player defender)) {
            return;
        }

        Entity rawDamager = event.getDamager();
        if (!(rawDamager instanceof LivingEntity attacker)) {
            return;
        }

        long now = System.currentTimeMillis();
        long windowMs = plugin.getConfig().getLong("followup-window-ms", 600L);

        // Case 1: Follow-up hit landed after shield break
        StunRecord record = activeStuns.get(defender.getUniqueId());
        if (record != null && (now - record.breakTimeMillis()) <= windowMs && record.attackerId().equals(attacker.getUniqueId())) {
            activeStuns.remove(defender.getUniqueId());

            if (plugin.getConfig().getBoolean("reset-invulnerability-on-break", true)) {
                defender.setNoDamageTicks(0);
            }

            if (plugin.getConfig().getBoolean("play-slam-sound", true)) {
                defender.getWorld().playSound(defender.getLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.2F, 1.1F);
            }

            double hMult = plugin.getConfig().getDouble("followup-knockback-horizontal-multiplier", 1.0D);
            double vMult = plugin.getConfig().getDouble("followup-knockback-vertical-multiplier", 1.0D);

            if (hMult != 1.0D || vMult != 1.0D) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!defender.isValid() || !attacker.isValid()) return;
                    Vector dir = defender.getLocation().toVector().subtract(attacker.getLocation().toVector()).setY(0);
                    if (dir.lengthSquared() > 1.0E-4D) {
                        dir.normalize();
                        defender.setVelocity(new Vector(dir.getX() * 0.4D * hMult, 0.35D * vMult, dir.getZ() * 0.4D * hMult));
                    }
                });
            }
            return;
        }

        // Case 2: Axe strike against active shield
        if (defender.isBlocking()) {
            // Cancel vanilla i-frames on block so rapid follow-ups register
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (defender.isValid()) {
                    defender.setNoDamageTicks(0);
                }
            });

            EntityEquipment eq = attacker.getEquipment();
            ItemStack held = eq != null ? eq.getItemInMainHand() : null;

            if (held != null && AXE_MATERIALS.contains(held.getType())) {
                activeStuns.put(defender.getUniqueId(), new StunRecord(attacker.getUniqueId(), now));

                // Cancel knockback on the shield-breaking strike itself so target stays in range
                if (plugin.getConfig().getBoolean("knockback-on-followup-only", true)) {
                    Vector currentVel = defender.getVelocity().clone();
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (defender.isValid()) {
                            defender.setVelocity(new Vector(currentVel.getX() * 0.05D, currentVel.getY(), currentVel.getZ() * 0.05D));
                        }
                    });
                }
            }
        }
    }

    public void cleanExpiredStuns() {
        long now = System.currentTimeMillis();
        long windowMs = plugin.getConfig().getLong("followup-window-ms", 600L);
        activeStuns.entrySet().removeIf(entry -> (now - entry.getValue().breakTimeMillis()) > windowMs * 2);
    }
}
