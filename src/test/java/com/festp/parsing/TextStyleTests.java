package com.festp.parsing;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TextStyleTests {

	@Test
	void parsesColorCode() {
		TextStyle style = new TextStyle().update(ChatColor.COLOR_CHAR + "0");
		Assertions.assertEquals(ChatColor.COLOR_CHAR + "0", style.getCodes());
		
		style = new TextStyle().update(ChatColor.COLOR_CHAR + "f");
		Assertions.assertEquals(ChatColor.COLOR_CHAR + "f", style.getCodes());
		
		style = new TextStyle().update(ChatColor.COLOR_CHAR + "F");
		Assertions.assertEquals(ChatColor.COLOR_CHAR + "f", style.getCodes());
	}

	@Test
	void parsesHexColor() {
		String hexColor = "#40A4FD";
		char c = ChatColor.COLOR_CHAR;
		String codes = c + "x" + c + "4" + c + "0" + c + "A" + c + "4" + c + "F" + c + "D";
		TextStyle style = new TextStyle().update(codes);
		Assertions.assertEquals("", style.getCodes());
		Assertions.assertEquals("\"color\":\"" + hexColor + "\",", style.getJson());
	}
	
	@Test
	void parsesBadString() {
		TextStyle style = new TextStyle().update("" + ChatColor.COLOR_CHAR);
		Assertions.assertEquals("", style.getCodes());
	}

	@Test
	void rewritesColor() {
		TextStyle style = new TextStyle().update(ChatColor.COLOR_CHAR + "0");
		style.update(ChatColor.COLOR_CHAR + "f");
		Assertions.assertEquals(ChatColor.COLOR_CHAR + "f", style.getCodes());
	}

	@Test
	void resetsStyle() {
		TextStyle style = new TextStyle()
				.update(ChatColor.COLOR_CHAR + "0")
				.update(ChatColor.BOLD.toString());
		style.update(ChatColor.COLOR_CHAR + "r");
		Assertions.assertEquals(ChatColor.COLOR_CHAR + "r", style.getCodes());
	}

	@ParameterizedTest
	@ValueSource(strings = {"k", "l", "m", "n", "o"})
	void parsesStyle(String code) {
		TextStyle style = new TextStyle().update(ChatColor.COLOR_CHAR + code);
		Assertions.assertEquals(ChatColor.COLOR_CHAR + code, style.getCodes());
	}

	@ParameterizedTest
	@CsvSource(value = {
			"k,obfuscated",
			"l,bold",
			"m,strikethrough",
			"n,underlined",
			"o,italic"
			}, delimiter = ',')
	void jsonsStyle(String code, String jsonField) {
		String expectedJson = "\"" + jsonField + "\":\"true\",";
		TextStyle style = new TextStyle().update(ChatColor.COLOR_CHAR + code);
		Assertions.assertEquals(expectedJson, style.getFullJson());
	}

}
