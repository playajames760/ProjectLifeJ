package me.playajames.projectlife.common.ollama;
import de.leonhard.storage.Json;

import java.util.concurrent.ConcurrentHashMap;

public class OllamaCharacterManager {

    private static ConcurrentHashMap<String, OllamaCharacter> characterMap = new ConcurrentHashMap<>();

    /*
     * Creates a new OllamaCharacter instance and adds it to the conversations map.
     *
     * @param characterId The unique characterId to identify this character.
     * @param instruction The initial instruction/prompt for the character.
     * @param context Any additional context to provide to the character.
     * @param messages A JsonObject containing any previous messages with the character.
     * @return The newly created OllamaCharacter instance.
     */
    public static OllamaCharacter createCharacter(String characterId, int generateEmotionCountMax, int messageHistoryCount) {
        OllamaCharacter character = new OllamaCharacter(characterId, generateEmotionCountMax, messageHistoryCount);
        characterMap.put(characterId, character);
        return character;
    }

    /*
     * Removes the character with the given characterId from the conversations map.
     *
     * @param characterId The characterId of the character to destroy.
     */
    public static void destroyCharacter(String characterId) {
        if (characterMap.containsKey(characterId)) {
            characterMap.remove(characterId);
        }
    }

    /*
     * Gets the OllamaCharacter instance for the given characterId.
     *
     * @param characterId The characterId of the character to retrieve.
     * @return The OllamaCharacter instance, or null if not found.
     */
    public static OllamaCharacter getCharacter(String characterId) {
            return characterMap.get(characterId);
    }

}
