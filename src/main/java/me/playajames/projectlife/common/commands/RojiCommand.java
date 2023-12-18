package me.playajames.projectlife.common.commands;

import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Help;
import dev.jorel.commandapi.annotations.arguments.AGreedyStringArgument;
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
                                sender.sendMessage("Roji: " + roji.chat(sender.getName(), message));
                            } catch (OllamaBaseException e) {
                                RojiCommand.chat(sender, message);
                            } catch (IOException | InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        })
                .execute();

    }


}
