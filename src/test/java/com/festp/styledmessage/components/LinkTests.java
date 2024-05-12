package com.festp.styledmessage.components;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.festp.styledmessage.components.Link;

class LinkTests {
	@Test
	void getUrl_ReturnUrl() {
		String expectedUrl = "google.com";
		
		String url = new Link(expectedUrl).getUrl();
		
		Assertions.assertEquals("google.com", url);
	}


	@ParameterizedTest
	@CsvSource(value = {
			"example.com/モエソデmoesode,example.com/%E3%83%A2%E3%82%A8%E3%82%BD%E3%83%87moesode",
			"https://www.лекарство.net,https://www.%D0%BB%D0%B5%D0%BA%D0%B0%D1%80%D1%81%D1%82%D0%B2%D0%BE.net"
			}, delimiter = ',')
	void appliesUrlEncoding(String text, String expectedUrl) {
		String url = new Link(text).getUrl();
		
		Assertions.assertEquals(expectedUrl, url);
	}
	
}
