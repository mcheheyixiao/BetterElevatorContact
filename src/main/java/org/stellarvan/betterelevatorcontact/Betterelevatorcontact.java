package org.stellarvan.betterelevatorcontact;

import com.simibubi.create.api.event.BlockEntityBehaviourEvent;
import com.simibubi.create.content.contraptions.elevator.ElevatorContactBlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.stellarvan.betterelevatorcontact.content.WirelessElevatorBehaviour;
import org.stellarvan.betterelevatorcontact.network.ContactNetwork;

@Mod(Betterelevatorcontact.MODID)
public class Betterelevatorcontact {
    public static final String MODID = "betterelevatorcontact";

    public Betterelevatorcontact() {
        ContactNetwork.register();
        MinecraftForge.EVENT_BUS.addGenericListener(ElevatorContactBlockEntity.class,
                Betterelevatorcontact::attachWirelessReceiver);
    }

    private static void attachWirelessReceiver(BlockEntityBehaviourEvent<ElevatorContactBlockEntity> event) {
        event.attach(new WirelessElevatorBehaviour(event.getBlockEntity()));
    }
}
