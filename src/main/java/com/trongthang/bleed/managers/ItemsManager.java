package com.trongthang.bleed.managers;

import com.trongthang.bleed.Bleed;
import com.trongthang.bleed.items.Bandage;
import com.trongthang.bleed.items.HealingNeedle;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ItemsManager {
    public static final Item HEALING_NEEDLE = new HealingNeedle(new Item.Settings());
    public static final Item BANDAGE = new Bandage(new Item.Settings());

    public static void register() {
        Registry.register(Registries.ITEM, new Identifier(Bleed.MOD_ID, "healing_needle"), HEALING_NEEDLE);
        Registry.register(Registries.ITEM, new Identifier(Bleed.MOD_ID, "bandage"), BANDAGE);
    }

    public static void addingItemToTab(){
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK).register(entries -> {
            entries.add(HEALING_NEEDLE);
            entries.add(BANDAGE);
        });
    }
}
