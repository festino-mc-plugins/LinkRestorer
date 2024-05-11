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
