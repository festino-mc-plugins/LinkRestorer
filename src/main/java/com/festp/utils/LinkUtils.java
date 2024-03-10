package com.festp.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class LinkUtils {
	
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
}
