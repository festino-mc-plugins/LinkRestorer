package com.festp.parsing;

import java.util.List;

public class StyledMessage {
	public final String plainText;
	public final List<Link> links;
	public final List<TextStyleSwitch> styleSwitches;
	public final boolean isPlain;
	public final boolean hasLinks;
	
	public StyledMessage(String plainText, List<Link> links, List<TextStyleSwitch> styleSwitches)
	{
		this.plainText = plainText;
		this.links = links;
		this.styleSwitches = styleSwitches;
		hasLinks = links.size() != 0;
		isPlain = !hasLinks && styleSwitches.size() == 0;
	}
}
