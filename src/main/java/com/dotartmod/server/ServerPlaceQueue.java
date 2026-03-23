package com.dotartmod.server;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayDeque;
import java.util.Queue;

public class ServerPlaceQueue {

    private static final Queue<PlaceTask> QUEUE = new ArrayDeque<>();

    public static void enqueue(ServerLevel level, Queue<Pair<BlockPos, ResourceLocation>> entries) {
        QUEUE.add(new PlaceTask(level, entries));
    }

    public static void tick() {
        PlaceTask task = QUEUE.peek();
        if (task != null && task.tick()) {
            QUEUE.poll();
        }
    }

    private record PlaceTask(ServerLevel level, Queue<Pair<BlockPos, ResourceLocation>> entries) {

        boolean tick() {
            int limit = 512;
            int placed = 0;

            while (!entries.isEmpty() && placed++ < limit) {
                Pair<BlockPos, ResourceLocation> e = entries.poll();
                ServerPlaceHandler.place(level, e.getFirst(), e.getSecond());
            }
            return entries.isEmpty();
        }
    }
}
