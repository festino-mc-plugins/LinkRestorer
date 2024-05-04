package com.festp.parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.festp.styledmessage.SingleStyleMessage;
import com.festp.styledmessage.StyledMessage;
import com.festp.styledmessage.components.TextComponent;

public class RecursiveStyledMessageParser implements StyledMessageParser {
	private final ComponentParser plainParser;
	private final List<ComponentParser> leafParsers = new ArrayList<>();
	
	/** @param parsers expected to return non-intersecting components sorted by getBeginIndex */
	public RecursiveStyledMessageParser(ComponentParser plainParser, Collection<ComponentParser> leafParsers)
	{
		this.plainParser = plainParser;
		this.leafParsers.addAll(leafParsers);
	}
	
	public StyledMessage parse(String message)
	{
		List<SingleStyleSubstring> styledSubstrings = Arrays.asList(new SingleStyleSubstring(0, message.length(), new ArrayList<>()));
		
		StringBuilder prevText = new StringBuilder(message);
		Iterable<? extends TextComponent> components = plainParser.getComponents(prevText.toString());
		StringBuilder text = new StringBuilder();
		int prevEnd = 0;
		for (TextComponent component : components)
		{
			if (prevEnd < component.getBeginIndex())
			{
				text.append(prevText.subSequence(prevEnd, component.getBeginIndex()));
			}
			text.append(component.getPlainText());
			prevEnd = component.getEndIndex();
		}
		if (prevEnd < prevText.length()) {
			text.append(prevText.subSequence(prevEnd, prevText.length()));
		}
		prevText = text;
		
		List<String> unusedParts = Arrays.asList(prevText.toString());
		for (ComponentParser parser : leafParsers)
		{
			// for part in parts:
			for (String part : unusedParts)
			{
				components = parser.getComponents(part);
				for (TextComponent component : components)
				{
					// remember component
					
				}
				// split unused
			}
		}

		List<SingleStyleMessage> styledParts = new ArrayList<>();
		return new StyledMessage(styledParts);
	}
}
