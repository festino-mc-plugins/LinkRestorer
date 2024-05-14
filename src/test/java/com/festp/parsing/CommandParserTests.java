package com.festp.parsing;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;

import com.festp.utils.CommandValidator;

class CommandParserTests extends SingleStyleSubstringHelpers
{
	@ParameterizedTest
	@ValueSource(strings = {"simple text", "a/b", ":/"})
	void parseNoCommands(String text) {
		List<SingleStyleSubstring> substrings = new CommandParser(getValidator(true)).getComponents(text);
		
		Assertions.assertEquals(1, substrings.size());
		assertPlain(substrings.get(0), 0, text.length());
	}

	@Test
	void parse_SingleCommand_OnlyCommand() {
		String command = "/help";
		String text = command;

		List<SingleStyleSubstring> substrings = new CommandParser(getValidator(true)).getComponents(text);
		
		Assertions.assertEquals(1, substrings.size());
		assertCommand(substrings.get(0), 0, command.length(), command);
	}

	@Test
	void parse_SingleCommand_DotCommandAtStart() {
		String command = "/help";
		String text = "." + command + " :)";

		List<SingleStyleSubstring> substrings = new CommandParser(getValidator(true)).getComponents(text);
		
		Assertions.assertEquals(2, substrings.size());
		assertCommand(substrings.get(0), 1, 6, command);
		assertPlain(substrings.get(1), 6, 9);
	}

	@Test
	void parse_SingleCommand_DotCommand() {
		String command = "/help";
		String text = ".." + command + " :)";

		List<SingleStyleSubstring> substrings = new CommandParser(getValidator(true)).getComponents(text);
		
		Assertions.assertEquals(4, substrings.size());
		assertPlain(substrings.get(0), 0, 1);
		assertPlain(substrings.get(1), 1, 2);
		assertCommand(substrings.get(2), 2, 7, command);
		assertPlain(substrings.get(3), 7, 10);
	}

	@Test
	void parse_SingleCommand_WordsOnEnds() {
		String command = "/help";
		String text = "use " + command + " :)";

		List<SingleStyleSubstring> substrings = new CommandParser(getValidator(true)).getComponents(text);
		
		Assertions.assertEquals(4, substrings.size());
		assertPlain(substrings.get(0), 0, 3);
		assertPlain(substrings.get(1), 3, 4);
		assertCommand(substrings.get(2), 4, 9, command);
		assertPlain(substrings.get(3), 9, 12);
	}

	@Test
	void parse_MultipleCommands() {
		String command_1 = "/command1";
		String command_2 = "/command2";
		String text = "!" + command_1 + " and " + command_2 + "!";

		List<SingleStyleSubstring> substrings = new CommandParser(getValidator(true)).getComponents(text);
		
		Assertions.assertEquals(6, substrings.size());
		assertPlain(substrings.get(0), 0, 1);
		assertCommand(substrings.get(1), 1, 10, command_1);
		assertPlain(substrings.get(2), 10, 14);
		assertPlain(substrings.get(3), 14, 15);
		assertCommand(substrings.get(4), 15, 24, command_2);
		assertPlain(substrings.get(5), 24, 25);
	}

	@Test
	void parse_FailValidation() {
		String command = "/help";
		String text = "use " + command + " :)";

		List<SingleStyleSubstring> substrings = new CommandParser(getValidator(false)).getComponents(text);

		Assertions.assertEquals(3, substrings.size());
		assertPlain(substrings.get(0), 0, 3);
		assertPlain(substrings.get(1), 3, 9);
		assertPlain(substrings.get(2), 9, 12);
	}
	
	private CommandValidator getValidator(boolean value)
	{
		CommandValidator validatorMock = Mockito.mock(CommandValidator.class);
		Mockito.when(validatorMock.commandExists(Mockito.anyString())).thenReturn(value);
		return validatorMock;
	}
}
