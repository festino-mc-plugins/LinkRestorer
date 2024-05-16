package com.festp.styledmessage;

import java.util.Collection;

import com.festp.styledmessage.components.TextComponent;

public interface StyledMessageBuilder
{
	public StyledMessage build();
	
	public void clear();
	
	public TheStyledMessageBuilder appendSplitting(String text);
	
	public TheStyledMessageBuilder appendSplitting(String text, Collection<TextComponent> additionalComponents);

	public TheStyledMessageBuilder append(String text);
}
