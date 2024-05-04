package com.festp.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.festp.styledmessage.components.Link;

public class LinkParser implements ComponentParser {
	// TODO check telegram desktop code
	// TextWithTags Ui::InputField::getTextWithTags, getTextWithTagsPart, getTextPart...
	
	private static final String LINK_REGEX = "(((?:https?):\\/\\/)?(?:(?:[-a-z0-9_а-яА-Я]{1,}\\.){1,}([a-z0-9а-яА-Я]{1,}).*?(?=[\\.\\?!,;:]?(?:[" + String.valueOf(org.bukkit.ChatColor.COLOR_CHAR) + " \\n]|$))))";
	private static final Pattern PATTERN = Pattern.compile(LINK_REGEX, Pattern.CASE_INSENSITIVE);
	private static final int SCHEME_GROUP_INDEX = 2;
	private static final int TLD_GROUP_INDEX = 3;

	/**@param message is a plain text 
	 * @return <b>null</b> if no links found */
	@Override
	public List<Link> getComponents(String message) {
		List<Link> links = new ArrayList<>();
		
		Matcher matcher = PATTERN.matcher(message);
		while (matcher.find())
		{
			int tldStart = matcher.start(TLD_GROUP_INDEX);
			int tldEnd = matcher.end(TLD_GROUP_INDEX);
			String tld = message.substring(tldStart, tldEnd);
			int domainsStart = matcher.end(SCHEME_GROUP_INDEX);
			if (domainsStart < 0)
				domainsStart = matcher.start();
			if (hasNumber(tld)) {
				if (!isValidIP(message, domainsStart, tldEnd))
					continue;
			}
			else if (!isValidTLD(tld)) {
				continue;
			}

			boolean hasProtocol = matcher.end(SCHEME_GROUP_INDEX) - matcher.start(SCHEME_GROUP_INDEX) > 0;
			Link link = new Link(message, matcher.start(), matcher.end(), hasProtocol);
			links.add(link);
		}
		
		return links;
	}
	
	private static boolean isValidTLD(String tld) {
		return tld.length() >= 2;
	}
	
	private static boolean isValidIP(String str, int begin, int end)
	{
		if (end - begin < 7)
			return false;
		int val = 0;
		int count = 1;
		for (int i = begin; i < end; i++)
		{
			char c = str.charAt(i);
			if (c == '.') {
				count++;
				val = 0;
				continue;
			}
			if (!Character.isDigit(c))
				return false;
			int digit = c - '0';
			val = val * 10 + digit;
			if (val > 255)
				return false;
		}
		if (count != 4 || str.charAt(end - 1) == '.')
			return false;
		return true;
	}

	private static boolean hasNumber(String str)
	{
		int length = str.length();
		for (int i = 0; i < length; i++)
			if (Character.isDigit(str.charAt(i)))
				return true;
		return false;
	}
}
