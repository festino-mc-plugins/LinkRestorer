package com.festp.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.festp.styledmessage.components.CopyableText;
import com.festp.styledmessage.components.TextComponent;
import com.google.common.collect.Lists;

public class CopyableTextParser implements ComponentParser
{
	// negative lookahead may be a bad choice
	private static final String COPYABLE_TEXT_REGEX = ",,((?:(?!,,).)*),,";
	private static final Pattern PATTERN = Pattern.compile(COPYABLE_TEXT_REGEX);
	private static final int COPYABLE_TEXT_INDEX = 1;
	
	@Override
	public List<SingleStyleSubstring> getComponents(String message) {
		List<TextComponent> emptyStyle = Lists.newArrayList();
		Matcher matcher = PATTERN.matcher(message);

		int prevEnd = 0;
		List<SingleStyleSubstring> substrings = new ArrayList<>();
		while (matcher.find())
		{
			int start = matcher.start();
			int end = matcher.end();
			if (prevEnd < start)
				substrings.add(new SingleStyleSubstring(prevEnd, start, emptyStyle));
			
			int copyableStart = matcher.start(COPYABLE_TEXT_INDEX);
			int copyableEnd = matcher.end(COPYABLE_TEXT_INDEX);
			CopyableText copyable = new CopyableText(message.substring(copyableStart, copyableEnd));
			substrings.add(new SingleStyleSubstring(copyableStart, copyableEnd, Lists.newArrayList(copyable)));
			
			prevEnd = end;
		}
		if (prevEnd < message.length())
			substrings.add(new SingleStyleSubstring(prevEnd, message.length(), emptyStyle));
		
		return substrings;
	}
}
