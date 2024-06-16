package com.festp.styledmessage;

import java.util.Collection;

import com.festp.styledmessage.attributes.StyleAttribute;

public interface StyledMessageBuilder
{
	public StyledMessage build();
	
	public void clear();
	
	public StyledMessageBuilder appendSplitting(String text);
	
	public StyledMessageBuilder appendSplitting(String text, Collection<StyleAttribute> additionalAttributes);

	public StyledMessageBuilder append(String text);
}
