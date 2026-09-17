package club.mcpvp.stuns.fabric.mixin;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
 * Replicates 1:1 mcpvp.club shield stun & follow-up launch knockback on Fabric:
 * 1. Cancels invulnerability frames on shield block and axe break so the follow-up hit connects on tick 1.
 * 2. When shield breaks, suppresses knockback so the target stays locked in front.
 * 3. On the follow-up hit, launches the defender into the air.
 * 4. Silent operation (no artificial sound effects).
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

    @Inject(method = "blockUsingShield", at = @At("TAIL"))
    protected void onBlockUsingShield(LivingEntity attacker, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        self.invulnerableTime = 0;
        self.hurtDuration = 0;
        self.hurtTime = 0;
    }

    @Inject(method = "hurt", at = @At("HEAD"))
    private void onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        Entity directAttacker = source.getEntity();

        if (directAttacker instanceof LivingEntity attacker) {
            long now = System.currentTimeMillis();
            Long breakTime = LAST_SHIELD_BREAK.get(self.getUUID());
            UUID breakAttackerId = SHIELD_BREAK_ATTACKER.get(self.getUUID());

            // Check if this is the follow-up hit after shield was broken
            if (breakTime != null && (now - breakTime) <= 1200 && attacker.getUUID().equals(breakAttackerId)) {
                LAST_SHIELD_BREAK.remove(self.getUUID());
                SHIELD_BREAK_ATTACKER.remove(self.getUUID());

                // Clear i-frames completely so follow-up connects
                self.invulnerableTime = 0;

                // Launch target up in the air
                Vec3 attackerPos = attacker.position();
                Vec3 targetPos = self.position();
                Vec3 dir = new Vec3(targetPos.x - attackerPos.x, 0, targetPos.z - attackerPos.z);
                if (dir.lengthSqr() > 1.0E-4) {
                    dir = dir.normalize();
                } else {
                    dir = attacker.getLookAngle();
                    dir = new Vec3(dir.x, 0, dir.z).normalize();
                }

                // Calibrated for stun-web combo: max ~0.5 blocks horizontal back and ~1.5 blocks vertical lift
                self.setDeltaMovement(dir.x * 0.16, 0.38, dir.z * 0.16);
                self.hurtMarked = true;
                return;
            }

            // Check if this hit is breaking the shield with an axe
            if (self.isBlocking()) {
                self.invulnerableTime = 0;
                ItemStack held = attacker.getMainHandItem();
                if (held != null && held.getItem() instanceof AxeItem) {
                    LAST_SHIELD_BREAK.put(self.getUUID(), now);
                    SHIELD_BREAK_ATTACKER.put(self.getUUID(), attacker.getUUID());
                    self.invulnerableTime = 0;
                }
            }
        }
    }

    @Inject(method = "knockback", at = @At("HEAD"), cancellable = true)
    private void onKnockback(double strength, double x, double z, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        Long breakTime = LAST_SHIELD_BREAK.get(self.getUUID());

        // Cancel knockback only from the shield breaking strike itself
        if (breakTime != null && (System.currentTimeMillis() - breakTime) < 80) {
            ci.cancel();
        }
    }
}
