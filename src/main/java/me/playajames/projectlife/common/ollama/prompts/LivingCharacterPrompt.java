package me.playajames.projectlife.common.ollama.prompts;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.leonhard.storage.Json;
import de.leonhard.storage.Yaml;
import me.playajames.projectlife.ProjectLife;
import me.playajames.projectlife.common.ollama.OllamaPrompt;
import me.playajames.projectlife.utils.StringUtils;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

import static me.playajames.projectlife.ProjectLife.OLLAMA_DEBUG;

public class LivingCharacterPrompt implements OllamaPrompt {

    String characterId;
    String instruction;
    String character;
    String scenario;
    String location;
    String appearance;
    String mood;
    List<String> objectives;
    List<String> memory;
    List<String> messages;
    String model;
    int generateEmotionCountMax;
    int messageHistoryCount;

    public LivingCharacterPrompt(String characterId) {
        Json characterJson = new Json("characters", ProjectLife.getPlugin(ProjectLife.class).getDataFolder().toString());

        // Load data, or generate default data and populate class variables.
        this.characterId = characterId;
        this.instruction = characterJson.getOrSetDefault("characters." + characterId + ".instruction", "Text transcript of a never-ending conversation between players on the server and {character}.");
        this.character = characterJson.getOrSetDefault("characters." + characterId + ".context", "{character} is a teenager from Alabama playing minecraft. He is currently playing in an online server called project life. {character} trying to make friends, learn how to play the server, and survive.");
        this.appearance = characterJson.getOrSetDefault("characters." + characterId + ".appearance", "Short, slim, handsome");
        this.scenario = characterJson.getOrSetDefault("characters." + characterId + ".scenario", "{character} just joined this server for the first time.");
        this.location = characterJson.getOrSetDefault("characters." + characterId + ".location", "Spawn area of the minecraft server.");
        this.mood = characterJson.getOrSetDefault("characters." + characterId + ".mood", "{character} is struggling to find a good server to play on, but may have finally found a good one, this makes him happy, and slightly excited.");
        this.objectives = characterJson.getOrSetDefault("characters." + characterId + ".objectives", new ArrayList<>());
        this.memory = characterJson.getOrSetDefault("characters." + characterId + ".memory", new ArrayList<>());
        this.messages = characterJson.getOrSetDefault("characters." + characterId + ".messages", new ArrayList<>()); //todo implement into saved file, currently no message context is persistent through server restarts
        this.model = characterJson.getOrSetDefault("characters." + characterId + ".model", "wizard-vicuna-uncensored:latest");
        this.generateEmotionCountMax = characterJson.getOrSetDefault("characters." + characterId + ".generateEmotionCountMax", 10);
        this.messageHistoryCount = characterJson.getOrSetDefault("characters." + characterId + ".messageHistoryCount", 25 );
    }

    @Override
    public String generateRaw() {
        Yaml chatInstruction = new Yaml("chat_instruction", ProjectLife.getPlugin(ProjectLife.class).getDataFolder().toString());
        String prompt = chatInstruction.getString("content");

        while (prompt.contains("{instruction}"))
            prompt = prompt.replace("{instruction}", instruction);

        while (prompt.contains("{character}"))
            prompt = prompt.replace("{character}", character);

        while (prompt.contains("{scenario}"))
            prompt = prompt.replace("{scenario}", scenario);

        while (prompt.contains("{location}"))
            prompt = prompt.replace("{location}", location);

        while (prompt.contains("{mood}"))
            prompt = prompt.replace("{mood}", mood);

        while (prompt.contains("{objectives}"))
            prompt = prompt.replace("{objectives}", objectives.toString());

        while (prompt.contains("{memory}"))
            prompt = prompt.replace("{memory}", memory.toString());

        while (prompt.contains("{messages}"))
            prompt = prompt.replace("{messages}", messages.toString());

        while (prompt.contains("["))
            prompt = prompt.replace("[", "");

        while (prompt.contains("]"))
            prompt = prompt.replace("]", "");

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
        saveData();
    }

    @Override
    public void trimMessages() {
        while (messages.stream().count() > messageHistoryCount)
            messages.remove(0);
        saveData();
    }

    @Override
    public void setScenario(String scenario) {
        this.scenario = scenario;
        saveData();
        if (OLLAMA_DEBUG)
            ProjectLife.getPlugin(ProjectLife.class).getLogger().info(characterId + " scenario has been updated. (" + scenario + ")");
    }

    @Override
    public void setAppearance(String appearance) {
        this.appearance = appearance;
        saveData();
        if (OLLAMA_DEBUG)
            ProjectLife.getPlugin(ProjectLife.class).getLogger().info(characterId + " appearance has been updated. (" + appearance + ")");
    }

    @Override
    public void setMood(String mood) {
        this.mood = mood;
        saveData();
        if (OLLAMA_DEBUG)
            ProjectLife.getPlugin(ProjectLife.class).getLogger().info(characterId + " mood has been updated. (" + mood + ")");
    }

    @Override
    public int getGenerateEmotionCountMax() {
        return generateEmotionCountMax;
    }

    @Override
    public String generateRequestPrompt(String from, String message) {
        if (OLLAMA_DEBUG)
            ProjectLife.getPlugin(ProjectLife.class).getLogger().info("Generating request prompt for " + characterId + "...");
        String prompt = generateRaw();
        while (prompt.contains("{characterId}"))
            prompt = prompt.replace("{characterId}", characterId);
        while (prompt.contains("{sender}"))
            prompt = prompt.replace("{sender}", from);
        while (prompt.contains("{input}"))
            prompt = prompt.replace("{input}", message);
        if (OLLAMA_DEBUG)
            ProjectLife.getPlugin(ProjectLife.class).getLogger().info("Generated request prompt for " + characterId + ": \n" + prompt);
        return prompt;
    }

    //todo Bug The LLM will generate a response from Roji and usually continue to generate a response from the sender as well. We can trim out the generated response from the sender as long as Roji generates a response from the sender only, if the sender asks Roji to call them something else, we dont know which name to trim the rest of the response from. Example talking to Roji from the console, I told Roji its playajames talking to you from the console. Roji's parsed response was "Hi there, I'm Roji from Alabama.Playajames: Welcome to the server! Do you need any help?", The playajames: part of the response was returned with Roji's response because we only know that CONSOLE is sender and we tried to trim out the generated section for CONSOLE: but Roji reffered to CONSOLE: as Playajames: therefore it was not trimmed off the response.
    @Override
    public String parseResponse(String from, String rawResponse) {
        if (OLLAMA_DEBUG)
            ProjectLife.getPlugin(ProjectLife.class).getLogger().info("Parsing raw response from " + characterId + ":\n" + rawResponse);

        // Parse raw response to JSON
        JsonObject jsonResponseObject = JsonParser.parseString(rawResponse).getAsJsonObject();

        // Trim response
        String parsedResponse = StringUtils.trimStringAfterChar(jsonResponseObject.get("response").getAsString(), from + ":");

        // Replace character id
        if (parsedResponse.contains(characterId + ":"))
            parsedResponse = parsedResponse.replace(characterId + ":", "");

        // Replace character variable
        if (parsedResponse.contains("\\\\{\\" + characterId + "*\\\\}"))
            parsedResponse = parsedResponse.replace("\\\\{\\" + characterId + "*\\\\}", "");

        // Replace user variable
        if (parsedResponse.contains("\\\\{\\user*\\\\}"))
            parsedResponse = parsedResponse.replace("\\\\{\\user*\\\\}", from);

        while (parsedResponse.contains("["))
            parsedResponse = parsedResponse.replace("[", "");

        while (parsedResponse.contains("]"))
            parsedResponse = parsedResponse.replace("]", "");

        while (parsedResponse.contains(":"))
            parsedResponse = parsedResponse.replace(":", "");

        if (parsedResponse.contains("\n"))
            parsedResponse = parsedResponse.replace("\n", "");

        if (parsedResponse.contains("Response"))
            parsedResponse = parsedResponse.replace("Response", "");

        while (parsedResponse.contains("#"))
            parsedResponse = parsedResponse.replace("#", "");

        //parsedResponse = parsedResponse.replaceFirst(" ", ""); //todo This just replaces the first space in the string, need to only do this when there is spaces at the beginning of the string.

        if (OLLAMA_DEBUG)
            ProjectLife.getPlugin(ProjectLife.class).getLogger().info("Parsed raw response from " + characterId + ": " + parsedResponse);

        return parsedResponse;
    }

    private void saveData() {
        Json characterJson = new Json("characters", ProjectLife.getPlugin(ProjectLife.class).getDataFolder().toString());
        characterJson.set("characters." + characterId + ".instruction", instruction);
        characterJson.set("characters." + characterId + ".context", character);
        characterJson.set("characters." + characterId + ".appearance", appearance);
        characterJson.set("characters." + characterId + ".scenario", scenario);
        characterJson.set("characters." + characterId + ".mood", mood);
        characterJson.set("characters." + characterId + ".messages", messages);
        characterJson.set("characters." + characterId + ".model", model);
    }

}
