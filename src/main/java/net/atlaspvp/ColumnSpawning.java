package net.atlaspvp;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayDeque;
import java.util.Queue;

public class ColumnSpawning {

    private final Long2ObjectMap<Queue<Entity>> queuedSpawns = new Long2ObjectOpenHashMap<>();

    public void queueSpawn(Entity entity) {
        int floorX = entity.getBlockX();
        int floorZ = entity.getBlockZ();
        long key = (((long) floorX) << 32) | (floorZ & 0xffffffffL);

        Queue<Entity> queue = queuedSpawns.get(key);
        if (queue == null) {
            queue = new ArrayDeque<>();
            queuedSpawns.put(key, queue);
        }
        queue.add(entity);
    }

    public void dispatch() {
        for (Queue<Entity> queue : queuedSpawns.values()) {
            for (Entity entity; (entity = queue.poll()) != null; ) {
                entity.level.addFreshEntity(entity);
                entity.level.playSound((Player) null, entity.xo, entity.yo, entity.zo, SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
        queuedSpawns.clear();
    }
}
