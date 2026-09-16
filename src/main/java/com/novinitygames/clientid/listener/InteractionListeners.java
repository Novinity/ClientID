package com.novinitygames.clientid.listener;

import com.novinitygames.clientid.ClientID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Environment(EnvType.SERVER)
public class InteractionListeners {
    private static final Map<ServerPlayer, FrozenData> frozenPositions = new ConcurrentHashMap<>();
    private static record FrozenData(Vec3 pos, float yaw, float pitch) {}

    public static void Register() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!ClientID.accepted.contains(player)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });
        AttackBlockCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!ClientID.accepted.contains(player)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!ClientID.accepted.contains(player)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!ClientID.accepted.contains(player)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (!ClientID.accepted.contains(player)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> ClientID.accepted.contains(player));

        ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
            if (entity instanceof ServerPlayer p)
                return ClientID.accepted.contains(p);
            return true;
        });

        ServerTickEvents.END_SERVER_TICK.register((server) -> {
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                if (ClientID.accepted.contains(p)) continue;

                FrozenData data = frozenPositions.get(p);
                if (data == null) {
                    frozenPositions.put(p, new FrozenData(p.position(), p.getCamera().xRotO, p.getCamera().yRotO));
                    continue;
                }

                p.setSprinting(false);
                p.setDeltaMovement(new Vec3(0,0,0));
                p.syncVelocity = true;

                p.getCamera().setXRot(data.pitch());
                p.getCamera().setYRot(data.yaw());
                p.setYHeadRot(data.yaw());
                p.setYBodyRot(data.yaw());

                p.connection.teleport(data.pos().x, data.pos().y, data.pos().z, data.yaw(), data.pitch());
            }
        });
    }
}
