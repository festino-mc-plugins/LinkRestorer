package com.festp.parsing;

import org.bukkit.ChatColor;

public class TextStyle implements Cloneable {
	private String color = "";
	private String style = "";
	
	public TextStyle() { }
	
	public TextStyle clone()
	{
		TextStyle copy = new TextStyle();
		copy.color = color;
		copy.style = style;
		return copy; 
	}

	/** @return the same TextStyle object for chaining */
	public TextStyle update(String s)
	{
		return update(s, 0, s.length());
	}

	/** @return the same TextStyle object for chaining */
	public TextStyle update(String s, int minIndex, int maxIndex)
	{
		for (int i = minIndex; i < maxIndex; i += 2)
		{
			char c = s.charAt(i + 1);
			if (c == 'x')
			{
				// x234567
				i += 7 * 2;
				if (i > maxIndex)
					return this;
				
				color = "#";
				for (int j = 0; j < 6; j++)
					color += s.charAt(i + 1 - 2 * 6 + 2 * j);
			}
			else
			{
				update(c);
			}
		}
		return this;
	}
	
	public String getCodes()
	{
		if (isRgbColor())
			return style;
		
		return style + color;
	}
	
	/**
	 * @return empty string or JSON fields with a trailing comma like '"color":"#ABCDEF",'*/
	public String getJson()
	{
		if (isRgbColor())
			return "\"color\":\"" + color + "\",";
		
		return "";
	}
	
	private boolean isRgbColor()
	{
		return color.length() > 0 && color.charAt(0) == '#';
	}
	
	private void update(char code)
	{
		code = Character.toLowerCase(code);
		if (code == 'r') {
			color = "";
			style = "";
		}
		else if ('0' <= code && code <= '9' || 'a' <= code && code <= 'f')
		{
			color = "" + ChatColor.COLOR_CHAR + code;
		}
		else
		{
			String newStyle = "" + ChatColor.COLOR_CHAR + code;
			if (!style.contains(newStyle))
				style += newStyle;
		}
	}

	/** @param color is TextSyle of ChatColor.GRAY.toString() + ChatColor.ITALIC.toString()
	 *  @return "color":"gray","italic":"true", */
	public Object getFullJson() {
		String styleStr = getCodes();
		StringBuilder res = new StringBuilder();
		for (int i = 1; i < styleStr.length(); i += 2)
		{
			ChatColor c = ChatColor.getByChar(styleStr.charAt(i));
			if (c == ChatColor.ITALIC)
				res.append("\"italic\":\"true\",");
			else if (c == ChatColor.UNDERLINE)
				res.append("\"underlined\":\"true\",");
			else if (c == ChatColor.BOLD)
				res.append("\"bold\":\"true\",");
			else if (c == ChatColor.STRIKETHROUGH)
				res.append("\"strikethrough\":\"true\",");
			else if (c == ChatColor.MAGIC)
				res.append("\"obfuscated\":\"true\",");
			else {
				res.append("\"color\":\"");
				res.append(c.name().toLowerCase());
				res.append("\",");
			}
		}
		res.append(getJson());
		return res.toString();
	}
}
