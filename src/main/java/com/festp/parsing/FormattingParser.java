package com.festp.parsing;

import java.util.List;

import org.bukkit.ChatColor;

import com.festp.styledmessage.attributes.Formatting;
import com.google.common.collect.Lists;

public class FormattingParser implements StyleParser
{
	@Override
	public List<SingleStyleSubstring> getStyles(String message) {
		List<SingleStyleSubstring> substrings = Lists.newArrayList();
		Formatting formatting = new Formatting();
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
				substrings.add(new SingleStyleSubstring(substringBegin, nextBeginIndex, Lists.newArrayList(formatting.clone())));
			}
			
			formatting.update(message, nextBeginIndex, i);

			beginIndex = nextBeginIndex;
			substringBegin = i;
			i--;
		}

		int nextBeginIndex = message.length();
		substrings.add(new SingleStyleSubstring(substringBegin, nextBeginIndex, Lists.newArrayList(formatting)));
		
		return substrings;
	}
}
