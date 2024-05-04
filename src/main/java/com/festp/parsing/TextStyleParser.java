package com.festp.parsing;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;

import com.festp.styledmessage.components.TextStyle;
import com.festp.styledmessage.components.TextStyleSwitch;

public class TextStyleParser implements ComponentParser {
	@Override
	public List<TextStyleSwitch> getComponents(String message) {
		List<TextStyleSwitch> switches = new ArrayList<>();
		TextStyle style = new TextStyle();
		int beginIndex = 0;
		int substringBegin = 0;
		for (int i = 0; i < message.length(); i++)
		{
			char c = message.charAt(i);
			if (c != ChatColor.COLOR_CHAR)
			{
				continue;
			}
			
			int nextBeginIndex = i;
			while (i < message.length() && message.charAt(i) == ChatColor.COLOR_CHAR)
			{
				i += 2;
			}
			
			if (beginIndex != nextBeginIndex || i == message.length())
			{
				String plainText = message.substring(substringBegin, nextBeginIndex);
				TextStyleSwitch styleSwitch = new TextStyleSwitch(style.clone(), beginIndex, nextBeginIndex, plainText);
				switches.add(styleSwitch);
			}
			
			style.update(message, nextBeginIndex, i);

			beginIndex = nextBeginIndex;
			substringBegin = i;
			i--;
		}
		
		if (substringBegin != message.length())
		{
			int nextBeginIndex = message.length();
			String plainText = message.substring(substringBegin, nextBeginIndex);
			TextStyleSwitch styleSwitch = new TextStyleSwitch(style, beginIndex, nextBeginIndex, plainText);
			switches.add(styleSwitch);
		}
		
		return switches;
	}

}
