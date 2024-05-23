# ClickableLinks
[![Tests](https://github.com/festino-mc-plugins/ClickableLinks/actions/workflows/main.yml/badge.svg)](https://github.com/festino-mc-plugins/ClickableLinks/actions/workflows/main.yml)  
Other pages: [Spigot](https://www.spigotmc.org/resources/clickablelinks.105786/)  

Bring back links to Minecraft chat.

## Why ClickableLinks?

Bukkit removed this feature in 1.19.1 due to [vanilla changes](https://www.minecraft.net/ru-ru/article/minecraft-1-19-1-pre-release-6).

Also, the original [Bukkit code](https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/src/main/java/org/bukkit/craftbukkit/util/CraftChatMessage.java) has some issues: no URL-escaping, only latin letters in domains, invalid IPv4, TLD of length 2-4 (no .museum or .vodka). These issues probably were inherited from the [vanilla parser 1.5.x-1.6.x](https://bugs.mojang.com/browse/MC-18898).  
  
## Features

Check [the wiki](https://github.com/festino-mc-plugins/ClickableLinks/wiki) for details.

The plugin provides clickable links, clickable/copyable commands and copyable text. Examples:  
* [Links](https://github.com/festino-mc-plugins/ClickableLinks/wiki/Clickable-links)  
Dup glitch!! <span>https://</span><span>www</span>.youtube.com/watch?v=dQw4w9WgXcQ
* [Commands](https://github.com/festino-mc-plugins/ClickableLinks/wiki/Clickable-commands) (checks whether such a command is registered)  
use /help  
./ping  
,,/say test,,
* [Copyable text](https://github.com/festino-mc-plugins/ClickableLinks/wiki/Copyable-text)  
,,copy this,,

Dynamic configuration is provided using the **/links** command.  
You can disable some features if you don't need them. You can also disable features using the corresponding permissions.  
