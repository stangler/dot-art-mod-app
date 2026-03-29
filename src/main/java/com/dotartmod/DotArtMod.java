package com.dotartmod;

import com.dotartmod.item.DotArtWandItem;
import com.dotartmod.network.ModNetwork;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod("dotartmod")
public class DotArtMod {

    public static final String MODID = "dotartmod";

    private static final DeferredRegister<Item> ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredHolder<Item, Item> DOT_ART_WAND = ITEMS.register("dot_art_wand",
            () -> new DotArtWandItem(new Item.Properties().stacksTo(1)));

    public DotArtMod(IEventBus modEventBus) {
        ITEMS.register(modEventBus);

        // ネットワーク登録イベントを購読
        modEventBus.addListener(this::registerNetwork);
    }

    private void registerNetwork(final RegisterPayloadHandlersEvent event) {
        ModNetwork.register(event);
    }
}