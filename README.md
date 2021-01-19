# GriefPrevention - The self-service anti-griefing Bukkit plugin for Minecraft servers since 2011.

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

- **[Documentation (The Manual)](https://docs.griefprevention.com)** - Where you'll find answers to common questions and information on (nearly) everything about GriefPrevention such as its range of commands and permissions, configuration parameters, etc.
- [Issue Tracker](https://github.com/TechFortress/GriefPrevention/issues) - The place to file bug reports
- [Discussions](https://github.com/TechFortress/GriefPrevention/discussions) - The place to ask questions and suggest ideas/features

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
