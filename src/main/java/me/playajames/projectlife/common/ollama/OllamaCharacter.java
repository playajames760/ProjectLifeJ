package me.playajames.projectlife.common.ollama;

import io.github.amithkoujalgi.ollama4j.core.exceptions.OllamaBaseException;
import me.playajames.projectlife.common.ollama.prompts.LivingCharacterPrompt;
import me.playajames.projectlife.utils.PlOllamaAPI;

import java.io.IOException;

import static me.playajames.projectlife.ProjectLife.OLLAMA_CURL;

public class OllamaCharacter {

    public OllamaPrompt ollamaPrompt;
    private int generateEmotionCounter = 0;
    private int generateEmotionCountMax;
    private int messageHistoryCount;

    public OllamaCharacter(String characterId) {
        this.ollamaPrompt = new LivingCharacterPrompt(characterId);
        this.generateEmotionCountMax = generateEmotionCountMax;
        this.messageHistoryCount = messageHistoryCount;
    }

    public String chat(String from, String message) throws OllamaBaseException, IOException, InterruptedException {
        PlOllamaAPI ollamaAPI = new PlOllamaAPI(OLLAMA_CURL);

        // Parse and validate request
        String request = ollamaPrompt.generateRequestPrompt(from, message);
        if (request.equalsIgnoreCase("null") || request == null || request.isEmpty() || request.isBlank())
            throw new OllamaBaseException("Request prompt not valid.");


        // make request
        String response = String.valueOf(ollamaAPI.ask(ollamaPrompt.getModel(), request));

        // Parse response and validate it
        String parsedResponse = ollamaPrompt.parseResponse(from, response);
        // Check for null response
        if (parsedResponse == null || parsedResponse.equalsIgnoreCase("null") || parsedResponse.isEmpty() || parsedResponse.isBlank()) {
            throw new OllamaBaseException("Parsed response not valid.");
        } else {
            ollamaPrompt.addMessage(from + ": " + message);
            ollamaPrompt.addMessage(ollamaPrompt.getCharacterId() + ": " + parsedResponse);
            ollamaPrompt.trimMessages();
        }

        return parsedResponse;
    }
//
//        if (!response.isEmpty())
//            messages.add(characterId + ": " + response);
//
//        if (generateEmotionCounter >= generateEmotionCountMax) {
//            generateEmotion(from);
//            generateEmotionCounter = 0;
//        } else
//            generateEmotionCounter++;

//    public void generateEmotion(String from) {
//        String newScenario = generateScenario(from);
//        String newMood = generateMood(from);
//        String newAppearence = generateAppearance(from);
//        if (newScenario.contains("Scenario:"))
//            newScenario.replace("Scenario:", "");
//        if (newScenario.contains("Scenario:"))
//            newScenario.replace("Scenario:", "");
//        if (newScenario.contains("Appearance:"))
//            newScenario.replace("Appearance:", "");
//        ProjectLife.getPlugin(ProjectLife.class).getLogger().info("New Scenario: " + newScenario); //todo remove debug
//        ProjectLife.getPlugin(ProjectLife.class).getLogger().info("New Mood: " + newMood); //todo remove debug
//        ProjectLife.getPlugin(ProjectLife.class).getLogger().info("New Appearance: " + newAppearence); //todo remove debug
//        this.scenario = newScenario;
//        this.mood = newMood;
//    }
//
//    private String generateScenario(String from) {
//        return trimResponse(from, makeRequest(from, "Briefly summarize your current scenario and our conversation, excluding your mood."));
//    }
//
//    private String generateAppearance(String from) {
//        return trimResponse(from, makeRequest(from, "Briefly summarize your appearance only making changes to the current appearance only based on {character}'s scenario and responses."));
//    }
//
//    private String generateMood(String from) {
//        return trimResponse(from, makeRequest(from, "Briefly describe your mood, taking into account the current scenario and our conversation."));
//    }
//
//    private String makeRequest(String from, String message) {
//        PlOllamaAPI ollamaAPI = new PlOllamaAPI(OLLAMA_CURL);
//        String prompt = "\n" + generatePrompt() + "\n" + from + ": " + message;
//        ProjectLife.getPlugin(ProjectLife.class).getLogger().info("Prompt: " + prompt);
//        String response;
//        try {
//            response =  String.valueOf(ollamaAPI.ask(model, prompt.replace("\\\\{\\user*\\\\}", from)));
//        } catch (OllamaBaseException e) {
//            throw new RuntimeException(e);
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
//        return response;
//    }
}
