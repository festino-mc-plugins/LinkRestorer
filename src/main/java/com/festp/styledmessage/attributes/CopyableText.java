package com.festp.styledmessage.attributes;


public class CopyableText implements StyleAttribute
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