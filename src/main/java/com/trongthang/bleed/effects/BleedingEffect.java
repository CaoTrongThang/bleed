package com.trongthang.bleed.effects;

import com.trongthang.bleed.ModConfig;
import com.trongthang.bleed.managers.EffectsManager;
import net.combatroll.CombatRoll;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import static com.trongthang.bleed.Bleed.*;

public class BleedingEffect extends StatusEffect {

    public BleedingEffect() {
        super(StatusEffectCategory.HARMFUL, 0x8B0000);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyUpdateEffect(LivingEntity entity, int amplifier) {

        if (entity.getWorld().isClient) return;
        ServerWorld world = (ServerWorld) entity.getWorld();

        if (world.getTimeOfDay() % ModConfig.getInstance().bleedingCheckInterval == 0) {
            ModConfig modConfig = ModConfig.getInstance();
            float damage = ModConfig.getInstance().bleedingDamage;

            if (ModConfig.getInstance().bleedingDamageDecreaseByArmor) {
                damage =  Math.min(calculateAdjustedDamageBaseOnArmor(entity), modConfig.maxBleedingDamageForHostileMobs);
                entity.damage(entity.getDamageSources().magic(), damage);
            } else {

                if (entity instanceof HostileEntity) {
                    if(ModConfig.getInstance().bleedingDamageScaleForHostielMobs){
                        damage = Math.min(damage + ((entity.getMaxHealth() * modConfig.bleedingDamageScaleByPercentOfHostileMobs) / 100f), modConfig.maxBleedingDamageForHostileMobs);;
                        entity.damage(entity.getDamageSources().magic(), damage);
                    } else {
                        entity.damage(entity.getDamageSources().magic(), damage);
                    }
                } else {
                    if(entity.isSneaking()){
                        entity.damage(entity.getDamageSources().magic(), damage / 2);
                    } else {
                        entity.damage(entity.getDamageSources().magic(), damage);
                    }

                }
            }
        } else if (world.getTimeOfDay() % 15 == 0) {
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(entity.getId());

            // Fix: Include the entity itself if it's a player
            for (ServerPlayerEntity p : PlayerLookup.tracking(entity)) {
                ServerPlayNetworking.send(p, SEND_SINGLE_ENTITY_WITH_BLEEDING_EFFECT, buf);
            }

            // Add this block to send to self
            if (entity instanceof ServerPlayerEntity selfPlayer) {
                ServerPlayNetworking.send(selfPlayer, SEND_SINGLE_ENTITY_WITH_BLEEDING_EFFECT, buf);
            }
        }
    }

    public static boolean shouldBleed(LivingEntity entity, float damage) {
        if(entity.getWorld().isClient) return false;
        if (entity.hasStatusEffect(EffectsManager.BLEEDING)) return false;
        if(hasRollInvulnerability(entity)) return false;



        if (entity.hasStatusEffect(StatusEffects.RESISTANCE)) {
            if(entity.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() > 100)
            return false;
        }

        String entityId = EntityType.getId(entity.getType()).toString();
        if (ModConfig.getInstance().blacklistMobs.contains(entityId)) {
            return false;
        }

        if (damage < ModConfig.getInstance().minDamageToGetBleed) return false;
        float baseChance = ModConfig.getInstance().bleedingChance;
        final float armor = entity.getArmor();

        if (ModConfig.getInstance().bleedingChanceDecreaseByArmor && armor > 0) {
            final float scalingFactor = 0.05f;
            final float minChance = 0.05f;

            // Calculate armor-adjusted chance
            float adjustedChance = baseChance / (1 + scalingFactor * armor);
            baseChance = Math.max(minChance, adjustedChance);
        }

        int rand = random.nextInt(0, 100);

        final boolean shouldBleed = rand < (baseChance * 100);

        return shouldBleed;
    }

    private static float calculateAdjustedDamageBaseOnArmor(LivingEntity entity) {
        if (ModConfig.getInstance().bleedingDamageDecreaseByArmor) {
            return ModConfig.getInstance().bleedingDamage;
        }

        float k = 10f;
        float minDamage = 1;

        float armor = entity.getArmor();

        float reduction = armor / (armor + k);
        float adjusted = 0;
        if(ModConfig.getInstance().bleedingDamageScaleForHostielMobs){
            if (entity instanceof HostileEntity) {
                adjusted = (ModConfig.getInstance().bleedingDamage + ((entity.getMaxHealth() * ModConfig.getInstance().bleedingDamageScaleByPercentOfHostileMobs) / 100)) * (1 - reduction);
            }
        } else {
            adjusted = (ModConfig.getInstance().bleedingDamage * (1 - reduction));
        }

        if (ModConfig.getInstance().doesSneakingReduceBleedingDamage) {
            float finalDamage = entity.isSneaking() ? adjusted / ModConfig.getInstance().bleedingDamageReducedWhileSneaking : adjusted;
            return Math.max(minDamage, finalDamage);
        } else {
            return Math.max(minDamage, adjusted);
        }
    }
}
