<img align="right" src="https://media.forgecdn.net/avatars/73/181/636163527796328705.png" height="200" width="200">

# GriefPrevention - Minecraft anti-griefing plugin [![Build Status](https://github.com/TeamWuffy/GriefPrevention/workflows/Build/badge.svg)](https://github.com/TeamWuffy/GriefPrevention/actions?query=workflow%3ABuild) [![Release Status](https://github.com/TeamWuffy/GriefPrevention/workflows/Releases/badge.svg)](https://github.com/TeamWuffy/GriefPrevention/releases/latest)

### Description
Stop responding to grief and prevent it instead. Grief Prevention will solve your grief problems without a roster of trained administrators, without 10 different anti-grief plugins, and without disabling any standard game features. Because Grief Prevention teaches players for you, you won't have to publish a training manual or tutorial for players, or add explanatory signs to your world.

Grief Prevention stops grief before it starts automatically without any effort from administrators, and with very little (self service) effort from players. Solve all your grief problems with a single anti grief download, no database, and (for most servers) no configuration customization.

### Links
- **[GriefPrevention Spigot.org](https://www.spigotmc.org/resources/griefprevention.1884/)**
- **[GriefPrevention Bukkit.org](https://dev.bukkit.org/projects/grief-prevention)**
- **[![Latest Release](https://github.com/TeamWuffy/GriefPrevention/workflows/Releases/badge.svg)](https://github.com/TeamWuffy/GriefPrevention/releases/latest)**
- **[![Latest Build](https://github.com/TeamWuffy/GriefPrevention/workflows/Build/badge.svg)](https://github.com/TeamWuffy/GriefPrevention/actions?query=workflow%3ABuild)**
- **[Wiki](https://github.com/TeamWuffy/GriefPrevention/wiki)**

### Wiki
The [GriefPrevention wiki here on GitHub](https://github.com/TeamWuffy/GriefPrevention/wiki)
contains documentation on using Grief Prevention, its range of commands and
permissions, etc.

## IMPORTANT

### Requirements:
- Java 1.8 or higher
- Spigot/PaperSpigot and any other fork of CraftBukkit! (1.15.*)

### Configurate
2. Download [Grief Prevention](https://github.com/TeamWuffy/GriefPrevention/releases/latest)
3. Restart your minecraft server
4. That was it.

## Developer

### Clone
1. Clone this repo "git clone https://github.com/TeamWuffy/GriefPrevention.git"
2. Open eclipse and right click on the "Project Explorer"
3. Click "Import..."
4. Maven -> Existing Maven Projects
5. Select the downloaded repo
6. Finished

## Build
1. Click right click on the **GriefPrevention** folder and select "Run as" -> "Maven Build..."
2. Put into Goals this "clean install compile package -Dgriefprevention-version=17.0.0"
3. Click Run
4. Your jar will be builded under the folder "target"
5. Finished

## Release a new version
1. git tag **version**
2. git push origin **version**
3. Finished

### Maven
```xml
<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
</repositories>
<dependencies>
	    <groupId>com.github.TechFortress</groupId>
	    <artifactId>GriefPrevention</artifactId>
	    <version>17.0.0</version>
	</dependency>
</dependencies>
```

### Add this to your Plugin.yml
```yml
depend: [GriefPrevention]
loadbefore: [GriefPrevention]
```

## Stats
[![Weird flex but ok](https://bstats.org/signatures/bukkit/GriefPrevention-legacy.svg)](https://bstats.org/plugin/bukkit/GriefPrevention-legacy)