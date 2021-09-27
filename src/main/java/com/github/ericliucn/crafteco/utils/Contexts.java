package com.github.ericliucn.crafteco.utils;

import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.event.EventContextKey;
import org.spongepowered.api.service.context.Context;

import java.util.UUID;

public class Contexts {

    public static EventContextKey<UUID> TRANSFER_PAYEE =
            EventContextKey.builder().type(UUID.class).build();

    public static EventContextKey<UUID> TRANSFER_RECEIVER =
            EventContextKey.builder().type(UUID.class).build();

}
