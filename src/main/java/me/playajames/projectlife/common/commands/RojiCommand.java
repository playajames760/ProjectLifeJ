package me.playajames.projectlife.common.commands;

import dev.jorel.commandapi.annotations.*;
import dev.jorel.commandapi.annotations.arguments.AGreedyStringArgument;
import dev.jorel.commandapi.annotations.arguments.ALiteralArgument;
import dev.jorel.commandapi.annotations.arguments.AMultiLiteralArgument;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import io.github.amithkoujalgi.ollama4j.core.exceptions.OllamaBaseException;
import me.playajames.projectlife.ProjectLife;
import me.playajames.projectlife.common.ollama.OllamaCharacter;
import me.playajames.projectlife.common.ollama.OllamaCharacterManager;
import org.bukkit.command.CommandSender;

import java.io.IOException;

@Command("roji")
@Alias("r")
@Help("Chat with Roji!")
public class RojiCommand {

    @Default
    public static void chat(CommandSender sender, @AGreedyStringArgument String message) {
        sender.sendMessage(sender.getName() + " -> Roji: " + message);
        OllamaCharacter roji = OllamaCharacterManager.getCharacter("Roji");
        ProjectLife.newChain()
                        .async(() -> {
                            try {
                                String response = roji.chat(sender.getName(), message);
                                if (response != null)
                                    sender.sendMessage("Roji: " + response);
                            } catch (OllamaBaseException | IOException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .execute();

    }

    @Subcommand("clearmessages")
    public static void clearMessages(CommandSender sender, @AStringArgument() String characterId) {
        OllamaCharacter ollamaCharacter = OllamaCharacterManager.getCharacter(characterId);
        if (ollamaCharacter != null) {
            ollamaCharacter.ollamaPrompt.getMessages().clear();
            sender.sendMessage(characterId + "'s message memory has been cleared.");
        } else {
            sender.sendMessage("Could not find character with that ID.");
        }
    }


}
