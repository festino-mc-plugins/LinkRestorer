package com.festp.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.festp.styledmessage.components.Command;
import com.festp.styledmessage.components.CopyableText;
import com.festp.styledmessage.components.TextComponent;
import com.festp.utils.CommandValidator;
import com.google.common.collect.Lists;

public class CopyableTextParser implements ComponentParser
{
	private static final int COPYABLE_TEXT_INDEX = 1;
	private final Pattern pattern;

	private final boolean useCommands;
	private final boolean useCopyableText;
	private final CommandValidator commandValidator;
	
	public CopyableTextParser(String beginQuotes, String endQuotes, boolean useCommands, boolean useCopyableText, CommandValidator commandValidator) {
		this.commandValidator = commandValidator;
		this.useCommands = useCommands;
		this.useCopyableText = useCopyableText;
		if (beginQuotes.isEmpty() || endQuotes.isEmpty()) {
			pattern = null;
			return;
		}

		beginQuotes = Pattern.quote(beginQuotes);
		endQuotes = Pattern.quote(endQuotes);
		// negative lookahead may be a bad choice
		String copyableTextRegex = beginQuotes + "((?:(?!" + endQuotes + ").)*)" + endQuotes;
		pattern = Pattern.compile(copyableTextRegex);
	}
	
	@Override
	public List<SingleStyleSubstring> getComponents(String message) {
		if (pattern == null) {
			return Lists.newArrayList(new SingleStyleSubstring(0, message.length(), Lists.newArrayList()));
		}

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
			TextComponent component = getComponent(message.substring(copyableStart, copyableEnd));
			List<TextComponent> components = component == null ? emptyStyle : Lists.newArrayList(component);
			substrings.add(new SingleStyleSubstring(copyableStart, copyableEnd, components));
			
			prevEnd = end;
		}
		if (prevEnd < message.length())
			substrings.add(new SingleStyleSubstring(prevEnd, message.length(), emptyStyle));
		
		return substrings;
	}
	
	private TextComponent getComponent(String s)
	{
		int spaceIndex = s.indexOf(' ');
		String firstPart = spaceIndex < 0 ? s : s.substring(0, spaceIndex);
		if (useCommands && commandValidator.commandExists(firstPart)) {
			return new Command(s);
		}
		if (useCopyableText) {
			return new CopyableText(s);
		}
		return null;
	}
}
