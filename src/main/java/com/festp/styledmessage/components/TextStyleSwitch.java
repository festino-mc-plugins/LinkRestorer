package com.festp.styledmessage.components;

public class TextStyleSwitch implements TextComponent {
	private final int beginIndex;
	private final int endIndex;
	private final String plainText;
	private final TextStyle style;
	
	public TextStyleSwitch(TextStyle style, int beginIndex, int endIndex, String plainText)
	{
		this.style = style;
		this.beginIndex = beginIndex;
		this.endIndex = endIndex;
		this.plainText = plainText;
	}

	@Override
	public int getBeginIndex() {
		return beginIndex;
	}

	@Override
	public int getEndIndex() {
		return endIndex;
	}

	@Override
	public String getPlainText() {
		return plainText;
	}
	
	public TextStyle getStyle() {
		return style;
	}
}
