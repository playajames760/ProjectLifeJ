package me.playajames.projectlife.common.ollama;

import me.playajames.projectlife.ProjectLife;
import org.bukkit.Bukkit;

import java.util.concurrent.ConcurrentHashMap;

public class OllamaCharacterManager {

    private static ConcurrentHashMap<String, OllamaCharacter> characterMap = new ConcurrentHashMap<>();

    /**
     * Creates a new OllamaCharacter instance and adds it to the conversations map.
     *
     * @param characterId The unique characterId to identify this character, must match character in characters.yml.
     * @return The newly created OllamaCharacter instance.
     */
    public static OllamaCharacter createCharacter(String characterId) {
        ProjectLife.getPlugin(ProjectLife.class).getLogger().info("Creating ollama character('" + characterId + "')...");
        OllamaCharacter character = new OllamaCharacter(characterId);
        characterMap.put(characterId, character);
        ProjectLife.getPlugin(ProjectLife.class).getLogger().info("Created character('" + characterId + "') successfully.");
        return character;
    }

    /**
     * Removes the character with the given characterId from the conversations map.
     *
     * @param characterId The characterId of the character to destroy.
     */
    public static void destroyCharacter(String characterId) {
        if (characterMap.containsKey(characterId)) {
            characterMap.remove(characterId);
        }
    }

    /**
     * Gets the OllamaCharacter instance for the given characterId.
     *
     * @param characterId The characterId of the character to retrieve.
     * @return The OllamaCharacter instance, or null if not found.
     */
    public static OllamaCharacter getCharacter(String characterId) {
            return characterMap.get(characterId);
    }

}
