package com.trongthang.bleed.items;

import com.trongthang.bleed.Bleed;
import com.trongthang.bleed.ModConfig;
import com.trongthang.bleed.managers.EffectsManager;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;

public class HealingNeedle extends Item {
    public HealingNeedle(Settings settings) {
        super(settings.maxCount(1).maxDamage(20));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return ModConfig.getInstance().healingNeedleConsumeTimeInTick;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {

        Text line1 = Text.literal("Heals 10 health, removes Bleeding Effect, and gives Regenerate Effect")
                .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY));

        tooltip.add(line1);

        tooltip.add(Text.literal(""));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) user;

            player.heal(10.0f);

            if(player.hasStatusEffect(EffectsManager.BLEEDING)){
                player.removeStatusEffect(EffectsManager.BLEEDING);
            }

            player.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 300, 1));

            // Damage the needle (reduce durability)
            stack.damage(stack.getMaxDamage(), player, (entity) -> {
                entity.sendToolBreakStatus(Hand.MAIN_HAND);
            });


            // Optional: Add a cooldown (20 ticks = 1 second)
            player.getItemCooldownManager().set(this, 20);
        }

        return stack;
    }
}
