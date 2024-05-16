package com.festp.styledmessage.components;


public class CopyableText implements TextComponent
{
	private final String text;
	
	public CopyableText(String text)
	{
		this.text = text;
	}

	public String getText() {
		return text;
	}
}