package com.festp.styledmessage.components;

import org.bukkit.ChatColor;

public class TextStyle implements UpdatableTextComponent {
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
	public void update(UpdatableTextComponent other)
	{
		if (!(other instanceof TextStyle)) return;
		
		TextStyle otherStyle = (TextStyle) other;
		update(otherStyle.getCodes());
		if (otherStyle.isRgbColor()) color = otherStyle.color;
	}

	/** @return the same TextStyle object for chaining */
	public TextStyle update(String s)
	{
		return update(s, 0, s.length());
	}

	/** @return the same TextStyle object for chaining */
	public TextStyle update(String s, int minIndex, int maxIndex)
	{
		for (int i = minIndex; i + 1 < maxIndex; i += 2)
		{
			char c = s.charAt(i + 1);
			if (c == 'x')
			{
				// x234567
				i += 7 * 2;
				if (i > maxIndex)
					return this;
				
				style = ChatColor.RESET.toString();
				color = "#";
				for (int j = 0; j < 6; j++)
					color += s.charAt(i + 1 - 2 * 6 + 2 * j);
				
				i -= 2;
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
		
		return color + style;
	}
	
	/**
	 * @return empty string or JSON fields with a trailing comma like '"color":"#ABCDEF",'*/
	public String getJson()
	{
		if (isRgbColor())
			return "\"color\":\"" + color + "\",";
		
		return "";
	}

	/** @return <b>"color":"gray","italic":"true",</b><br>
	 * if object is TextSyle of ChatColor.GRAY.toString() + ChatColor.ITALIC.toString()*/
	public Object getFullJson() {
		String styleStr = getCodes();
		StringBuilder res = new StringBuilder();
		res.append(getJson());
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
			else if (c != ChatColor.RESET && !isRgbColor()) {
				res.append("\"color\":\"");
				res.append(c.name().toLowerCase());
				res.append("\",");
			}
		}
		return res.toString();
	}
	
	private boolean isRgbColor()
	{
		return color.length() > 0 && color.charAt(0) == '#';
	}
	
	private void update(char code)
	{
		code = Character.toLowerCase(code);
		if ('0' <= code && code <= '9' || 'a' <= code && code <= 'f' || code == 'r')
		{
			style = "";
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
