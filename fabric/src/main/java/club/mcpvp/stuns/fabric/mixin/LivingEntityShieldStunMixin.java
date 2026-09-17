package club.mcpvp.stuns.fabric.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Replicates 1:1 mcpvp.club shield stun & knockback mechanics on Fabric:
 * 1. Skips damage tick (invulnerability / hurtResistantTime) when damage is blocked by shield.
 * 2. When shield is broken by an axe, suppresses velocity from the break hit and sets invulnerability ticks to 0.
 * 3. When attacker lands follow-up hit within the 1-tick / 600ms combo window, delivers full knockback and damage.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityShieldStunMixin extends Entity {

    @Shadow public int hurtTime;
    @Shadow public int hurtDuration;

    public LivingEntityShieldStunMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    private static final Map<UUID, Long> LAST_SHIELD_BREAK = new ConcurrentHashMap<>();
    private static final Map<UUID, UUID> SHIELD_BREAK_ATTACKER = new ConcurrentHashMap<>();

    /**
     * Skip vanilla damage tick / invulnerability when damage is blocked with shield.
     * Equivalent to Paper's skip-vanilla-damage-tick-when-shield-blocked.
     */
    @Inject(method = "blockUsingShield", at = @At("TAIL"))
    protected void onBlockUsingShield(LivingEntity attacker, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        // Clear invulnerability frames immediately
        self.invulnerableTime = 0;
        self.hurtDuration = 0;
        self.hurtTime = 0;
    }

    /**
     * Intercept hurt / damage processing to handle the shield break and follow-up knockback timing.
     */
    @Inject(method = "hurt", at = @At("HEAD"))
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        Entity directAttacker = source.getEntity();

        if (directAttacker instanceof LivingEntity attacker) {
            long now = System.currentTimeMillis();
            Long breakTime = LAST_SHIELD_BREAK.get(self.getUUID());
            UUID breakAttackerId = SHIELD_BREAK_ATTACKER.get(self.getUUID());

            // Check if this is the instant 1-tick follow-up hit after shield was broken
            if (breakTime != null && (now - breakTime) <= 600 && attacker.getUUID().equals(breakAttackerId)) {
                // Remove record and ensure target takes the hit without any i-frame block
                LAST_SHIELD_BREAK.remove(self.getUUID());
                SHIELD_BREAK_ATTACKER.remove(self.getUUID());
                self.invulnerableTime = 0;
                return;
            }

            // Check if this hit is breaking the shield
            if (self.isBlocking()) {
                ItemStack held = attacker.getMainHandItem();
                if (held != null && held.getItem() instanceof AxeItem) {
                    // Axe hit on blocking shield: record shield break!
                    LAST_SHIELD_BREAK.put(self.getUUID(), now);
                    SHIELD_BREAK_ATTACKER.put(self.getUUID(), attacker.getUUID());

                    // Clear invulnerability on shield break so follow-up can register on next tick
                    self.invulnerableTime = 0;
                }
            }
        }
    }

    /**
     * Apply knockback only on the follow-up hit, not during the shield breaking strike itself.
     */
    @Inject(method = "knockback", at = @At("HEAD"), cancellable = true)
    private void onKnockback(double strength, double x, double z, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        Long breakTime = LAST_SHIELD_BREAK.get(self.getUUID());

        if (breakTime != null) {
            long diff = System.currentTimeMillis() - breakTime;
            // If the knockback is triggered during the shield break (within 50ms), cancel/suppress it
            if (diff < 50) {
                ci.cancel();
            }
        }
    }
}
