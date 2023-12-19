package me.playajames.projectlife.common.ollama;

import io.github.amithkoujalgi.ollama4j.core.exceptions.OllamaBaseException;
import me.playajames.projectlife.ProjectLife;
import me.playajames.projectlife.common.commands.RojiCommand;
import me.playajames.projectlife.common.ollama.prompts.LivingCharacterPrompt;
import me.playajames.projectlife.utils.PlOllamaAPI;

import java.io.IOException;

import static me.playajames.projectlife.ProjectLife.OLLAMA_CURL;

public class OllamaCharacter {

    public OllamaPrompt ollamaPrompt;
    private int generateEmotionCounter = 0;
    private int generateEmotionCountMax;

    public OllamaCharacter(String characterId) {
        this.ollamaPrompt = new LivingCharacterPrompt(characterId);
        this.generateEmotionCountMax = ollamaPrompt.getGenerateEmotionCountMax();
    }

    public String chat(String from, String message) throws OllamaBaseException, IOException, InterruptedException {
        String response = makeRequest(from, message);
        if (response != null) {
            ollamaPrompt.addMessage(from + ": " + message);
            ollamaPrompt.addMessage(ollamaPrompt.getCharacterId() + ": " + response);
            ollamaPrompt.trimMessages();

            if (generateEmotionCounter >= generateEmotionCountMax) {
                ProjectLife.newChain()
                        .async(() -> {
                            try {
                                updateScenario("CONSOLE");
                                updateAppearance("CONSOLE");
                                updateMood("CONSOLE");
                            } catch (OllamaBaseException e) {
                                throw new RuntimeException(e);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .execute();
                generateEmotionCounter = 0;
            } else
                generateEmotionCounter++;
        } else {
            chat(from, message);
        }
        return response;
    }

    public String parseEmotion(String message) {
        if (message.contains("Scenario:"))
            message = message.replace("Scenario:", "");
        if (message.contains("Scenario:"))
            message = message.replace("Scenario:", "");
        if (message.contains("Appearance:"))
            message = message.replace("Appearance:", "");
        return message;
    }

    private void updateScenario(String from) throws OllamaBaseException, IOException, InterruptedException {
        String response = parseEmotion(makeRequest(from, ollamaPrompt.generateRequestPrompt(from,"Briefly summarize your current scenario and your conversation, excluding your mood.")));
        ollamaPrompt.setScenario(response);
    }

    private void updateAppearance(String from) throws OllamaBaseException, IOException, InterruptedException {
        String response = parseEmotion(makeRequest(from, ollamaPrompt.generateRequestPrompt(from,"Briefly summarize your appearance only allowing " + ollamaPrompt.getCharacterId() + "'s messages to make changes to the current appearance.")));
        ollamaPrompt.setAppearance(response);
    }

    private void updateMood(String from) throws OllamaBaseException, IOException, InterruptedException {

        String response = parseEmotion(makeRequest(from, ollamaPrompt.generateRequestPrompt(from,"Briefly describe your mood, taking into account the current scenario and your conversation.")));
        ollamaPrompt.setMood(response);
    }

    private String makeRequest(String from, String message) throws OllamaBaseException, IOException, InterruptedException {
        PlOllamaAPI ollamaAPI = new PlOllamaAPI(OLLAMA_CURL);

        // Parse and validate request
        String request = ollamaPrompt.generateRequestPrompt(from, message);

        if (request.equalsIgnoreCase("null") || request == null || request.isEmpty() || request.isBlank())
            throw new OllamaBaseException("Request was null or empty.");

        // Make request
        String rawResponse = String.valueOf(ollamaAPI.ask(ollamaPrompt.getModel(), request));

        // Parse raw response and validate it
        String parsedResponse = ollamaPrompt.parseResponse(from, rawResponse);

        // Check for null rawResponse and update messages
        if (parsedResponse == null || parsedResponse.equalsIgnoreCase("null") || parsedResponse.isEmpty() || parsedResponse.isBlank()) {
            throw new OllamaBaseException("Response was null or empty.");
        }
        return  parsedResponse;
    }
}
