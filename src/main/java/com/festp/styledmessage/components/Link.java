package com.festp.styledmessage.components;

import com.festp.utils.LinkUtils;

public class Link implements TextComponent
{
	public static final String DEFAULT_PROTOCOL = "https://";
	
	private final String _text;
	
	private final int _beginIndex;
	private final int _endIndex;
	private final boolean _hasProtocol;
	
	public Link(String orig, int beginIndex, int endIndex, boolean hasProtocol)
	{
		_text = orig.substring(beginIndex, endIndex);
		_beginIndex = beginIndex;
		_endIndex = endIndex;
		_hasProtocol = hasProtocol;
	}

	public int getBeginIndex() {
		return _beginIndex;
	}

	public int getEndIndex() {
		return _endIndex;
	}

	public String getUrl() {
		String url = LinkUtils.applyBrowserEncoding(_text);
		if (!_hasProtocol)
			url = DEFAULT_PROTOCOL + url;
		
		return url;
	}

	@Override
	public String getPlainText() {
		return _text;
	}
}
