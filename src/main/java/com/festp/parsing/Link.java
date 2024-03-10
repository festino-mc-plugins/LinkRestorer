package com.festp.parsing;

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
	
	public String getString()
	{
		return orig.substring(beginIndex, endIndex);
	}
}
