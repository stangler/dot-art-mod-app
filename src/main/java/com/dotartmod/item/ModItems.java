package com.dotartmod.item;

import com.dotartmod.DotArtMod;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(DotArtMod.MOD_ID);

    public static final DeferredItem<DotArtWandItem> DOT_ART_WAND =
            ITEMS.registerItem(
                "dot_art_wand",
                DotArtWandItem::new,
                new Item.Properties().stacksTo(1)
            );
}
