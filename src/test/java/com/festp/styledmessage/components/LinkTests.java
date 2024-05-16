package com.festp.styledmessage.components;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.festp.styledmessage.components.Link;

class LinkTests {
	@Test
	void getUrl_ReturnUrl() {
		String expectedUrl = "google.com";
		
		String url = new Link(expectedUrl).getUrl();
		
		Assertions.assertEquals("google.com", url);
	}
}
