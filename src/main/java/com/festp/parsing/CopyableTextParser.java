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
	private final Pattern pattern;
	private static final int COPYABLE_TEXT_INDEX = 1;
	
	public CopyableTextParser(String beginQuotes, String endQuotes) {
		if (beginQuotes.isEmpty()) beginQuotes = ",,";
		if (endQuotes.isEmpty()) endQuotes = ",,";

		beginQuotes = Pattern.quote(beginQuotes);
		endQuotes = Pattern.quote(endQuotes);
		// negative lookahead may be a bad choice
		String copyableTextRegex = beginQuotes + "((?:(?!" + endQuotes + ").)*)" + endQuotes;
		pattern = Pattern.compile(copyableTextRegex);
	}
	
	@Override
	public List<SingleStyleSubstring> getComponents(String message) {
		List<TextComponent> emptyStyle = Lists.newArrayList();
		Matcher matcher = pattern.matcher(message);

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
