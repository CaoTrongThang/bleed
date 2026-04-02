package com.trongthang.bleed.client;

import com.trongthang.bleed.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.BlockDustParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import net.minecraft.world.World;

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

        ClientPlayNetworking.registerGlobalReceiver(RELOAD_CONFIG,
                (client, handler, buf, responseSender) -> {
                    client.execute(ModConfig::loadConfig);
                });
    }

    private static void updateBleedingParticles(int entityId) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null)
            return;

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

        String entityIdStr = Registries.ENTITY_TYPE.getId(entity.getType()).toString();
        Vector3f colorVec = ModConfig.getInstance().getEntityBloodColor(entityIdStr);

        for (int i = 0; i < particleCount; ++i) {
            double offsetX = (world.random.nextDouble() - 0.5) * (double) entity.getWidth();
            double offsetY = isPlayer ? world.random.nextDouble() * ((double) entity.getHeight() * 0.8)
                    : world.random.nextDouble() * (double) entity.getHeight();
            double offsetZ = (world.random.nextDouble() - 0.5) * (double) entity.getWidth();

            double x = pos.x + offsetX;
            double y = pos.y + offsetY;
            double z = pos.z + offsetZ;

            double velX = (world.random.nextDouble() - 0.5) * 0.5;
            double velY = world.random.nextDouble() * 0.5;
            double velZ = (world.random.nextDouble() - 0.5) * 0.5;

            BlockDustParticle particle = new BlockDustParticle((ClientWorld) world, x, y, z, velX, velY, velZ,
                    Blocks.WHITE_CONCRETE.getDefaultState(), BlockPos.ofFloored(x, y, z));
            particle.setColor(colorVec.x() * 0.8f, colorVec.y() * 0.8f, colorVec.z() * 0.8f);
            MinecraftClient.getInstance().particleManager.addParticle(particle);
        }
    }
}
