package com.github.ericliucn.crafteco;

import com.github.ericliucn.crafteco.eco.CraftEcoService;
import com.google.inject.Inject;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Server;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.lifecycle.*;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.jvm.Plugin;

import java.util.function.Supplier;

/**
 * The main class of your Sponge plugin.
 *
 * <p>All methods are optional -- some common event registrations are included as a jumping-off point.</p>
 */
@Plugin("crafteco")
public class Main {

    public static Main instance;

    private final PluginContainer container;
    private final Logger logger;
    private CraftEcoService craftEcoService;

    @Inject
    Main(final PluginContainer container, final Logger logger) {
        instance = this;
        this.container = container;
        this.logger = logger;
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event) {

    }

    @Listener
    public void onServerStarting(final StartingEngineEvent<Server> event) {
        craftEcoService = new CraftEcoService();
    }

    @Listener
    public void onRegisterService(final ProvideServiceEvent<EconomyService> event){
        event.suggest(() -> craftEcoService);
    }

    @Listener
    public void onServerStopping(final StoppingEngineEvent<Server> event) {

    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event) {

    }

    public void printLog(Level level, String content){
        this.logger.log(level, content);
    }

    public PluginContainer getContainer() {
        return container;
    }

    public CraftEcoService getCraftEcoService() {
        return craftEcoService;
    }
}