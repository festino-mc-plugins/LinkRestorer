package com.festp.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

public class LinkUtils {

	// TODO check telegram desktop code
	// TextWithTags Ui::InputField::getTextWithTags, getTextWithTagsPart, getTextPart...
	
	// TODO? prevent using both latin and cyrillic letters
	private static String LINK_REGEX = "(((?:https?):\\/\\/)?(?:(?:[-a-z0-9_à-ÿÀ-ß]{1,}\\.){1,}([a-z0-9à-ÿÀ-ß]{1,}).*?(?=[\\.\\?!,;:]?(?:[" + String.valueOf(org.bukkit.ChatColor.COLOR_CHAR) + " \\n]|$))))";
	private static Pattern PATTERN = Pattern.compile(LINK_REGEX, Pattern.CASE_INSENSITIVE);
	private static int SCHEME_GROUP_INDEX = 2;
	private static int TLD_GROUP_INDEX = 3;

	public static String applyBrowserEncoding(String str)
	{
		try {
			HashMap<Character, String> encoding = new HashMap<>();
			int length = str.length();
			for (int i = 0; i < length; i++)
			{
				char c = str.charAt(i);
				if (encoding.containsKey(c))
					continue;
				// not very optimized?
				String encoded = URLEncoder.encode("" + c, StandardCharsets.UTF_8.toString());
				encoding.put(c, encoded);
			}

			StringBuilder res = new StringBuilder();
			for (int i = 0; i < length; i++)
			{
				char c = str.charAt(i);
				if (c <= 255)
					res.append(c);
				else
					res.append(encoding.get(c));
			}
			
			return res.toString();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return str;
		}
	}
	
	/** @return <b>null</b> if no links found */
	public static Iterable<Link> findLinks(String message)
	{
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
			
			int linkStart = matcher.start();
			if (linkStart > 0 && message.charAt(linkStart - 1) == ChatColor.COLOR_CHAR)
				linkStart++;

			boolean hasProtocol = matcher.end(SCHEME_GROUP_INDEX) - matcher.start(SCHEME_GROUP_INDEX) > 0;
			Link link = new Link(message, linkStart, matcher.end(), hasProtocol);
			links.add(link);
		}
		
		if (links.size() == 0)
			return null;
		
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
