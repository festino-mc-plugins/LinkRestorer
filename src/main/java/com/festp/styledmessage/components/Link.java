package com.festp.styledmessage.components;

public class Link implements StyleAttribute
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
