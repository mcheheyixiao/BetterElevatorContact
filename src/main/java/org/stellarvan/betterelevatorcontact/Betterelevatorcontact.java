package org.stellarvan.betterelevatorcontact;

import com.simibubi.create.api.event.BlockEntityBehaviourEvent;
import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import org.stellarvan.betterelevatorcontact.content.WirelessElevatorBehaviour;
import org.stellarvan.betterelevatorcontact.network.ContactNetwork;

@Mod(Betterelevatorcontact.MODID)
public class Betterelevatorcontact {
    public static final String MODID = "betterelevatorcontact";

    // Keep compatibility with earlier Forge 47.x; newer context APIs were backported later.
    @SuppressWarnings("removal")
    public Betterelevatorcontact() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
        ContactNetwork.register();
        MinecraftForge.EVENT_BUS.addGenericListener(ElevatorContactBlockEntity.class,
                Betterelevatorcontact::attachWirelessReceiver);
    }

    private static void attachWirelessReceiver(BlockEntityBehaviourEvent<ElevatorContactBlockEntity> event) {
        event.attach(new WirelessElevatorBehaviour(event.getBlockEntity()));
    }
}
