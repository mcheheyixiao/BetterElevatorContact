package org.stellarvan.betterelevatorcontact.client;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.stellarvan.betterelevatorcontact.Betterelevatorcontact;
import org.stellarvan.betterelevatorcontact.content.CamouflageBehaviour;

@Mod.EventBusSubscriber(modid = Betterelevatorcontact.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CamouflageClient {
    private CamouflageClient() {}

    @SubscribeEvent
    public static void models(ModelEvent.ModifyBakingResult event) {
        for (var state : AllBlocks.ELEVATOR_CONTACT.get().getStateDefinition().getPossibleStates()) {
            var key = BlockModelShaper.stateToModelLocation(state);
            var original = event.getModels().get(key);
            if (original != null && !(original instanceof CamouflageModel))
                event.getModels().put(key, new CamouflageModel(original));
        }
    }

    @SubscribeEvent
    public static void colors(RegisterColorHandlersEvent.Block event) {
        event.register((state, world, pos, tint) -> {
            if (world != null && pos != null
                    && world.getBlockEntity(pos) instanceof ElevatorContactBlockEntity contact) {
                var camouflage = contact.getBehaviour(CamouflageBehaviour.TYPE);
                if (camouflage != null && camouflage.material() != null)
                    return event.getBlockColors().getColor(camouflage.material(), world, pos, tint);
            }
            return -1;
        }, AllBlocks.ELEVATOR_CONTACT.get());
    }
}
