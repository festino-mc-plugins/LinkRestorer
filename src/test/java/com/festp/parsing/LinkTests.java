package com.festp.parsing;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.festp.styledmessage.components.Link;

class LinkTests {

	@Test
	void getsSubstring() {
		String orig = "open google.com please";
		Link link = new Link(orig, 5, 15, false);
		Assertions.assertEquals("google.com", link.getPlainText());
	}

	@Test
	void addsProtocol() {
		String orig = "open google.com please";
		Link link = new Link(orig, 5, 15, false);
		Assertions.assertEquals("https://google.com", link.getUrl());
		link = new Link(orig, 5, 15, true);
		Assertions.assertEquals("google.com", link.getUrl());
	}


	@ParameterizedTest
	@CsvSource(value = {
			"example.com/モエソデmoesode,example.com/%E3%83%A2%E3%82%A8%E3%82%BD%E3%83%87moesode",
			"https://www.лекарство.net,https://www.%D0%BB%D0%B5%D0%BA%D0%B0%D1%80%D1%81%D1%82%D0%B2%D0%BE.net"
			}, delimiter = ',')
	void appliesUrlEncoding(String text, String expectedUrl) {
		Link link = new Link(text, 0, text.length(), true);
		Assertions.assertEquals(expectedUrl, link.getUrl());
	}
	
}
