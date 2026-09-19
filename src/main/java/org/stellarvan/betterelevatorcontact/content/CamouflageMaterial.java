package org.stellarvan.betterelevatorcontact.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public final class CamouflageMaterial {
    private CamouflageMaterial() {}

    public static boolean isValid(ItemStack stack) {
        return stateFor(stack) != null;
    }

    @Nullable
    public static BlockState stateFor(ItemStack stack) {
        if (!(stack.getItem() instanceof BlockItem item)) return null;
        BlockState state = item.getBlock().defaultBlockState();
        if (state.isAir() || state.hasBlockEntity() || !state.getFluidState().isEmpty()
                || state.getRenderShape() != RenderShape.MODEL || state.is(Blocks.BARRIER)
                || state.is(Blocks.STRUCTURE_VOID) || state.getBlock() instanceof LeavesBlock)
            return null;
        try {
            var world = EmptyBlockGetter.INSTANCE;
            var pos = BlockPos.ZERO;
            return state.isSolidRender(world, pos)
                    && Block.isShapeFullBlock(state.getShape(world, pos))
                    && Block.isShapeFullBlock(state.getCollisionShape(world, pos)) ? state : null;
        } catch (RuntimeException exception) {
            // Some mod blocks require a live world even for their default shape. They are not supported materials.
            return null;
        }
    }
}
