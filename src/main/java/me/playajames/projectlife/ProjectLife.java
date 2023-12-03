package me.playajames.projectlife;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import com.google.gson.JsonArray;
import dev.jorel.commandapi.CommandAPI;
import me.playajames.projectlife.common.commands.RojiCommand;
import me.playajames.projectlife.common.ollama.OllamaCharacterManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProjectLife extends JavaPlugin {

    public static String OLLAMA_CURL;
    private static TaskChainFactory taskChainFactory;

    @Override
    public void onEnable() {
        // Plugin startup logic
        initConfig();
        taskChainFactory = BukkitTaskChainFactory.create(this);
        OLLAMA_CURL = getConfig().getString("ollama_curl");

        OllamaCharacterManager.createCharacter(
                "Roji",
                "Text transcript of a never-ending conversation between players on the server and {character}.",
                "{character} is a teenager from Alabama playing minecraft. He is currently playing in an online server called project life. {character} trying to make friends, learn how to play the server, and survive.",
                "{character} just joined this server for the first time.",
                "{character} is struggling to find a good server to play on, but may have finally found a good one, this makes him happy, and slightly excited.",
                new JsonArray(),
                "wizard-vicuna-uncensored:latest",
                5);
        registerCommands();
    }

    private void registerCommands() {
        CommandAPI.registerCommand(RojiCommand.class);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void initConfig() {
        getLogger().info("Initializing configuration...");
        saveDefaultConfig();
        getLogger().info("Configuration initialization completed successfully.");
    }

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }
    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }
}
