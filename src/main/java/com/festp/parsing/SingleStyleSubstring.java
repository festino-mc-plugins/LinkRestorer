package com.festp.parsing;

import java.util.List;

import com.festp.styledmessage.components.TextComponent;

public class SingleStyleSubstring {
	public final int beginIndex;
	public final int endIndex;
	public final List<TextComponent> components;
	
	public SingleStyleSubstring(int beginIndex, int endIndex, List<TextComponent> components)
	{
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
		this.components = components;
	}
}
