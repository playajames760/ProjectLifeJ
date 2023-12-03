package me.playajames.projectlife.common.ollama.prompts;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.leonhard.storage.Json;
import me.playajames.projectlife.common.ollama.OllamaPrompt;
import me.playajames.projectlife.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.Date;

public class LivingCharacterPrompt implements OllamaPrompt {

    String characterId;
    String instruction;
    String context;
    String appearance;
    String scenario;
    String mood;
    String messages;
    String model;

    public LivingCharacterPrompt(String characterId) {
        //parse and load json file from here into this object.
        return;
    }

    @Override
    public String generate() {
        String prompt = "Instruction: " + instruction + "\n" +
                "Your name: " + characterId + "\n" +
                "Context: " + context + "\n" +
                "Current Appearance: " + appearance + "\n" +
                "Current Scenario: " + scenario + "\n" +
                "Current Mood: " + mood + "\n" +
                "Most recent conversation: " + messages + "\n" +
                "Current Time: " + new Date();
        prompt = prompt.replace("{character}", characterId);
        return  parsePrompt(prompt);
    }

    @Override
    public String getModel() {
        return model;
    }

    private String parsePrompt(String prompt) {

    }

}
