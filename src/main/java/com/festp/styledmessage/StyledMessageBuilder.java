package com.festp.styledmessage;

import java.util.List;

import com.festp.parsing.ComponentParser;
import com.festp.styledmessage.ClosableStyledMessage.ClosableStyledMessagePart;
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
		if (styledMessage.getStyledParts().isEmpty())
			return new StyledMessage(Lists.newArrayList(new SingleStyleMessage("", Lists.newArrayList())));
		
		return styledMessage;
	}
	
	public void clear()
	{
		styledMessage = new StyledMessage();
		startStyle = Lists.newArrayList();
	}
	
	public StyledMessageBuilder append(String text)
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
