package com.festp.parsing;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

public class StyledMessageParser {
	public static StyledMessage parse(String message)
	{
		StringBuilder plainBuilder = new StringBuilder();
		List<TextStyleSwitch> switches = new ArrayList<>();
		for (int i = 0; i < message.length(); i++)
		{
			char c = message.charAt(i);
			if (c == ChatColor.COLOR_CHAR)
			{
				i += 1;
				// TODO TextStyle logic
				continue;
			}
			
			plainBuilder.append(c);
		}
		
		String plainMessage = plainBuilder.toString();
		List<Link> links = LinkParser.getLinks(plainMessage);
		return new StyledMessage(plainMessage, links, switches);
	}
}
