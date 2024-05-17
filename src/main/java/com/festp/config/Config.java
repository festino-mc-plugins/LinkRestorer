package com.festp.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.festp.Logger;
import com.festp.messaging.DisplaySettings;
import com.festp.utils.FileUtils;

public class Config implements IConfig
{
	private JavaPlugin plugin;
	private LangConfig lang;
	private MemoryConfiguration config;
	private final HashMap<String, Object> map = new HashMap<>();
	
	public Config(JavaPlugin jp, LangConfig lang) {
		this.plugin = jp;
		this.lang = lang;
	}
	
	public DisplaySettings getDisplaySettings() {
		return new DisplaySettings(
				get(Key.UNDERLINE_LINKS), get(Key.UNDERLINE_COMMANDS), get(Key.UNDERLINE_COPYABLE_TEXT),
				get(Key.TOOLTIP_LINKS), get(Key.TOOLTIP_COMMANDS), get(Key.TOOLTIP_COPYABLE_TEXT));
	}
	
	public void load() {
		File configFile = new File(plugin.getDataFolder(), "config.yml");
		if (!configFile.exists())
			FileUtils.copyFileFromResource(configFile, "config.yml");
		plugin.reloadConfig();
		config = plugin.getConfig();
		map.putAll(config.getValues(true));
		saveSilently();
		Logger.info(lang.config_reload);
	}

	public void save() {
		saveSilently();
		Logger.info(lang.config_save);
	}
	public void saveSilently() {
		for (Key key : Key.values()) {
			config.set(key.name, get(key));
		}
		plugin.saveConfig();
	}
	
	public void set(Key key, Object value) {
		map.put(key.toString(), value);
		save();
	}
	
	
	
	@SuppressWarnings("unchecked")
	public <T extends Object> T get(Key key, T defaultValue) {
		applyDefault(key, defaultValue);
		
		Class<?> clazz;
		if (defaultValue != null) {
			clazz = defaultValue.getClass();
		} else {
			clazz = key.getDefault().getClass();
		}
		
		Object res = map.getOrDefault(key.toString(), defaultValue);
		if (clazz.isInstance(res)) {
			return (T) res;
		}
		return defaultValue;
	}
	public <T extends Object> T get(Key key) {
		return get(key, null);
	}
	
	

	@Override
	public Object getObject(IConfig.Key key, Object defaultValue) {
		return map.getOrDefault(key.toString(), defaultValue);
	}

	@Override
	public Object getObject(IConfig.Key key) {
		return getObject(key, key.getDefault());
	}
	
	
	
	private void applyDefault(String key, Object defaultValue) {
		if (!map.containsKey(key)) {
			map.put(key, defaultValue);
		}
	}
	private void applyDefault(Key key, Object defaultValue) {
		if (defaultValue == null)
			applyDefault(key);
		else
			applyDefault(key.toString(), defaultValue);
	}
	private void applyDefault(Key key) {
		applyDefault(key.toString(), key.getDefault());
	}
	
	
	
	public enum Key implements IConfig.Key {
		LOG_DEBUG("log-debug-info", false),
		
		ENABLE_LINKS("enable-links", true),
		ENABLE_COMMANDS("enable-commands", true),
		ENABLE_COPYABLE_TEXT("enable-copyable-text", true),
		
		UNDERLINE_LINKS("underline-links", true),
		UNDERLINE_COMMANDS("underline-commands", true),
		UNDERLINE_COPYABLE_TEXT("underline-copyable-text", true),

		TOOLTIP_LINKS("tooltip-links", ""),
		TOOLTIP_COMMANDS("tooltip-commands", "Copy command"),
		TOOLTIP_COPYABLE_TEXT("tooltip-copyable-text", "Copy text"),
		
		COMMANDS_REMOVE_STARTING_DOT("commands-remove-starting-dot", true),
		
		COPYABLE_TEXT_BEGIN_QUOTES("copyable-text-begin-quotes", ",,"),
		COPYABLE_TEXT_END_QUOTES("copyable-text-end-quotes", ",,"),
		COPYABLE_TEXT_ONLY_COMMANDS("copyable-text-only-commands", false),
		
		LISTEN_TO_WHISPER("do-whisper", true),
		WHISPER_SEPARATE_MESSAGE("whisper-separate-message", false);
		
		private final String name;
		private final Object defaultValue;

		Key(String name, Object defaultValue) {
			this.name = name;
			this.defaultValue = defaultValue;
		}
		public Object getDefault() { return defaultValue; }
		@Override
		public String toString() { return name; }
		
		public Object validateValue(String valueStr) {
			try {
				if (defaultValue instanceof Boolean) {
					return Boolean.parseBoolean(valueStr);
				}
				if (defaultValue instanceof Integer) {
					return Integer.parseInt(valueStr);
				}
				if (defaultValue instanceof Double) {
					return Double.parseDouble(valueStr);
				}
				if (defaultValue instanceof String) {
					return valueStr;
				}
			} catch (Exception e) {}
			return null;
		}
		
		public Class<?> getValueClass() {
			if (defaultValue instanceof Boolean) {
				return Boolean.class;
			}
			if (defaultValue instanceof Integer) {
				return Integer.class;
			}
			if (defaultValue instanceof Double) {
				return Double.class;
			}
			if (defaultValue instanceof String) {
				return String.class;
			}
			return null;
		}
		
		public static boolean isValidKey(String keyStr) {
			return getKey(keyStr) != null;
		}
		
		public static Key getKey(String keyStr) {
			for (Key key : Key.values())
				if (key.name.equalsIgnoreCase(keyStr))
					return key;
			return null;
		}
		
		public static List<String> getKeys() {
			List<String> keys = new ArrayList<>();
			for (Key key : Key.values()) {
				keys.add(key.name);
			}
			return keys;
		}
	}
}
