package org.stellarvan.betterelevatorcontact.network;

import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import com.simibubi.create.content.decoration.slidingDoor.DoorControl;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import org.stellarvan.betterelevatorcontact.content.WirelessElevatorBehaviour;

import java.util.function.Supplier;

/** Frequency choices: -2 keeps the stored item, -1 clears, 0..35 copies a player inventory slot. */
public record ConfigureContactPacket(BlockPos pos, String shortName, String longName, int doorMode,
                                     int firstSlot, int secondSlot) {
    public void encode(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeUtf(shortName, 4);
        buffer.writeUtf(longName, 90);
        buffer.writeVarInt(doorMode);
        buffer.writeInt(firstSlot);
        buffer.writeInt(secondSlot);
    }

    public static ConfigureContactPacket decode(FriendlyByteBuf buffer) {
        return new ConfigureContactPacket(buffer.readBlockPos(), buffer.readUtf(4), buffer.readUtf(90),
                buffer.readVarInt(), buffer.readInt(), buffer.readInt());
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        var context = supplier.get();
        context.setPacketHandled(true);
        ServerPlayer player = context.getSender();
        if (player == null || player.isSpectator() || !player.mayBuild()
                || doorMode < 0 || doorMode >= DoorControl.values().length
                || !validSlot(firstSlot) || !validSlot(secondSlot))
            return;
        var level = player.serverLevel();
        if (!level.hasChunkAt(pos) || !level.mayInteract(player, pos)
                || !(level.getBlockEntity(pos) instanceof ElevatorContactBlockEntity contact)
                || !contact.canPlayerUse(player) || contact.columnCoords == null)
            return;
        var wireless = contact.getBehaviour(WirelessElevatorBehaviour.TYPE);
        if (wireless == null)
            return;
        ItemStack first = resolve(player, firstSlot, wireless.frequency(true));
        ItemStack second = resolve(player, secondSlot, wireless.frequency(false));
        contact.updateName(shortName, longName);
        contact.doorControls.set(DoorControl.values()[doorMode]);
        wireless.setFrequencies(first, second);
        contact.notifyUpdate();
    }

    private static boolean validSlot(int slot) {
        return slot >= -2 && slot < 36;
    }

    private static ItemStack resolve(ServerPlayer player, int slot, ItemStack previous) {
        if (slot == -2)
            return previous;
        if (slot == -1)
            return ItemStack.EMPTY;
        ItemStack copy = player.getInventory().getItem(slot).copy();
        if (!copy.isEmpty())
            copy.setCount(1);
        return copy;
    }
}
