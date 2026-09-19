package org.stellarvan.betterelevatorcontact.content;

import com.simibubi.create.content.contraptions.elevator.ElevatorColumn;
import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlock;
import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import com.simibubi.create.content.redstone.link.LinkBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

/** Own behaviour type keeps Create's in-world link renderer/interaction away from the contact. */
public final class WirelessElevatorBehaviour extends BlockEntityBehaviour {
    public static final BehaviourType<WirelessElevatorBehaviour> TYPE = new BehaviourType<>();
    private static final String NBT_KEY = "BetterElevatorContact";
    private final ElevatorContactBlockEntity contact;
    private final LinkBehaviour receiver;
    private boolean initialized;
    private boolean registered;
    private boolean powered;
    private boolean pendingCall;

    public WirelessElevatorBehaviour(ElevatorContactBlockEntity contact) {
        super(contact);
        this.contact = contact;
        // This delegate is never exposed under LinkBehaviour.TYPE and has no world-space slots.
        receiver = LinkBehaviour.receiver(contact, Pair.of(null, null), this::receive);
    }

    public ItemStack frequency(boolean first) {
        return receiver.getNetworkKey().get(first).getStack().copy();
    }

    private boolean enabled() {
        return !frequency(true).isEmpty() || !frequency(false).isEmpty();
    }

    private void receive(int strength) {
        boolean nowPowered = strength > 0;
        if (nowPowered && !powered)
            pendingCall = true;
        powered = nowPowered;
    }

    @Override
    public void initialize() {
        initialized = true;
        connect();
    }

    private void connect() {
        if (initialized && !getWorld().isClientSide && enabled() && !registered) {
            registered = true;
            receiver.initialize();
        }
    }

    private void disconnect() {
        if (registered) {
            registered = false;
            receiver.unload();
        }
        powered = false;
        pendingCall = false;
    }

    /** Remove once, replace both frequencies, then join once: never listen to an intermediate key. */
    public void setFrequencies(ItemStack first, ItemStack second) {
        if (ItemStack.isSameItemSameTags(first, frequency(true))
                && ItemStack.isSameItemSameTags(second, frequency(false)))
            return;
        disconnect();
        CompoundTag data = new CompoundTag();
        data.put("FrequencyFirst", single(first).save(new CompoundTag()));
        data.put("FrequencyLast", single(second).save(new CompoundTag()));
        receiver.read(data, false);
        connect();
        contact.setChanged();
        contact.sendData();
    }

    private static ItemStack single(ItemStack stack) {
        ItemStack copy = stack.copy();
        if (!copy.isEmpty())
            copy.setCount(1);
        return copy;
    }

    @Override
    public void tick() {
        if (getWorld().isClientSide || !pendingCall || contact.columnCoords == null)
            return;
        pendingCall = false;
        var state = contact.getBlockState();
        if (!(state.getBlock() instanceof ElevatorContactBlock block)
                || state.getValue(ElevatorContactBlock.CALLING)
                || state.getValue(ElevatorContactBlock.POWERED))
            return;
        var column = ElevatorColumn.getOrCreate(getWorld(), contact.columnCoords);
        // Wireless input must not latch the block's physical redstone POWERED property.
        block.callToContactAndUpdate(column, state, getWorld(), getPos(), false);
    }

    @Override
    public void unload() {
        disconnect();
        initialized = false;
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket) {
        CompoundTag data = new CompoundTag();
        receiver.write(data, clientPacket);
        tag.put(NBT_KEY, data);
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket) {
        disconnect();
        receiver.read(tag.getCompound(NBT_KEY), clientPacket);
        connect();
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
