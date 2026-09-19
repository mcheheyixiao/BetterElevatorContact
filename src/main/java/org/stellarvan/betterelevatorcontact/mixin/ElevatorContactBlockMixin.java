package org.stellarvan.betterelevatorcontact.mixin;

import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlock;
import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.stellarvan.betterelevatorcontact.client.WirelessContactScreen;

@Mixin(ElevatorContactBlock.class)
public abstract class ElevatorContactBlockMixin {
    @Inject(method = "displayScreen(Lcom/simibubi/create/content/contraptions/elevator/ElevatorContactBlockEntity;Lnet/minecraft/world/entity/player/Player;)V",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void betterelevatorcontact$openScreen(ElevatorContactBlockEntity contact, Player player, CallbackInfo ci) {
        if (player instanceof LocalPlayer) {
            Minecraft.getInstance().setScreen(new WirelessContactScreen(contact));
            ci.cancel();
        }
    }
}
