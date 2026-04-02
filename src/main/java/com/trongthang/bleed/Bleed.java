package com.trongthang.bleed;

import com.trongthang.bleed.managers.EffectsManager;
import com.trongthang.bleed.managers.ItemsManager;
import net.combatroll.CombatRoll;
import net.combatroll.api.event.ServerSideRollEvents;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import static net.minecraft.server.command.CommandManager.literal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Random;

public class Bleed implements ModInitializer {
    public static final String MOD_ID = "bleed";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Identifier SEND_SINGLE_ENTITY_WITH_BLEEDING_EFFECT = new Identifier(MOD_ID, "bleeding_mob");
    public static final Identifier RELOAD_CONFIG = new Identifier(MOD_ID, "reload_config");

    public static Random random = new Random();

    public static boolean IS_COMBAT_ROLL_ENABLE = false;

    public static HashMap<LivingEntity, Integer> livingEntityInvulnerableTicks = new HashMap<>();

    @Override
    public void onInitialize() {
        ModConfig.loadConfig();
        EffectsManager.registerEffects();
        ItemsManager.register();
        ItemsManager.addingItemToTab();

        registerCommands();

        IS_COMBAT_ROLL_ENABLE = FabricLoader.getInstance().isModLoaded("combatroll");
        if (IS_COMBAT_ROLL_ENABLE) {
            setupCombatRollIntegration();
        }
    }

    private void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(literal("bleed")
                    .then(literal("reloadconfig")
                            .requires(source -> source.hasPermissionLevel(2))
                            .executes(context -> {
                                ModConfig.loadConfig();

                                // Sync to all clients
                                context.getSource().getServer().getPlayerManager().getPlayerList().forEach(player -> {
                                    ServerPlayNetworking.send(player, RELOAD_CONFIG, PacketByteBufs.empty());
                                });

                                context.getSource().sendFeedback(() -> Text.literal("Bleed config reloaded!"), true);
                                return 1;
                            })));
        });
    }

    private void setupCombatRollIntegration() {
        // 1. Register roll start event
        ServerSideRollEvents.PLAYER_START_ROLLING.register((player, direction) -> {
            int invulnerabilityTicks = CombatRoll.config.invulnerable_ticks_upon_roll;
            livingEntityInvulnerableTicks.put(player, invulnerabilityTicks);
        });

        // 2. Register server tick for countdown
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            List<LivingEntity> toRemove = new ArrayList<>();

            livingEntityInvulnerableTicks.forEach((player, ticks) -> {
                if (ticks <= 0) {
                    toRemove.add(player);
                } else {
                    livingEntityInvulnerableTicks.put(player, ticks - 1);
                }
            });

            // Remove expired entries
            toRemove.forEach(livingEntityInvulnerableTicks::remove);
        });
    }

    public static boolean hasRollInvulnerability(LivingEntity player) {
        return livingEntityInvulnerableTicks.containsKey(player);
    }
}