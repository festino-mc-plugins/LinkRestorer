package com.festp.styledmessage;

import java.util.Collection;
import java.util.List;

import com.festp.parsing.ComponentParser;
import com.festp.styledmessage.ClosableStyledMessage.ClosableStyledMessagePart;
import com.festp.styledmessage.components.TextComponent;
import com.google.common.collect.Lists;

public class TheStyledMessageBuilder implements StyledMessageBuilder
{
	private final List<ComponentParser> globalParsers;
	private final List<ComponentParser> splittingParsers;
	private StyledMessage styledMessage;
	private List<TextComponent> startStyle;
	
	public TheStyledMessageBuilder(List<ComponentParser> globalParsers,
								List<ComponentParser> splittingParsers)
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
	
	public TheStyledMessageBuilder appendSplitting(String text, Collection<TextComponent> additionalComponents)
	{
		int startLength = styledMessage.getStyledParts().size();
		append(text, false);
		int endLength = styledMessage.getStyledParts().size();
		for (int i = startLength; i < endLength; i++) {
			styledMessage.getStyledParts().get(i).getComponents().addAll(additionalComponents);
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
		
		for (ComponentParser parser : globalParsers)
		{
			for (ClosableStyledMessagePart part : closableMessage.getOpenParts())
			{
				part.replace(parser.getComponents(part.getPlainText()), false);
			}
		}

		startStyle = closableMessage.getEndStyle();

		if (!useSplittingParsers)
		{
			styledMessage.addAll(closableMessage.getStyledParts());
			return this;
		}
		
		for (ComponentParser parser : splittingParsers)
		{
			for (ClosableStyledMessagePart part : closableMessage.getOpenParts())
			{
				part.replace(parser.getComponents(part.getPlainText()), true);
			}
		}

		styledMessage.addAll(closableMessage.getStyledParts());
		return this;
	}
	
}
