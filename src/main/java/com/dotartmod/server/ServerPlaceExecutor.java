package com.dotartmod.server;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayDeque;
import java.util.Queue;

public class ServerPlaceExecutor {

    private static final Queue<PlaceTask> QUEUE = new ArrayDeque<>();
    private static boolean running = false;

    public static void enqueue(ServerLevel level, Queue<Pair<BlockPos, ResourceLocation>> entries) {
        QUEUE.add(new PlaceTask(level, entries));
        if (!running) {
            running = true;
            scheduleNext(level.getServer());
        }
    }

    private static void scheduleNext(MinecraftServer server) {
        server.execute(() -> {
            PlaceTask task = QUEUE.peek();
            if (task == null) {
                running = false;
                return;
            }

            task.runStep();

            if (task.isDone()) {
                QUEUE.poll();
            }

            scheduleNext(server);
        });
    }

    private record PlaceTask(ServerLevel level, Queue<Pair<BlockPos, ResourceLocation>> entries) {

        void runStep() {
            int limit = 512;
            int placed = 0;

            while (!entries.isEmpty() && placed++ < limit) {
                Pair<BlockPos, ResourceLocation> e = entries.poll();
                ServerPlaceHandler.place(level, e.getFirst(), e.getSecond());
            }
        }

        boolean isDone() {
            return entries.isEmpty();
        }
    }
}
