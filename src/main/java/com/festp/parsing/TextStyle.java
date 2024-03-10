package com.festp.parsing;

import org.bukkit.ChatColor;

public class TextStyle {
	String color = "";
	String style = "";
	
	public TextStyle() { }
	
	public void update(String s, int minIndex, int maxIndex)
	{
		for (int i = minIndex; i < maxIndex; i += 2)
		{
			char c = s.charAt(i + 1);
			if (c == 'x')
			{
				// x234567
				i += 7 * 2;
				if (i >= maxIndex)
					return;
				
				color = "#";
				for (int j = 0; j < 6; j++)
					color += s.charAt(i + 1 - 2 * 6 + 2 * j);
			}
			else
			{
				update(c);
			}
		}
	}
	
	public String getCodes()
	{
		if (isRgbColor())
			return style;
		
		return style + color;
	}
	
	public String getJson()
	{
		if (isRgbColor())
			return ",\"color\":\"" + color + "\"";
		
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
}
