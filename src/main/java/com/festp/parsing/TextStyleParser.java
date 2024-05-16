package com.festp.parsing;

import java.util.List;

import org.bukkit.ChatColor;

import com.festp.styledmessage.components.TextStyle;
import com.google.common.collect.Lists;

public class TextStyleParser implements ComponentParser
{
	@Override
	public List<SingleStyleSubstring> getComponents(String message) {
		List<SingleStyleSubstring> substrings = Lists.newArrayList();
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
			
			if (beginIndex != nextBeginIndex)
			{
				substrings.add(new SingleStyleSubstring(substringBegin, nextBeginIndex, Lists.newArrayList(style.clone())));
			}
			
			style.update(message, nextBeginIndex, i);

			beginIndex = nextBeginIndex;
			substringBegin = i;
			i--;
		}

		int nextBeginIndex = message.length();
		substrings.add(new SingleStyleSubstring(substringBegin, nextBeginIndex, Lists.newArrayList(style)));
		
		return substrings;
	}
}
