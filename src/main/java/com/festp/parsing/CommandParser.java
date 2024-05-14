package com.festp.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.festp.styledmessage.components.Command;
import com.festp.styledmessage.components.TextComponent;
import com.google.common.collect.Lists;

public class CommandParser implements ComponentParser
{
	private static final String COMMAND_SEPARATORS = " \\.\\?!,:;\\\\\"'";
	private static final String COMMAND_REGEX = "(?:[" + COMMAND_SEPARATORS + "]|^)(\\/[^" + COMMAND_SEPARATORS + "]{1,})";
	private static final Pattern PATTERN = Pattern.compile(COMMAND_REGEX, Pattern.CASE_INSENSITIVE);
	private static final int COMMAND_GROUP_INDEX = 1;

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
			
			Command command = tryParseCommand(message, matcher);
			if (command == null) {
				substrings.add(new SingleStyleSubstring(start, end, emptyStyle));
			}
			else {
				if (message.charAt(start) != '/') {
					// remove dot if it has index 0, else add substring of single char
					if (message.charAt(start) != '.' || start != 0) {
						substrings.add(new SingleStyleSubstring(start, start + 1, emptyStyle));
					}
					start++;
				}
				substrings.add(new SingleStyleSubstring(start, end, Lists.newArrayList(command)));
			}
			
			prevEnd = end;
		}
		if (prevEnd < message.length())
			substrings.add(new SingleStyleSubstring(prevEnd, message.length(), emptyStyle));
		
		return substrings;
	}

	private static Command tryParseCommand(String message, Matcher matcher)
	{
		String command = message.substring(matcher.start(COMMAND_GROUP_INDEX), matcher.end(COMMAND_GROUP_INDEX));
		// check if command is registered
		return new Command(command);
	}
}
