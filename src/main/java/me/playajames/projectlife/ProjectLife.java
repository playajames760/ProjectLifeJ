package me.playajames.projectlife;

import co.aikar.taskchain.BukkitTaskChainFactory;
import co.aikar.taskchain.TaskChain;
import co.aikar.taskchain.TaskChainFactory;
import dev.jorel.commandapi.CommandAPI;
import me.playajames.projectlife.common.commands.RojiCommand;
import me.playajames.projectlife.common.ollama.OllamaCharacterManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ProjectLife extends JavaPlugin {

    public static String OLLAMA_CURL;
    public static boolean OLLAMA_DEBUG;
    private static TaskChainFactory taskChainFactory;

    @Override
    public void onEnable() {
        initConfig();
        initTaskChain();
        createOllamaCharacters();
        registerCommands();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void initConfig() {
        getLogger().info("Initializing configuration...");
        saveDefaultConfig();
        OLLAMA_CURL = getConfig().getString("ollama_curl");
        OLLAMA_DEBUG = getConfig().getBoolean("ollama_debug");
        getLogger().info("Configuration initialization completed successfully.");
    }

    private void initTaskChain() {
        taskChainFactory = BukkitTaskChainFactory.create(this);
    }

    private void createOllamaCharacters() {
        OllamaCharacterManager.createCharacter("Roji");
    }

    private void registerCommands() {
        CommandAPI.registerCommand(RojiCommand.class);
    }

    public static <T> TaskChain<T> newChain() {
        return taskChainFactory.newChain();
    }
    public static <T> TaskChain<T> newSharedChain(String name) {
        return taskChainFactory.newSharedChain(name);
    }
}
