package com.festp.utils;

import java.util.Collection;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.help.HelpTopic;

import com.festp.Logger;

public class SpigotCommandValidator implements CommandValidator
{
	// lazy initialization
	private Collection<String> commands = new HashSet<>();
	
	@Override
	public boolean commandExists(String command) {
		if (commands.isEmpty()) commands = getCommands();
		
		if (command.charAt(0) != '/') return false;
        return commands.contains(command.substring(1));
	}

	private static HashSet<String> getCommands()
	{
		HashSet<String> commandsAndAliases = new HashSet<>();
		for (HelpTopic topic : Bukkit.getServer().getHelpMap().getHelpTopics()) {
            String fullText = topic.getFullText(Bukkit.getConsoleSender());
			if (topic.getName().startsWith("/"))
				commandsAndAliases.add(topic.getName().substring(1));

            // format: <color>Alias for <color>/msg
            if (fullText.contains("Alias for ")) {
                String[] text = fullText.split("\n");
                int index = text[0].indexOf('/');
                if (index >= 0)
                {
                    String aliasFor = text[0].substring(index + 1);
                    commandsAndAliases.add(aliasFor);
                }
            }
        }
		Logger.info("Found commands: " + String.join(", ", commandsAndAliases));
		return commandsAndAliases;
	}
}
