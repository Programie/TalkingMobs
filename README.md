# TalkingMobs

A Minecraft Bukkit plugin which lets mobs talk to the player.

![](screenshot.png)

[![Build Status](https://travis-ci.org/Programie/TalkingMobs.svg?branch=master)](https://travis-ci.org/Programie/TalkingMobs)
[![Issues](https://img.shields.io/github/issues/Programie/TalkingMobs.svg)](https://github.com/Programie/TalkingMobs/issues)
[![Release](https://img.shields.io/github/release/Programie/TalkingMobs.svg)](https://github.com/Programie/TalkingMobs/releases/latest)


## Installation

You can get the latest release from [bukkit.org](https://dev.bukkit.org/projects/talkingmobs) or [GitHub](https://github.com/Programie/TalkingMobs/releases/latest).

You may also check out the project from the repository and build it yourself (See Build section bellow).


## Custom messages

All messages can be customized in the [messages.yml](src/main/resources/messages.yml) file.


## Permissions

TalkingMobs knows the following permissions:

* `talkingmobs` - Required to access the /talkingmobs command (Default: everyone)
* `talkingmobs.receive` - Allow to receive messages from mobs (Default: everyone)
* `talkingmobs.reload` - Allow to reload the configuration (Default: op)
* `talkingmobs.*` - Allow access to all features (Default: op)


## Commands

The `/talkingmobs` command is the one and only command provided by this plugin.

**Usage:** `/talkingmobs [subcommand] [arguments]`

The following sub commands are currently available:

* `help` - Show the help of the plugin
* `reload` - Reload the configuration
* `toggle` - Toggle messages sent by mobs
* `version` - Show the version of the plugin


## Event types

The following event types are currently available and can be used for the type in the `/talkingmobs toggle <type>` command and in the configuration files.

* `attacked` - Mob has been attacked by a player
* `idle` - Mob is idle, for example the mob is just standing/walking/running around (Currently unused)
* `interacted` - Player interacted with the mob (Right click on mob)
* `killed` - Mob has been killed by a player
* `looking` - Mob is looking at the player (and player is looking at the mob)
* `spawn` - Mob has been spawned (Mob spawner, egg or another plugin by default, but can be configured in config.yml)
* `tamed` - Mob has been tamed


## Build

You can build the project in the following 2 steps:

 * Check out the repository
 * Build the jar file using maven: *mvn clean package*

**Note:** JDK 1.7 and Maven is required to build the project!