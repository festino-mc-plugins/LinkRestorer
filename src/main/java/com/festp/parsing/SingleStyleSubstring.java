package com.festp.parsing;

import java.util.List;

import com.festp.styledmessage.components.StyleAttribute;

public class SingleStyleSubstring {
	public final int beginIndex;
	public final int endIndex;
	public final List<StyleAttribute> style;
	
	public SingleStyleSubstring(int beginIndex, int endIndex, List<StyleAttribute> style)
	{
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
		this.style = style;
	}
}
