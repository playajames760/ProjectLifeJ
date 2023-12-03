package me.playajames.projectlife.common.commands;

import dev.jorel.commandapi.annotations.Alias;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Help;
import dev.jorel.commandapi.annotations.arguments.AGreedyStringArgument;
import me.playajames.projectlife.ProjectLife;
import me.playajames.projectlife.common.ollama.OllamaCharacter;
import me.playajames.projectlife.common.ollama.OllamaCharacterManager;
import org.bukkit.command.CommandSender;

@Command("roji")
@Alias("r")
@Help("Chat with Roji!")
public class RojiCommand {

    @Default
    public static void sayHello(CommandSender sender, @AGreedyStringArgument String message) {
        sender.sendMessage(sender.getName() + " -> Roji: " + message);
        OllamaCharacter roji = OllamaCharacterManager.getCharacter("Roji");
        ProjectLife.newChain()
                        .async(() -> {
                            sender.sendMessage("Roji: " + roji.chat(sender.getName(), message));
                        })
                .execute();

    }


}
