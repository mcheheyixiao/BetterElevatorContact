package org.stellarvan.betterelevatorcontact.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.stellarvan.betterelevatorcontact.Betterelevatorcontact;
import java.util.Objects;

public final class ContactNetwork {
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            Objects.requireNonNull(ResourceLocation.tryBuild(Betterelevatorcontact.MODID, "main")),
            () -> "2", "2"::equals, "2"::equals);

    private ContactNetwork() {}

    public static void register() {
        CHANNEL.messageBuilder(ConfigureContactPacket.class, 0, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ConfigureContactPacket::encode)
                .decoder(ConfigureContactPacket::decode)
                .consumerMainThread(ConfigureContactPacket::handle)
                .add();
    }
}
