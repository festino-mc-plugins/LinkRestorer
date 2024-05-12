package com.festp.styledmessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StyledMessage {
	private final List<SingleStyleMessage> parts;

	public StyledMessage()
	{
		this(new ArrayList<>());
	}
	
	public StyledMessage(String plainText) {
		this(Arrays.asList(new SingleStyleMessage(plainText, new ArrayList<>())));
	}
	
	public StyledMessage(List<SingleStyleMessage> parts)
	{
		this.parts = parts;
	}
	
	public List<SingleStyleMessage> getStyledParts()
	{
		return parts;
	}

	public void addAll(List<SingleStyleMessage> nextStyledParts) {
		parts.addAll(nextStyledParts);
	}
}
