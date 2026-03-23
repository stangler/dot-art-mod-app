package com.dotartmod.client;

import com.dotartmod.network.PlaceBlocksPacket;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class ClientPlaceQueue {

    private final Queue<PlaceBlocksPacket> queue = new ArrayDeque<>();
    private long lastSendTime = 0L;
    private final long intervalMs;

    public ClientPlaceQueue() {
        this(50);
    }

    public ClientPlaceQueue(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    public void enqueue(List<PlaceBlocksPacket> packets) {
        queue.addAll(packets);
    }

    public void tick() {
        if (queue.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastSendTime < intervalMs) {
            return;
        }

        PlaceBlocksPacket packet = queue.poll();
        if (packet != null) {
            PacketDistributor.sendToServer(packet);
            lastSendTime = now;
        }
    }

    public boolean isFinished() {
        return queue.isEmpty();
    }

    public void cancel() {
        queue.clear();
    }
}
