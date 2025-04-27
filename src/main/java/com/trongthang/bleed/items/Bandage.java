package com.trongthang.bleed.items;

import com.trongthang.bleed.ModConfig;
import com.trongthang.bleed.managers.EffectsManager;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;

import java.util.List;

public class Bandage extends Item {
    public Bandage(Settings settings) {
        super(settings.maxDamage(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        user.setCurrentHand(hand);
        return TypedActionResult.consume(stack);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        return ModConfig.getInstance().bandageConsumeTimeInTick;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }

    @Override
    public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {

        Text line1 = Text.literal("Heals 3 health, removes Bleeding Effect")
                .setStyle(Style.EMPTY.withItalic(true).withColor(Formatting.GRAY));

        tooltip.add(line1);

        tooltip.add(Text.literal(""));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (user instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) user;

            player.heal(3.0f);

            if(player.hasStatusEffect(EffectsManager.BLEEDING)){
                player.removeStatusEffect(EffectsManager.BLEEDING);
            }

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
