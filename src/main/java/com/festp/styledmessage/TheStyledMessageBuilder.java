package com.festp.styledmessage;

import java.util.Collection;
import java.util.List;

import com.festp.parsing.StyleParser;
import com.festp.styledmessage.ClosableStyledMessage.ClosableStyledMessagePart;
import com.festp.styledmessage.components.StyleAttribute;
import com.google.common.collect.Lists;

public class TheStyledMessageBuilder implements StyledMessageBuilder
{
	private final List<StyleParser> globalParsers;
	private final List<StyleParser> splittingParsers;
	private StyledMessage styledMessage;
	private List<StyleAttribute> startStyle;
	
	public TheStyledMessageBuilder(List<StyleParser> globalParsers,
								List<StyleParser> splittingParsers)
	{
		this.globalParsers = globalParsers;
		this.splittingParsers = splittingParsers;
		styledMessage = new StyledMessage();
		startStyle = Lists.newArrayList();
	}
	
	public StyledMessage build()
	{
		if (styledMessage.getStyledParts().isEmpty())
			return new StyledMessage(Lists.newArrayList(new SingleStyleMessage("", Lists.newArrayList())));
		
		return styledMessage;
	}
	
	public void clear()
	{
		styledMessage = new StyledMessage();
		startStyle = Lists.newArrayList();
	}
	
	public TheStyledMessageBuilder appendSplitting(String text)
	{
		append(text, false);
		return this;
	}
	
	public TheStyledMessageBuilder appendSplitting(String text, Collection<StyleAttribute> additionalAttributes)
	{
		int startLength = styledMessage.getStyledParts().size();
		append(text, false);
		int endLength = styledMessage.getStyledParts().size();
		for (int i = startLength; i < endLength; i++) {
			styledMessage.getStyledParts().get(i).getStyle().addAll(additionalAttributes);
		}
		return this;
	}

	public TheStyledMessageBuilder append(String text)
	{
		return append(text, true);
	}
	
	private TheStyledMessageBuilder append(String text, boolean useSplittingParsers)
	{
		ClosableStyledMessage closableMessage = new ClosableStyledMessage(Lists.newArrayList(new SingleStyleMessage(text, startStyle)));
		
		for (StyleParser parser : globalParsers)
		{
			for (ClosableStyledMessagePart part : closableMessage.getOpenParts())
			{
				part.replace(parser.getStyles(part.getPlainText()), false);
			}
		}

		startStyle = closableMessage.getEndStyle();

		if (!useSplittingParsers)
		{
			styledMessage.addAll(closableMessage.getStyledParts());
			return this;
		}
		
		for (StyleParser parser : splittingParsers)
		{
			for (ClosableStyledMessagePart part : closableMessage.getOpenParts())
			{
				part.replace(parser.getStyles(part.getPlainText()), true);
			}
		}

		styledMessage.addAll(closableMessage.getStyledParts());
		return this;
	}
}
