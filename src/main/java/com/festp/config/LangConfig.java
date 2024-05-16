package com.festp.config;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LangConfig
{
	private File configFile;
	
	private static String KEY_COMMAND_NO_PERM = "command-no-perm";
	public String command_noPerm = ChatColor.RED + "You must be an operator or have " + ChatColor.WHITE + "%s" + ChatColor.RED + " permission to perform this command.";
	private static String KEY_COMMAND_NO_ARGS = "command-no-args";
	public String command_noArgs = ChatColor.GRAY + "Usage: \n    /links <option> [true|false]\n    /links reload";
	private static String KEY_COMMAND_ARG0_ERROR = "command-arg0-error";
	public String command_arg0_error = ChatColor.RED + "\"%s\" is an invalid option. Please follow tab-completion.";
	private static String KEY_COMMAND_CONFIGURE_KEY_ERROR = "command-configure-key-error";
	public String command_configure_key_error = ChatColor.RED + "\"%s\" is an invalid option. Please follow tab-completion.";;
	private static String KEY_COMMAND_CONFIGURE_VALUE_ERROR = "command-arg1-error";
	public String command_configure_value_error = ChatColor.RED + "\"%s\" is an invalid value. Please follow tab-completion.";
	private static String KEY_COMMAND_GET_OK = "command-get-ok";
	public String command_getOk = ChatColor.GREEN + "Option %s is equal to %s.";
	private static String KEY_COMMAND_SET_OK = "command-set-ok";
	public String command_setOk = ChatColor.GREEN + "Option %s was set to %s.";
	private static String KEY_COMMAND_RELOAD_OK = "command-reload-ok";
	public String command_reloadOk = ChatColor.GREEN + "Config reloaded.";
	private static String KEY_CONFIG_RELOAD = "config-reload";
	public String config_reload = "Config reloaded.";
	private static String KEY_CONFIG_SAVE = "config-save";
	public String config_save = "Config successfully saved.";
	
	public LangConfig(File configFile)
	{
		this.configFile = configFile;
	}
	
	public void load()
	{
		LangConfig defaults = new LangConfig(configFile);
		FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		command_noPerm = config.getString(KEY_COMMAND_NO_PERM, defaults.command_noPerm);
		command_noArgs = config.getString(KEY_COMMAND_NO_ARGS, defaults.command_noArgs);
		command_arg0_error = config.getString(KEY_COMMAND_ARG0_ERROR, defaults.command_arg0_error);
		command_configure_key_error = config.getString(KEY_COMMAND_CONFIGURE_KEY_ERROR, defaults.command_configure_key_error);
		command_configure_value_error = config.getString(KEY_COMMAND_CONFIGURE_VALUE_ERROR, defaults.command_configure_value_error);
		command_getOk = config.getString(KEY_COMMAND_GET_OK, defaults.command_getOk);
		command_setOk = config.getString(KEY_COMMAND_SET_OK, defaults.command_setOk);
		command_reloadOk = config.getString(KEY_COMMAND_RELOAD_OK, defaults.command_reloadOk);
		config_reload = config.getString(KEY_CONFIG_RELOAD, defaults.config_reload);
		config_save = config.getString(KEY_CONFIG_SAVE, defaults.config_save);
		save();
	}
	
	public void save()
	{
		FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		config.set(KEY_COMMAND_NO_PERM, command_noPerm);
		config.set(KEY_COMMAND_NO_ARGS, command_noArgs);
		config.set(KEY_COMMAND_ARG0_ERROR, command_arg0_error);
		config.set(KEY_COMMAND_CONFIGURE_KEY_ERROR, command_configure_key_error);
		config.set(KEY_COMMAND_CONFIGURE_VALUE_ERROR, command_configure_value_error);
		config.set(KEY_COMMAND_GET_OK, command_getOk);
		config.set(KEY_COMMAND_SET_OK, command_setOk);
		config.set(KEY_COMMAND_RELOAD_OK, command_reloadOk);
		config.set(KEY_CONFIG_RELOAD, config_reload);
		config.set(KEY_CONFIG_SAVE, config_save);
		try {
			config.save(configFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
