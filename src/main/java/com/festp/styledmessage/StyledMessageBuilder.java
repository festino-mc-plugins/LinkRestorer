package com.festp.styledmessage;

import java.util.Collection;

import com.festp.styledmessage.components.StyleAttribute;

public interface StyledMessageBuilder
{
	public StyledMessage build();
	
	public void clear();
	
	public TheStyledMessageBuilder appendSplitting(String text);
	
	public TheStyledMessageBuilder appendSplitting(String text, Collection<StyleAttribute> additionalAttributes);

	public TheStyledMessageBuilder append(String text);
}
