package com.festp.parsing;

import com.festp.styledmessage.components.TextComponent;

public class ParsedTextComponent<T extends TextComponent> {
	public final int beginIndex;
	public final int endIndex;
	public final T component;
	
	public ParsedTextComponent(int beginIndex, int endIndex, T component) {
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
		this.component = component;
	}
}
