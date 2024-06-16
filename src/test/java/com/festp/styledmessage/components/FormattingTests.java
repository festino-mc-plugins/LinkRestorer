package com.festp.styledmessage.components;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.festp.styledmessage.components.Formatting;

class FormattingTests {

	@Test
	void parsesColorCode() {
		Formatting formatting = new Formatting().update(ChatColor.COLOR_CHAR + "0");
		Assertions.assertEquals(ChatColor.COLOR_CHAR + "0", formatting.getCodes());
		
		formatting = new Formatting().update(ChatColor.COLOR_CHAR + "f");
		Assertions.assertEquals(ChatColor.COLOR_CHAR + "f", formatting.getCodes());
		
		formatting = new Formatting().update(ChatColor.COLOR_CHAR + "F");
		Assertions.assertEquals(ChatColor.COLOR_CHAR + "f", formatting.getCodes());
	}

	@Test
	void parsesHexColor() {
		String hexColor = "#40A4FD";
		char c = ChatColor.COLOR_CHAR;
		String codes = c + "x" + c + "4" + c + "0" + c + "A" + c + "4" + c + "F" + c + "D";
		Formatting formatting = new Formatting().update(codes);
		Assertions.assertEquals(c + "r", formatting.getCodes());
		Assertions.assertEquals("\"color\":\"" + hexColor + "\",", formatting.getJson());
	}

	@Test
	void parsesCodeAfterHexColor() {
		char c = ChatColor.COLOR_CHAR;
		String codes = c + "x" + c + "4" + c + "0" + c + "A" + c + "4" + c + "F" + c + "D";
		Formatting formatting = new Formatting().update(codes + c + "l");
		Assertions.assertEquals(c + "r" + c + "l", formatting.getCodes());
	}
	
	@Test
	void parsesBadString() {
		Formatting formatting = new Formatting().update("" + ChatColor.COLOR_CHAR);
		Assertions.assertEquals("", formatting.getCodes());
	}

	@Test
	void rewritesColor() {
		Formatting formatting = new Formatting().update(ChatColor.COLOR_CHAR + "0");
		formatting.update(ChatColor.COLOR_CHAR + "f");
		Assertions.assertEquals(ChatColor.COLOR_CHAR + "f", formatting.getCodes());
	}

	@Test
	void resetsformatting() {
		Formatting formatting = new Formatting()
				.update(ChatColor.COLOR_CHAR + "0")
				.update(ChatColor.BOLD.toString());
		formatting.update(ChatColor.COLOR_CHAR + "r");
		Assertions.assertEquals(ChatColor.COLOR_CHAR + "r", formatting.getCodes());
	}

	@ParameterizedTest
	@ValueSource(strings = {"k", "l", "m", "n", "o"})
	void parsesformatting(String code) {
		Formatting formatting = new Formatting().update(ChatColor.COLOR_CHAR + code);
		Assertions.assertEquals(ChatColor.COLOR_CHAR + code, formatting.getCodes());
	}

	@ParameterizedTest
	@CsvSource(value = {
			"k,obfuscated",
			"l,bold",
			"m,strikethrough",
			"n,underlined",
			"o,italic"
			}, delimiter = ',')
	void jsonsformatting(String code, String jsonField) {
		String expectedJson = "\"" + jsonField + "\":\"true\",";
		Formatting formatting = new Formatting().update(ChatColor.COLOR_CHAR + code);
		Assertions.assertEquals(expectedJson, formatting.getFullJson());
	}

}
