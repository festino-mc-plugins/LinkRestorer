package com.festp.parsing;

import java.util.ArrayList;
import java.util.List;

import com.festp.styledmessage.components.Command;
import com.festp.styledmessage.components.CopyableText;
import com.festp.styledmessage.components.TextComponent;
import com.festp.utils.CommandValidator;
import com.google.common.collect.Lists;

public class CopyableTextParser implements ComponentParser
{
	private final boolean useCommands;
	private final boolean useCopyableText;
	private final CommandValidator commandValidator;
	
	private final String beginQuotes;
	private final String endQuotes;
	
	public CopyableTextParser(String beginQuotes, String endQuotes, boolean useCommands, boolean useCopyableText, CommandValidator commandValidator) {
		this.commandValidator = commandValidator;
		this.useCommands = useCommands;
		this.useCopyableText = useCopyableText;
		this.beginQuotes = beginQuotes;
		this.endQuotes = endQuotes;
	}
	
	@Override
	public List<SingleStyleSubstring> getComponents(String message) {
		if (beginQuotes.isEmpty() || endQuotes.isEmpty()) {
			return Lists.newArrayList(new SingleStyleSubstring(0, message.length(), Lists.newArrayList()));
		}

		List<TextComponent> emptyStyle = Lists.newArrayList();

		int prevEnd = 0;
		MatchInfo match = nextMatch(message, prevEnd);
		List<SingleStyleSubstring> substrings = new ArrayList<>();
		while (match != null)
		{
			int start = match.start;
			int end = match.end;
			if (prevEnd < start)
				substrings.add(new SingleStyleSubstring(prevEnd, start, emptyStyle));
			
			int copyableStart = match.contentStart;
			int copyableEnd = match.contentEnd;
			TextComponent component = getComponent(message.substring(copyableStart, copyableEnd));
			List<TextComponent> components = component == null ? emptyStyle : Lists.newArrayList(component);
			substrings.add(new SingleStyleSubstring(copyableStart, copyableEnd, components));
			
			prevEnd = end;
			match = nextMatch(message, prevEnd);
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
	
	private MatchInfo nextMatch(String s, int startIndex)
	{
		int beginLength = beginQuotes.length();
		int endLength = endQuotes.length();
		
		int start = s.indexOf(beginQuotes, startIndex);
		if (start < 0) return null;

		int prevStart = start;
		int nextStart = s.indexOf(beginQuotes, prevStart + beginLength);
		while (nextStart - prevStart == beginLength) {
			prevStart = nextStart;
			nextStart = s.indexOf(beginQuotes, prevStart + beginLength);
		}
		
		if (beginQuotes.equals(endQuotes) && start + beginLength < prevStart) {
			return new MatchInfo(start, prevStart + beginLength, start + beginLength, prevStart);
		}
		
		int end = s.indexOf(endQuotes, prevStart + beginLength);
		if (end < 0) return null;

		end += endLength;
		int nextEnd = s.indexOf(endQuotes, end) + endLength;
		while (nextEnd - end == endLength) {
			end = nextEnd;
			nextEnd = s.indexOf(endQuotes, end) + endLength;
		}
		return new MatchInfo(start, end, start + beginLength, end - endLength);
	}
	
	private static class MatchInfo {
		public final int start;
		public final int end;
		public final int contentStart;
		public final int contentEnd;
		
		public MatchInfo(int start, int end, int contentStart, int contentEnd) {
			this.start = start;
			this.end = end;
			this.contentStart = contentStart;
			this.contentEnd = contentEnd;
		}
	}
}
