package com.dotartmod.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class DotArtWandItem extends Item {

    public DotArtWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            openGui();
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @OnlyIn(Dist.CLIENT)
    private void openGui() {
        var mc = net.minecraft.client.Minecraft.getInstance();

        // 既に同じ画面が開いている場合は再生成しない
        if (mc.screen instanceof com.dotartmod.client.screen.DotArtScreen) {
            return;
        }

        mc.setScreen(new com.dotartmod.client.screen.DotArtScreen());
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, TooltipContext context,
            List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal("§a右クリック§r でドットアート画面を開く"));
        tooltip.add(Component.literal("§7画像を選ぶだけで自動でブロックを設置！"));
        tooltip.add(Component.literal("§7サバイバルでも使えます"));
    }
}