package me.playajames.projectlife.common.ollama;

import java.util.List;

public interface OllamaPrompt {

    String generateRaw();
    String generateRequestPrompt(String from, String message);
    String parseResponse(String from, String rawResponse);
    String getModel();
    String getCharacterId();
    List<String> getMessages();
    void addMessage(String message);
    void trimMessages();

}
