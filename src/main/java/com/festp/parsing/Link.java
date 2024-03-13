package com.festp.parsing;

import com.festp.utils.LinkUtils;

public class Link
{
	public String orig;
	public int beginIndex;
	public int endIndex;
	public boolean hasProtocol;
	
	public Link(String orig, int beginIndex, int endIndex, boolean hasProtocol)
	{
		this.orig = orig;
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
		this.hasProtocol = hasProtocol;
	}

	public String getUrl() {
		String url = LinkUtils.applyBrowserEncoding(getText());
		if (!hasProtocol)
			url = "https://" + url;
		
		return url;
	}
	
	public String getText()
	{
		return orig.substring(beginIndex, endIndex);
	}
}
