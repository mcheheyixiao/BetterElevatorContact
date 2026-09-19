package org.stellarvan.betterelevatorcontact.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ChunkRenderTypeSet;
import net.minecraftforge.client.model.BakedModelWrapper;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import org.stellarvan.betterelevatorcontact.content.CamouflageBehaviour;

import javax.annotation.Nullable;
import java.util.List;

/** Material models retain their face UVs, render layers and tint indices. */
public final class CamouflageModel extends BakedModelWrapper<BakedModel> {
    private static final ModelProperty<ModelData> WRAPPED_DATA = new ModelProperty<>();

    public CamouflageModel(BakedModel original) {
        super(original);
    }

    private static BakedModel model(BlockState state) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
    }

    private static ModelData wrapped(ModelData data) {
        ModelData result = data.get(WRAPPED_DATA);
        return result == null ? ModelData.EMPTY : result;
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData data) {
        BlockState material = data.get(CamouflageBehaviour.MATERIAL);
        if (material == null) return originalModel.getModelData(world, pos, state, data);
        return data.derive().with(WRAPPED_DATA,
                model(material).getModelData(world, pos, material, ModelData.EMPTY)).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
                                    ModelData data, @Nullable RenderType renderType) {
        BlockState material = data.get(CamouflageBehaviour.MATERIAL);
        if (material == null) return originalModel.getQuads(state, side, random, data, renderType);
        return model(material).getQuads(material, side, random, wrapped(data), renderType);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        BlockState material = data.get(CamouflageBehaviour.MATERIAL);
        return material == null ? originalModel.getRenderTypes(state, random, data)
                : model(material).getRenderTypes(material, random, wrapped(data));
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        BlockState material = data.get(CamouflageBehaviour.MATERIAL);
        return material == null ? originalModel.getParticleIcon(data)
                : model(material).getParticleIcon(wrapped(data));
    }

}
