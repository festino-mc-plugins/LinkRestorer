package com.festp.styledmessage;

import java.util.List;

import com.festp.parsing.ComponentParser;
import com.festp.parsing.SingleStyleSubstring;
import com.festp.styledmessage.components.TextComponent;
import com.google.common.collect.Lists;

public class StyledMessageBuilder {
	private final List<ComponentParser> globalParsers;
	private final List<ComponentParser> splittingParsers;
	private StyledMessage styledMessage;
	private List<TextComponent> startStyle;
	
	public StyledMessageBuilder(List<ComponentParser> globalParsers,
								List<ComponentParser> splittingParsers)
	{
		this.globalParsers = globalParsers;
		this.splittingParsers = splittingParsers;
		styledMessage = new StyledMessage();
		startStyle = Lists.newArrayList();
	}
	
	public StyledMessage build()
	{
		StyledMessage result;
		if (styledMessage.getStyledParts().isEmpty()) {
			result =  new StyledMessage(Lists.newArrayList(new SingleStyleMessage("", startStyle)));
		}
		else {
			result = styledMessage;
			styledMessage = new StyledMessage();
		}
		startStyle = Lists.newArrayList();
		return result;
	}
	
	public StyledMessageBuilder append(String text)
	{
		String plainText = text;
		List<SingleStyleSubstring> styles = Lists.newArrayList(new SingleStyleSubstring(0, plainText.length(), startStyle));
		List<TextComponent> nextStartStyle = Lists.newArrayList();
		
		for (ComponentParser parser : globalParsers)
		{
			StringBuilder newPlainText = new StringBuilder();
			List<SingleStyleSubstring> newStyles = Lists.newArrayList();
			SingleStyleSubstring endStyle = null;
			for (SingleStyleSubstring styledSubstring : parser.getComponents(plainText))
			{
				if (!validateSubstring(styledSubstring, plainText, text, parser))
					continue;
				
				appendSubstring(newStyles, styledSubstring, styles);
				newPlainText.append(plainText.substring(styledSubstring.beginIndex, styledSubstring.endIndex));
				endStyle = styledSubstring;
			}
			
			if (endStyle != null)
			{
				// update nextStartStyle using endStyle
			}
			
			styles = newStyles;
			plainText = newPlainText.toString();
		}
		
		startStyle = nextStartStyle;
		
		for (ComponentParser parser : splittingParsers)
		{
			StringBuilder newPlainText = new StringBuilder();
			List<SingleStyleSubstring> newStyles = Lists.newArrayList();
			// FOR PART: used => append, unused => parse and append
			for (SingleStyleSubstring styledSubstring : parser.getComponents(plainText))
			{
				if (!validateSubstring(styledSubstring, plainText, text, parser))
					continue;
				
				appendSubstring(newStyles, styledSubstring, styles);
			}
			
			styles = newStyles;
			plainText = newPlainText.toString();
		}
		
		List<SingleStyleMessage> styledParts = Lists.newArrayList();
		for (SingleStyleSubstring styledSubstring : styles) {
			String partText = plainText.substring(styledSubstring.beginIndex, styledSubstring.endIndex);
			styledParts.add(new SingleStyleMessage(partText, styledSubstring.components));
		}
		styledMessage.append(new StyledMessage(styledParts));
		
		return this;
	}
	
	private boolean validateSubstring(SingleStyleSubstring styledSubstring, String plainText, String text, ComponentParser parser)
	{
		if (styledSubstring.beginIndex < 0)
		{
			System.err.println("Styled substring index is out of range " + styledSubstring.beginIndex + " < 0"
					           + " in " + parser + " for  text\"" + text + "\"");
			return false;
		}
		if (styledSubstring.endIndex > plainText.length())
		{
			System.err.println("Styled substring index is out of range " + styledSubstring.endIndex + " > " + plainText.length()
					           + " in " + parser + " for  text\"" + text + "\"");
			return false;
		}
		if (styledSubstring.beginIndex > styledSubstring.endIndex)
		{
			System.err.println("Styled substring has negative length: " + styledSubstring.beginIndex + " > " + styledSubstring.endIndex
					           + " in " + parser + " for  text\"" + text + "\"");
			return false;
		}
		
		return true;
	}
	
	/** Append <b>styledSubstring</b> to <b>styles</b> updating indices and merging styles with <b>oldStyles</b>
	 * @param styles is new styled substrings
	 * @param styledSubstring is new styled substring, indices belong to old plain text
	 * @param oldStyles defines old styled substrings (and therefore old plain text)
	 * */
	private void appendSubstring(List<SingleStyleSubstring> styles, SingleStyleSubstring styledSubstring, List<SingleStyleSubstring> oldStyles)
	{
		int beginIndex = styles.size() == 0 ? 0 : styles.get(styles.size() - 1).endIndex;
		int endIndex = beginIndex + styledSubstring.endIndex - styledSubstring.beginIndex;
		for (SingleStyleSubstring oldStyledSubstring : oldStyles)
		{
			if (oldStyledSubstring.endIndex < styledSubstring.beginIndex) continue;
			if (oldStyledSubstring.beginIndex >= styledSubstring.endIndex) break;
			
			int partLength = oldStyledSubstring.endIndex - oldStyledSubstring.beginIndex;
			List<TextComponent> components = styledSubstring.components.size() == 0 ? oldStyledSubstring.components :
				oldStyledSubstring.components.size() == 0 ? styledSubstring.components : null;
			
			if (components == null) {
				components = Lists.newArrayList(oldStyledSubstring.components);
				components.addAll(styledSubstring.components);
				// TODO update updaable
			}
			
			styles.add(new SingleStyleSubstring(beginIndex, Math.min(endIndex, beginIndex + partLength), components));
			beginIndex += partLength;
		} 
	}
}
