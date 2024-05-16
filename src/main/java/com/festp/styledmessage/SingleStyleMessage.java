package com.festp.styledmessage;

import java.util.List;

import com.festp.styledmessage.components.TextComponent;

public class SingleStyleMessage {
	private final String plainText;
	private final List<TextComponent> components;
	
	public SingleStyleMessage(String plainText, List<TextComponent> components)
	{
		this.plainText = plainText;
		this.components = components;
	}
	
	public String getText()
	{
		return plainText;
	}
	
	public List<TextComponent> getComponents()
	{
		return components;
	}
}
