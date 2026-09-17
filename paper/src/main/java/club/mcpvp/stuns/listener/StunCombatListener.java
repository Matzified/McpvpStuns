package club.mcpvp.stuns.listener;

import club.mcpvp.stuns.McPvpStuns;
import io.papermc.paper.event.player.PlayerShieldDisableEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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
 * Combat listener replicating exact mcpvp.club shield stun & follow-up knockback mechanics:
 * 1. Cancels invulnerability frames on shield block and axe break so the follow-up hit connects on tick 1.
 * 2. When shield breaks, suppresses horizontal knockback so the target stays locked in front of the attacker.
 * 3. On the follow-up hit (e.g. mace or sword), launches the target directly into the air with vertical knockback.
 * 4. Silent operation (no artificial sound effects).
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

    @EventHandler(priority = EventPriority.LOWEST)
    public void onShieldDisable(PlayerShieldDisableEvent event) {
        if (!plugin.getConfig().getBoolean("runtime-stun-override", true)) {
            return;
        }

        Player defender = event.getPlayer();
        int cooldownTicks = Math.max(0, plugin.getConfig().getInt("shield-cooldown-ticks", 100));
        event.setCooldown(cooldownTicks);

        // Instantly eliminate i-frames so the follow-up hit on tick 1 connects
        defender.setNoDamageTicks(0);
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (defender.isValid()) {
                defender.setNoDamageTicks(0);
            }
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamagePre(EntityDamageByEntityEvent event) {
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

        // Always ensure no damage ticks are blocking hits if defender is shielding or recently stunned
        if (defender.isBlocking() || activeStuns.containsKey(defender.getUniqueId())) {
            defender.setNoDamageTicks(0);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
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
        long windowMs = plugin.getConfig().getLong("followup-window-ms", 1200L);

        // Case 1: Follow-up hit after shield was broken
        StunRecord record = activeStuns.get(defender.getUniqueId());
        if (record != null && (now - record.breakTimeMillis()) <= windowMs && record.attackerId().equals(attacker.getUniqueId())) {
            activeStuns.remove(defender.getUniqueId());

            // If vanilla cancelled or swallowed this hit due to damage ticks, uncancel it!
            if (event.isCancelled()) {
                event.setCancelled(false);
            }

            defender.setNoDamageTicks(0);

            // Launch the player up in the air with strong follow-up knockback
            double hMult = plugin.getConfig().getDouble("followup-knockback-horizontal-multiplier", 1.0D);
            double vMult = plugin.getConfig().getDouble("followup-knockback-vertical-multiplier", 1.0D);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!defender.isValid() || !attacker.isValid()) return;

                Vector dir = defender.getLocation().toVector().subtract(attacker.getLocation().toVector());
                dir.setY(0);
                if (dir.lengthSquared() > 1.0E-4D) {
                    dir.normalize();
                } else {
                    dir = attacker.getLocation().getDirection().setY(0).normalize();
                }

                // Calibrated for stun-web combo: max ~0.5 blocks horizontal back and ~1.5 blocks vertical lift
                Vector launch = new Vector(dir.getX() * 0.16D * hMult, 0.38D * vMult, dir.getZ() * 0.16D * hMult);
                defender.setVelocity(launch);
            });
            return;
        }

        // Case 2: Axe hitting a blocking shield
        if (defender.isBlocking()) {
            defender.setNoDamageTicks(0);
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (defender.isValid()) {
                    defender.setNoDamageTicks(0);
                }
            });

            EntityEquipment eq = attacker.getEquipment();
            ItemStack held = eq != null ? eq.getItemInMainHand() : null;

            if (held != null && AXE_MATERIALS.contains(held.getType())) {
                activeStuns.put(defender.getUniqueId(), new StunRecord(attacker.getUniqueId(), now));

                // Suppress knockback on the shield break strike itself so target stays locked in front
                if (plugin.getConfig().getBoolean("knockback-on-followup-only", true)) {
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (defender.isValid()) {
                            Vector v = defender.getVelocity();
                            defender.setVelocity(new Vector(0, v.getY() < 0 ? v.getY() : 0, 0));
                        }
                    });
                }
            }
        }
    }
}
