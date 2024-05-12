package com.festp.styledmessage.components;

import com.festp.utils.LinkUtils;

public class Link implements TextComponent
{
	private final String url;
	
	public Link(String url)
	{
		this.url = url;
	}

	public String getUrl() {
		return LinkUtils.applyBrowserEncoding(this.url); // TODO move encoding to the presentation level
	}
}
