package me.playajames.projectlife.common.ollama.prompts;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.leonhard.storage.Json;
import me.playajames.projectlife.ProjectLife;
import me.playajames.projectlife.common.ollama.OllamaPrompt;
import me.playajames.projectlife.utils.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static me.playajames.projectlife.ProjectLife.OLLAMA_DEBUG;

public class LivingCharacterPrompt implements OllamaPrompt {

    String characterId;
    String instruction;
    String context;
    String appearance;
    String scenario;
    String mood;
    List<String> messages;
    String model;
    int generateEmotionCounter;
    int generateEmotionCountMax;
    int messageHistoryCount;

    public LivingCharacterPrompt(String characterId) {
        Json characterJson = new Json("characters", ProjectLife.getPlugin(ProjectLife.class).getDataFolder().toString());

        // Load data, or generate default data and populate class variables.
        this.characterId = characterId;
        this.instruction = characterJson.getOrSetDefault("characters." + characterId + ".instruction", "Text transcript of a never-ending conversation between players on the server and {character}.");
        this.context = characterJson.getOrSetDefault("characters." + characterId + ".context", "{character} is a teenager from Alabama playing minecraft. He is currently playing in an online server called project life. {character} trying to make friends, learn how to play the server, and survive.");
        this.appearance = characterJson.getOrSetDefault("characters." + characterId + ".appearance", "Short, slim, handsome");
        this.scenario = characterJson.getOrSetDefault("characters." + characterId + ".scenario", "{character} just joined this server for the first time.");
        this.mood = characterJson.getOrSetDefault("characters." + characterId + ".mood", "{character} is struggling to find a good server to play on, but may have finally found a good one, this makes him happy, and slightly excited.");
        this.messages = new ArrayList<>(); //todo implement into saved file, currently no message context is persistent through server restarts
        this.model = characterJson.getOrSetDefault("characters." + characterId + ".model", "wizard-vicuna-uncensored:latest");
        this.generateEmotionCountMax = characterJson.getOrSetDefault("characters." + characterId + ".generateEmotionCountMax", 10);
        this.messageHistoryCount = characterJson.getOrSetDefault("characters." + characterId + ".messageHistoryCount", 25 );
    }

    @Override
    public String generateRaw() {
        if (OLLAMA_DEBUG)
            ProjectLife.getPlugin(ProjectLife.class).getLogger().info("Generating raw prompt for " + characterId + "...");
        String prompt = "Instruction: " + instruction + "\n" +
                "Your name: " + characterId + "\n" +
                "Context: " + context + "\n" +
                "Current Appearance: " + appearance + "\n" +
                "Current Scenario: " + scenario + "\n" +
                "Current Mood: " + mood + "\n" +
                "Most recent conversation: " + (messages.isEmpty() ? "You have no conversation memory yet." : messages) + "\n" +
                "Current Time: " + new Date();
        if (OLLAMA_DEBUG)
            ProjectLife.getPlugin(ProjectLife.class).getLogger().info("Generated raw prompt for " + characterId + ": \n" + prompt);
        return  prompt;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public String getCharacterId() {
        return characterId;
    }

    @Override
    public List<String> getMessages() {
        return messages;
    }

    @Override
    public void addMessage(String message) {
        messages.add(message);
    }

    @Override
    public void trimMessages() {
        while (messages.stream().count() > messageHistoryCount)
            messages.remove(0);
    }

    public int getGenerateEmotionCounter() {
        return generateEmotionCounter;
    }

    public int getGenerateEmotionCountMax() {
        return generateEmotionCountMax;
    }

    public int getMessageHistoryCount() {
        return messageHistoryCount;
    }

    @Override
    public String generateRequestPrompt(String from, String message) {
        if (OLLAMA_DEBUG)
            ProjectLife.getPlugin(ProjectLife.class).getLogger().info("Generating request prompt for " + characterId + "...");
        String prompt = generateRaw();
        if (prompt.contains("{character}"))
            prompt = prompt.replace("{character}", characterId);
        prompt = prompt + "\n" + from + ": " + message;
        if (OLLAMA_DEBUG)
            ProjectLife.getPlugin(ProjectLife.class).getLogger().info("Generated request prompt for " + characterId + ": \n" + prompt);
        return prompt;
    }

    //todo Bug The LLM will generate a response from Roji and usually continue to generate a response from the sender as well. We can trim out the generated response from the sender as long as Roji generates a response from the sender only, if the sender asks Roji to call them something else, we dont know which name to trim the rest of the response from. Example talking to Roji from the console, I told Roji its playajames talking to you from the console. Roji's parsed response was "Hi there, I'm Roji from Alabama.Playajames: Welcome to the server! Do you need any help?", The playajames: part of the response was returned with Roji's response because we only know that CONSOLE is sender and we tried to trim out the generated section for CONSOLE: but Roji reffered to CONSOLE: as Playajames: therefore it was not trimmed off the response.
    @Override
    public String parseResponse(String from, String rawResponse) {
        if (OLLAMA_DEBUG)
            ProjectLife.getPlugin(ProjectLife.class).getLogger().info("Parsing raw response from " + characterId + "...\n" + rawResponse);

        // Parse raw response to JSON
        JsonObject jsonResponseObject = JsonParser.parseString(rawResponse).getAsJsonObject();

        // Trim response
        String parsedResponse = StringUtils.trimStringAfterChar(jsonResponseObject.get("response").getAsString(), from + ":");
        if (OLLAMA_DEBUG)
            ProjectLife.getPlugin(ProjectLife.class).getLogger().info("Parsed raw response from " + characterId + "...\n" + parsedResponse);

        // Replace character id
        if (parsedResponse.contains(characterId + ":")) {
            parsedResponse = parsedResponse.replace(characterId + ":", "");
        }

        // Replace character variable
        if (parsedResponse.contains("\\\\{\\" + characterId + "*\\\\}")) {
            parsedResponse = parsedResponse.replace("\\\\{\\" + characterId + "*\\\\}", "");
        }

        // Replace user variable
        if (parsedResponse.contains("\\\\{\\user*\\\\}")) {
            parsedResponse = parsedResponse.replace("\\\\{\\user*\\\\}", from);
        }

        // More replacements
        parsedResponse = parsedResponse.replace("\\\\{\\" + characterId + "*\\\\}:", "");
        parsedResponse = parsedResponse.replace("\n", "");
        parsedResponse = parsedResponse.replaceFirst(" ", "");

        return parsedResponse;
    }

}
