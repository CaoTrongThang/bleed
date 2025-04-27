package com.trongthang.bleed.managers;

import com.trongthang.bleed.Bleed;
import com.trongthang.bleed.effects.BleedingEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class EffectsManager {
    public static final StatusEffect BLEEDING = Registry.register(
            Registries.STATUS_EFFECT,
            new Identifier(Bleed.MOD_ID, "bleeding"),
            new BleedingEffect() // Your custom StatusEffect subclass
    );;

    public static void registerEffects() {
    }
}
