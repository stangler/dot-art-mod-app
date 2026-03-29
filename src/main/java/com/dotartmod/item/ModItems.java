package com.dotartmod.item;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredHolder<Item, Item> DOT_ART_WAND = DeferredRegister.createItems("dotartmod")
            .register("dot_art_wand", () -> new DotArtWandItem(new Item.Properties().stacksTo(1)));
}