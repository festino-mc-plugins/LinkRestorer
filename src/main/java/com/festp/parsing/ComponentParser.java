package com.festp.parsing;

import com.festp.styledmessage.components.TextComponent;

public interface ComponentParser {
	public Iterable<? extends TextComponent> getComponents(String s);
}
