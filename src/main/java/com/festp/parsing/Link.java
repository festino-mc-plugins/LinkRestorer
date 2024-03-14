package com.festp.parsing;

import com.festp.utils.LinkUtils;

public class Link
{
	private final String _text;
	public final int beginIndex;
	public final int endIndex;
	public final boolean hasProtocol;
	
	public Link(String orig, int beginIndex, int endIndex, boolean hasProtocol)
	{
		_text = orig.substring(beginIndex, endIndex);
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
		this.hasProtocol = hasProtocol;
	}

	public String getUrl() {
		String url = LinkUtils.applyBrowserEncoding(_text);
		if (!hasProtocol)
			url = "https://" + url;
		
		return url;
	}
	
	public String getText()
	{
		return _text;
	}
}
