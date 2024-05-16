package com.festp.styledmessage.components;

public class Link implements TextComponent
{
	private final String url;
	
	public Link(String url)
	{
		this.url = url;
	}

	public String getUrl() {
		return url;
	}
}
