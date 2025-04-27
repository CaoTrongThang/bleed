package com.trongthang.bleed.client;

import com.trongthang.bleed.managers.EffectsManager;
import net.combatroll.internals.RollManager;
import net.combatroll.internals.RollingEntity;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static com.trongthang.bleed.Bleed.*;

public class BleedClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(SEND_SINGLE_ENTITY_WITH_BLEEDING_EFFECT,
                (client, handler, buf, responseSender) -> {
                    int entityId = buf.readInt();

                    client.execute(() -> {
                        updateBleedingParticles(entityId);
                    });
                });
    }

    private static void updateBleedingParticles(int entityId) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        Entity entity = client.world.getEntityById(entityId);

        if (entity instanceof LivingEntity livingEntity && !livingEntity.isDead()) {
            spawnBloodParticles(livingEntity);
        }
    }

    private static void spawnBloodParticles(LivingEntity entity) {
        World world = entity.getWorld();
        Vec3d pos = entity.getPos();
        int particleCount = 12;

        boolean isPlayer = entity instanceof PlayerEntity;

        for (int i = 0; i < particleCount; ++i) {
            double offsetX = (world.random.nextDouble() - 0.5) * (double) entity.getWidth();
            double offsetY = isPlayer ? world.random.nextDouble() * ((double) entity.getHeight() * 0.8) : world.random.nextDouble() * (double) entity.getHeight();
            double offsetZ = (world.random.nextDouble() - 0.5) * (double) entity.getWidth();
            world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.getDefaultState()), pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 0.0, 0.0, 0.0);
            world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.REDSTONE_BLOCK.getDefaultState()), pos.x + offsetX, pos.y + offsetY, pos.z + offsetZ, 0.0, 0.0, 0.0);
        }
    }
}
