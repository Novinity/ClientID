package com.novinitygames.clientid.mixin;

import com.novinitygames.clientid.ClientID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class PreventDropMixin {
    @Inject(method = "drop(Z)V",
            at = @At("HEAD"),
            cancellable = true)
    private void dropItem(boolean all, CallbackInfo ci) {
        ServerPlayer self = (ServerPlayer)(Object)this;
        if (!ClientID.accepted.contains(self)) {
            ci.cancel();
        }
    }
}
