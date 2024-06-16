package com.festp.styledmessage;

import java.util.List;

import com.festp.styledmessage.attributes.StyleAttribute;

public class SingleStyleMessage {
	private final String plainText;
	private final List<StyleAttribute> style;
	
	public SingleStyleMessage(String plainText, List<StyleAttribute> style)
	{
		this.plainText = plainText;
		this.style = style;
	}
	
	public String getText()
	{
		return plainText;
	}
	
	public List<StyleAttribute> getStyle()
	{
		return style;
	}
}
