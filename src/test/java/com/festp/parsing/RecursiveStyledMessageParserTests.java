package com.festp.parsing;

import org.bukkit.ChatColor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.festp.styledmessage.StyledMessage;

class RecursiveStyledMessageParserTests {

	@Test
	void parsesPlainText() {
		String message = "hi";
		StyledMessage styledMessage = new TextStyleParser().getComponents(message);
		Assertions.assertTrue(styledMessage.isPlain);
		Assertions.assertFalse(styledMessage.hasLinks);
		Assertions.assertEquals(message, styledMessage.plainText);
		Assertions.assertEquals(0, styledMessage.links.size());
		Assertions.assertEquals(0, styledMessage.styleSwitches.size());
	}

	@Test
	void parsesColors() {
		String message = ChatColor.GRAY + "hi, " + ChatColor.WHITE + "Player";
		StyledMessage styledMessage = new RecursiveStyledMessageParser().parse(message);
		Assertions.assertFalse(styledMessage.isPlain);
		Assertions.assertEquals("hi, Player", styledMessage.plainText);
		Assertions.assertEquals(2, styledMessage.styleSwitches.size());
		Assertions.assertEquals(0, styledMessage.styleSwitches.get(0).index);
		Assertions.assertEquals(4, styledMessage.styleSwitches.get(1).index);
	}

	@Test
	void parsesLinks() {
		String message = "see example.com";
		StyledMessage styledMessage = new RecursiveStyledMessageParser().parse(message);
		Assertions.assertFalse(styledMessage.isPlain);
		Assertions.assertTrue(styledMessage.hasLinks);
		Assertions.assertEquals(message, styledMessage.plainText);
		Assertions.assertEquals(1, styledMessage.links.size());
		Assertions.assertEquals("example.com", styledMessage.links.get(0).getText());
	}
}
