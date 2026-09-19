package org.stellarvan.betterelevatorcontact.mixin;

import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.stellarvan.betterelevatorcontact.content.CamouflageBehaviour;

@Mixin(ElevatorContactBlockEntity.class)
public abstract class ElevatorContactModelDataMixin extends SmartBlockEntity {
    protected ElevatorContactModelDataMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // Forge extension method, not a mapped Minecraft method. The target inherits it from its superclass.
    @Override
    public ModelData getModelData() {
        ModelData original = super.getModelData();
        var camouflage = getBehaviour(CamouflageBehaviour.TYPE);
        if (camouflage == null || camouflage.material() == null) return original;
        return original.derive().with(CamouflageBehaviour.MATERIAL, camouflage.material()).build();
    }
}
