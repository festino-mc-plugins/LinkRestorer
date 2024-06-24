package com.festp.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.help.HelpTopic;

import com.festp.Logger;
import com.festp.config.Config;
import com.festp.config.Config.Key;
import com.google.common.collect.Lists;

public class SpigotCommandValidator implements CommandValidator
{
	private final Config config;
	
	// lazy initialization
	private Collection<String> commands = new HashSet<>();
	private Map<String, Collection<String>> commandsAliases = new HashMap<>();
	
	public SpigotCommandValidator(Config config) {
		this.config = config;
	}
	
	@Override
	public boolean commandExists(String command) {
		tryInit();
		if (!command.startsWith("/")) return false;
        return commands.contains(command.substring(1));
	}

	@Override
	public Collection<String> getCommandAliases(String command) {
		tryInit();
		Collection<String> aliases = commandsAliases.getOrDefault(command, Collections.emptyList());
		return aliases;
	}

	private void tryInit()
	{
		if (!commands.isEmpty()) return;
		
		List<HashSet<String>> aliasSets = getCommandAliasSets();
		
		commands = new HashSet<>();
		commandsAliases = new HashMap<>();
		for (HashSet<String> aliasSet : aliasSets) {
			commands.addAll(aliasSet);
			for (String alias : aliasSet)
				commandsAliases.put(alias, aliasSet);
		}
		
		if (config.get(Key.LOG_DEBUG, true)) // TODO Logger.canLog(level)
		{
			List<String> setStrings = Lists.newArrayList();
			for (HashSet<String> set : aliasSets)
				setStrings.add(String.join(", ", set));
			
			Logger.info("Alias sets: {" + String.join("}, {", setStrings) + "}");
		}
	}
	
	private static List<HashSet<String>> getCommandAliasSets()
	{
		List<HashSet<String>> aliasSets = Lists.newArrayList();
		for (HelpTopic topic : Bukkit.getServer().getHelpMap().getHelpTopics()) {
			HashSet<String> aliasSet = new HashSet<>();
            String fullText = topic.getFullText(Bukkit.getConsoleSender());
			if (topic.getName().startsWith("/")) {
				aliasSet.add(topic.getName().substring(1));
			}

            // format: <color>Alias for <color>/msg
            if (fullText.contains("Alias for ")) {
                String[] text = fullText.split("\n");
                int index = text[0].indexOf('/');
                if (index >= 0)
                {
                    String aliasFor = text[0].substring(index + 1);
                    aliasSet.add(aliasFor);
                }
            }
            
            if (aliasSet.size() == 0) continue;
            
            boolean merged = false;
            for (HashSet<String> previousSet : aliasSets) {
            	if (!Collections.disjoint(previousSet, aliasSet)) {
            		merged = true;
            		previousSet.addAll(aliasSet);
            	}
            }
            if (!merged) {
            	aliasSets.add(aliasSet);
            }
        }
		return aliasSets;
	}
}
