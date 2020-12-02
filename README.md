# GriefPrevention - Minecraft anti-griefing plugin

Stop responding to grief and prevent it instead. Grief Prevention will solve
your grief problems without a roster of trained administrators, without 10
different anti-grief plugins, and without disabling any standard game features.
Because Grief Prevention teaches players for you, you won't have to publish a
training manual or tutorial for players, or add explanatory signs to your world.

Grief Prevention stops grief before it starts automatically without any effort
from administrators, and with very little (self service) effort from players.
Solve all your grief problems with a single anti grief download, no database,
and (for most servers) no configuration customization.

## Downloads

- [Downloads](https://dev.bukkit.org/projects/grief-prevention/files)
- [Release Notes](https://github.com/TechFortress/GriefPrevention/releases)

## Help+Support

- **[Documentation](https://docs.griefprevention.com)** - Information on using Grief Prevention, its range of commands and permissions, configuration parameters, etc.
- [FAQ](https://github.com/TechFortress/GriefPrevention/issues/752) - Frequently Asked Questions about GriefPrevention.
- [Issue Tracker (GitHub Issues)](https://github.com/TechFortress/GriefPrevention/issues) - The place to file bug reports or request features.

### Community

Discussion and community support for GriefPrevention can be found at these sites:

[#GriefPrevention IRC channel on irc.spi.gt](https://griefprevention.com/chat) (dumcord also available)

[Grief Prevention on dev.bukkit.org](https://dev.bukkit.org/projects/grief-prevention)

[GriefPrevention on spigotmc.org](https://www.spigotmc.org/resources/griefprevention.1884/)

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
	    <version>16.7.1</version>
	</dependency>
```

You can also add it to gradle/sbt/leiningen projects: https://jitpack.io/#TechFortress/GriefPrevention/

---

[![Weird flex but ok](https://bstats.org/signatures/bukkit/GriefPrevention-legacy.svg)](https://bstats.org/plugin/bukkit/GriefPrevention-legacy)
(Plugin usage stats since version 16.11 - actual use across all versions is larger)
