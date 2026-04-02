package com.trongthang.bleed.mixin;

import com.trongthang.bleed.ModConfig;
import com.trongthang.bleed.managers.EffectsManager;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.trongthang.bleed.Bleed.*;
import static com.trongthang.bleed.effects.BleedingEffect.shouldBleed;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "damage", at = @At("HEAD"))
    private void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {

        if (!ModConfig.getInstance().modEnable)
            return;
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity.getWorld().isClient)
            return;

        if (entity.isInvulnerableTo(source)) {
            return;
        }

        if (entity.isBlocking() && entity.getActiveItem().isDamageable()) {
            return;
        }

        if (source.isOf(DamageTypes.MAGIC))
            return;

        if (shouldBleed(entity, amount)) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(entity.getId());
            for (ServerPlayerEntity p : PlayerLookup.tracking(entity)) {
                ServerPlayNetworking.send(p, SEND_SINGLE_ENTITY_WITH_BLEEDING_EFFECT, buf);
            }

            entity.addStatusEffect(
                    new StatusEffectInstance(
                            EffectsManager.BLEEDING,
                            ModConfig.getInstance().bleedingDurationInTick,
                            0,
                            false,
                            false,
                            true),
                    source.getAttacker() // Add source entity for proper effect attribution
            );
        }
    }
}
