package com.festp.parsing;

import java.util.List;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TextStyleParserTests extends SingleStyleSubstringHelpers
{
	@Test
	void parse_Empty() {
		TextStyleParser parser = new TextStyleParser();
		
		List<SingleStyleSubstring> substrings = parser.getComponents("");

		Assertions.assertEquals(1, substrings.size());
		assertColor(substrings.get(0), 0, 0, ChatColor.RESET);
	}
	
	@Test
	void parse_NoColors() {
		String message = "hi";
		TextStyleParser parser = new TextStyleParser();
		
		List<SingleStyleSubstring> substrings = parser.getComponents(message);

		Assertions.assertEquals(1, substrings.size());
		assertColor(substrings.get(0), 0, 2, ChatColor.COLOR_CHAR + "r");
	}
	
	@Test
	void parse_Hex() {
		String c = "" + ChatColor.COLOR_CHAR;
		String hexColor = c + 'x' + c + '1' + c + '2' + c + '3' + c + '4' + c + '5' + c + '6';
		String message = hexColor + "hi";
		TextStyleParser parser = new TextStyleParser();
		
		List<SingleStyleSubstring> substrings = parser.getComponents(message);

		Assertions.assertEquals(1, substrings.size());
		assertColor(substrings.get(0), 14, 16, hexColor);
	}
	
	@Test
	void parse_MultipleColors() {
		String message = "hi, " + ChatColor.BOLD + "Player" + ChatColor.RESET + "!";
		TextStyleParser parser = new TextStyleParser();
		
		List<SingleStyleSubstring> substrings = parser.getComponents(message);

		Assertions.assertEquals(3, substrings.size());
		assertColor(substrings.get(0), 0, 4, ChatColor.RESET);
		assertColor(substrings.get(1), 6, 12, ChatColor.BOLD);
		assertColor(substrings.get(2), 14, 15, ChatColor.RESET);
	}
	
	@Test
	void parse_LastColor() {
		String message = "test" + ChatColor.RED;
		TextStyleParser parser = new TextStyleParser();
		
		List<SingleStyleSubstring> substrings = parser.getComponents(message);

		Assertions.assertEquals(2, substrings.size());
		assertColor(substrings.get(0), 0, 4, ChatColor.RESET);
		assertColor(substrings.get(1), 6, 6, ChatColor.RED);
	}
}
