package com.festp.messaging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import com.festp.styledmessage.StyledMessage;
import com.festp.styledmessage.components.Link;
import com.festp.styledmessage.components.TextStyle;
import com.google.common.collect.Lists;

class RawJsonBuilderTests {
	@ParameterizedTest
	@CsvSource(value = {
			"example.com/モエソデmoesode,example.com/%E3%83%A2%E3%82%A8%E3%82%BD%E3%83%87moesode",
			"https://www.лекарство.net,https://www.%D0%BB%D0%B5%D0%BA%D0%B0%D1%80%D1%81%D1%82%D0%B2%D0%BE.net"
			}, delimiter = ',')
	void appendJoinedLinks_ApplyUrlEncoding(String url, String expectedUrl) {
		RawJsonBuilder builder = new RawJsonBuilder(new DisplaySettings(false, false, false, "", "", ""));
		Link link = new Link(url);
		
		builder.appendJoinedLinks(Lists.newArrayList(link), new TextStyle(), "");
		String json = builder.toString();
		
		String expectedJson = "[{\"text\":\"\"},{\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + expectedUrl + "\"},\"text\":\"" + url + "\"}]";
		Assertions.assertEquals(expectedJson, json);
	}
	
	@Test
	void appendStyledMessage_EscapesQuotes() {
		RawJsonBuilder builder = new RawJsonBuilder(new DisplaySettings(false, false, false, "", "", ""));
		
		builder.appendStyledMessage(new StyledMessage("\"quotes\""));
		String json = builder.toString();
		
		String expectedJson = "[{\"text\":\"\"},{\"text\":\"\\\"quotes\\\"\"}]";
		Assertions.assertEquals(expectedJson, json);
	}
}