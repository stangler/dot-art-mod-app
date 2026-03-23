package com.dotartmod;

import com.dotartmod.item.ModItems;
import com.dotartmod.network.ModNetwork;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(DotArtMod.MOD_ID)
public class DotArtMod {

    public static final String MOD_ID = "dotartmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DotArtMod(IEventBus modEventBus, ModContainer modContainer) {
        ModItems.ITEMS.register(modEventBus);
        ModNetwork.register(modEventBus);
        LOGGER.info("[DotArt Mod] 起動しました！");
    }
}
