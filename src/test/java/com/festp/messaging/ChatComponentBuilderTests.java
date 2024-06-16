package com.festp.messaging;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.festp.styledmessage.SingleStyleMessage;
import com.festp.styledmessage.StyledMessage;
import com.festp.styledmessage.attributes.Command;
import com.festp.styledmessage.attributes.Formatting;
import com.festp.styledmessage.attributes.Link;
import com.google.common.collect.Lists;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;

class ChatComponentBuilderTests
{
	@ParameterizedTest
	@CsvSource(value = {
			"example.com/モエソデmoesode,example.com/%E3%83%A2%E3%82%A8%E3%82%BD%E3%83%87moesode",
			"https://www.лекарство.net,https://www.%D0%BB%D0%B5%D0%BA%D0%B0%D1%80%D1%81%D1%82%D0%B2%D0%BE.net"
			}, delimiter = ',')
	void appendJoinedLinks_ApplyUrlEncoding(String url, String expectedUrl) {
		ChatComponentBuilder builder = new ChatComponentBuilder(new DisplaySettings("%s", "%s", "%s", "", "", "", false));
		Link link = new Link(url);
		
		builder.appendJoinedLinks(Lists.newArrayList(link), new Formatting(), "");
		BaseComponent components = builder.build();
		
		String expectedJson = "[{\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + expectedUrl + "\"},\"text\":\"" + url + "\"}]";
		assertEquals(expectedJson, components);
	}
	
	@Test
	void appendStyledMessage_EscapesQuotesAndCackSlashes() {
		ChatComponentBuilder builder = new ChatComponentBuilder(new DisplaySettings("%s", "%s", "%s", "", "", "", false));
		
		builder.appendStyledMessage(new StyledMessage("\\ and \""));
		BaseComponent components = builder.build();
		
		String expectedJson = "[{\"text\":\"\\\\ and \\\"\"}]";
		assertEquals(expectedJson, components);
	}
	
	@Test
	void appendStyledMessage_EscapesQuotesAndCackSlashes_LinkUrl() {
		ChatComponentBuilder builder = new ChatComponentBuilder(new DisplaySettings("%s", "%s", "%s", "", "", "", false));
		
		SingleStyleMessage styledPart = new SingleStyleMessage("a", Lists.newArrayList(new Link("\\\"")));
		builder.appendStyledMessage(new StyledMessage(Lists.newArrayList(styledPart)));
		BaseComponent components = builder.build();

		String expectedUrl = "\\\\\\\""; // not "\\\\%22"
		String expectedJson = "[{\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + expectedUrl + "\"},\"text\":\"a\"}]";
		assertEquals(expectedJson, components);
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	void appendStyledMessage_CommandAction(boolean runCommands) {
		String action = runCommands ? "run_command" : "suggest_command";
		ChatComponentBuilder builder = new ChatComponentBuilder(new DisplaySettings("%s", "%s", "%s", "", "", "", runCommands));
		String cmd = "/ping";
		Command command = new Command(cmd);
		
		builder.appendStyledMessage(new StyledMessage(Lists.newArrayList(new SingleStyleMessage(cmd, Lists.newArrayList(command)))));
		BaseComponent components = builder.build();
		
		String expectedJson = "[{\"clickEvent\":{\"action\":\"" + action + "\",\"value\":\"" + cmd + "\"},\"text\":\"" + cmd + "\"}]";
		assertEquals(expectedJson, components);
	}
	
	private void assertEquals(String expectedInnerRawJson, BaseComponent component) {
		String json = ComponentSerializer.toString(component);
		Assertions.assertEquals("{\"extra\":" + expectedInnerRawJson + ",\"text\":\"\"}", json);
	}
}