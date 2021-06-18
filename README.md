# GriefPrevention - The self-service anti-griefing Bukkit plugin for Minecraft servers since 2011.

Stop responding to grief and prevent it instead. GriefPrevention stops grief 
before it starts automatically without any effort from administrators, 
and with very little (self service) effort from players.
Because GriefPrevention teaches players for you, you won't have to publish a
training manual or tutorial for players, or add explanatory signs to your world.

> GriefPrevention is a Bukkit plugin, therefore it is compatible with any implemention of the Bukkit API. Implementations include CraftBukkit, Spigot, Paper, Tuinity, Purpur, etc.

- [Downloads](https://dev.bukkit.org/projects/grief-prevention/files)
- [Release Notes](https://github.com/TechFortress/GriefPrevention/releases)

## Help+Support

- **[Documentation (The Manual)](https://docs.griefprevention.com)** - Check here first! This contains the list of commands, permissions, configurations, answers to common questions, etc.
- [Issue Tracker](https://github.com/TechFortress/GriefPrevention/issues) - For reporting bugs
- [Discussions](https://github.com/TechFortress/GriefPrevention/discussions) - For asking questions, suggesting new ideas/features, and general discussion

Community support and general discussion on GriefPrevention can be found at these sites:

- [GitHub Discussions](https://github.com/TechFortress/GriefPrevention/discussions)
- [#GriefPrevention chat channel on IRC or discord](https://griefprevention.com/chat)
- [Grief Prevention on dev.bukkit.org](https://dev.bukkit.org/projects/grief-prevention)
- [GriefPrevention on spigotmc.org](https://www.spigotmc.org/resources/griefprevention.1884/)

---

### Adding GriefPrevention as a maven/gradle/etc. dependency

GriefPrevention will be added to maven central soon - in the meantime, there's this neat thing called JitPack [![](https://jitpack.io/v/TechFortress/GriefPrevention.svg)](https://jitpack.io/#TechFortress/GriefPrevention) that makes a public maven repo for public Github repos on the fly.
According to it, this is all you need to do to add to your pom.xml:
```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

Replace `<version>` number with this number: [![](https://jitpack.io/v/TechFortress/GriefPrevention.svg)](https://jitpack.io/#TechFortress/GriefPrevention)
```xml
	<dependency>
	    <groupId>com.github.TechFortress</groupId>
	    <artifactId>GriefPrevention</artifactId>
	    <version>16.17.1</version>
	</dependency>
```

You can also add it to gradle/sbt/leiningen projects: https://jitpack.io/#TechFortress/GriefPrevention/

---

[![Weird flex but ok](https://bstats.org/signatures/bukkit/GriefPrevention-legacy.svg)](https://bstats.org/plugin/bukkit/GriefPrevention-legacy)
(Plugin usage stats since version 16.11 - actual use across all versions is larger)
