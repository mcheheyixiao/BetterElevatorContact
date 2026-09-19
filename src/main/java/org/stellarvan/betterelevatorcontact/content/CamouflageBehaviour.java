package org.stellarvan.betterelevatorcontact.content;

import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;

public final class CamouflageBehaviour extends BlockEntityBehaviour {
    public static final BehaviourType<CamouflageBehaviour> TYPE = new BehaviourType<>();
    public static final ModelProperty<BlockState> MATERIAL = new ModelProperty<>();
    private static final String KEY = "BetterElevatorCamouflage";
    @Nullable private BlockState material;

    public CamouflageBehaviour(ElevatorContactBlockEntity contact) {
        super(contact);
    }

    @Nullable
    public BlockState material() {
        return material;
    }

    public ItemStack materialItem() {
        return material == null ? ItemStack.EMPTY : new ItemStack(material.getBlock());
    }

    public void setMaterial(ItemStack stack) {
        BlockState next = CamouflageMaterial.stateFor(stack);
        if (material == next) return;
        material = next;
        blockEntity.setChanged();
        redraw();
    }

    private void redraw() {
        if (getWorld() == null || !getWorld().isClientSide) return;
        blockEntity.requestModelDataUpdate();
        var state = blockEntity.getBlockState();
        getWorld().sendBlockUpdated(getPos(), state, state, 16);
    }

    @Override
    public void write(CompoundTag tag, boolean clientPacket) {
        // Store only the material item ID/count, never arbitrary item or block entity NBT.
        tag.put(KEY, materialItem().save(new CompoundTag()));
    }

    @Override
    public void read(CompoundTag tag, boolean clientPacket) {
        BlockState before = material;
        material = CamouflageMaterial.stateFor(ItemStack.of(tag.getCompound(KEY)));
        if (before != material) redraw();
    }

    @Override
    public void initialize() {
        redraw();
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
