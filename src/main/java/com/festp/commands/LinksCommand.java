package com.festp.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import com.festp.config.Config;
import com.festp.config.LangConfig;

public class LinksCommand implements CommandExecutor, TabCompleter
{
	private static final String PERMISSION_CONFIGURE = "clickablelinks.configure";
	public static final String COMMAND = "links";
	private static final String SUBCOMMAND_RELOAD = "reload";
	private static final String SUBCOMMAND_CONFIGURE = "config";
	
	Config config;
	LangConfig lang;
	
	public LinksCommand(Config config, LangConfig lang)
	{
		this.config = config;
		this.lang = lang;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		if (!sender.hasPermission(PERMISSION_CONFIGURE)) {
			sender.sendMessage(String.format(lang.command_noPerm, PERMISSION_CONFIGURE));
			return false;
		}
		
		if (args.length == 0) {
			sender.sendMessage(lang.command_noArgs);
			return false;
		}
		if (args[0].equalsIgnoreCase(SUBCOMMAND_RELOAD))
		{
			config.load();
			lang.load();
			sender.sendMessage(lang.command_reloadOk);
			return true;
		}
		else if (args[0].equalsIgnoreCase(SUBCOMMAND_CONFIGURE))
		{
			return OnConfigure(sender, args);
		}
		
		sender.sendMessage(String.format(lang.command_arg0_error, args[0]));
		
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String lbl, String[] args)
	{
		List<String> options = new ArrayList<>();

		if (!sender.hasPermission(PERMISSION_CONFIGURE)) {
			return options;
		}
		
		if (args.length == 1)
		{
			String arg = args[0].toLowerCase();
			if (SUBCOMMAND_RELOAD.startsWith(arg))
				options.add(SUBCOMMAND_RELOAD);
			if (SUBCOMMAND_CONFIGURE.startsWith(arg))
				options.add(SUBCOMMAND_CONFIGURE);
		}
		else if (args.length >= 2)
		{
			if (args[0].equalsIgnoreCase(SUBCOMMAND_CONFIGURE))
			{
				onConfigureTabComplete(options, args);
			}
		}
		return options;
	}

	private void onConfigureTabComplete(List<String> options, String[] args) {
		String keyStr = args[1].toLowerCase();
		if (args.length == 2) {
			for (Config.Key k : Config.Key.values())
				if (k.toString().startsWith(keyStr))
					options.add(k.toString());
			
			return;
		}
		
		Config.Key key = null;
		for (Config.Key k : Config.Key.values())
			if (k.toString().equalsIgnoreCase(keyStr)) {
				key = k;
				break;
			}
		if (key == null) return;
		
		if (key.getValueClass() == Boolean.class) {
			options.add("true");
			options.add("false");
		}
		else if (key.getValueClass() == String.class) {
			String defaultValue = key.getDefault().toString();
			defaultValue = defaultValue.contains(" ") ? "\"" + defaultValue + "\"" : defaultValue;
			options.add(defaultValue);
			String emptyValue = defaultValue.contains("%s") ? "%s" : "\"\"";
			options.add(emptyValue);
		}
	}

	private boolean OnConfigure(CommandSender sender, String[] args)
	{
		String keyStr = args[1];
		Config.Key key = null;
		for (Config.Key k : Config.Key.values())
			if (k.toString().equalsIgnoreCase(keyStr)) {
				key = k;
				break;
			}
		if (key == null)
		{
			sender.sendMessage(String.format(lang.command_configure_key_error, keyStr));
			return false;
		}
		if (args.length == 2) {
			sender.sendMessage(String.format(lang.command_getOk, key.toString(), getDisplayValue(key)));
			return true;
		}
		
		Object val = null;
		if (key.getValueClass() == Boolean.class) val = tryParseBoolean(args[2]);
		else if (key.getValueClass() == String.class) val = joinQuotedArgs(args, 2);
		if (val == null) {
			sender.sendMessage(String.format(lang.command_configure_value_error, args[2]));
			return false;
		}
		
		config.set(key, val);
		sender.sendMessage(String.format(lang.command_setOk, key.toString(), getDisplayValue(key)));
		
		return true;
	}
	
	private String getDisplayValue(Config.Key key)
	{
		String strValue = config.get(key).toString();
		if (key.getValueClass() == String.class) return "\"" + strValue + "\"";
		return strValue;
	}
	
	private static String joinQuotedArgs(String[] args, int startIndex)
	{
		String stringVal = args[startIndex];
		if (!stringVal.startsWith("\""))
			return stringVal;
		
		stringVal = stringVal.substring(1);
		if (stringVal.endsWith("\"")) {
			stringVal = stringVal.substring(0, stringVal.length() - 1);
			return stringVal;
		}
		
		for (int i = startIndex + 1; i < args.length; i++)
		{
			stringVal += ' ';
			if (args[i].endsWith("\"")) {
				stringVal += args[i].substring(0, args[i].length() - 1);
				return stringVal;
			}
			stringVal += args[i];
		}
		return null;
	}

	private static Boolean tryParseBoolean(String str) {
		if (str.equalsIgnoreCase("true") || str.equalsIgnoreCase("1"))
			return true;
		if (str.equalsIgnoreCase("false") || str.equalsIgnoreCase("0"))
			return false;
		return null;
	}
}
