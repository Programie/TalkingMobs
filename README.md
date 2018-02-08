# TalkingMobs

A Minecraft Bukkit plugin which lets mobs talk to the player.

![](screenshot.png)

[![Build Status](https://travis-ci.org/Programie/TalkingMobs.svg?branch=master)](https://travis-ci.org/Programie/TalkingMobs)
[![Issues](https://img.shields.io/github/issues/Programie/TalkingMobs.svg)](https://github.com/Programie/TalkingMobs/issues)
[![Release](https://img.shields.io/github/release/Programie/TalkingMobs.svg)](https://github.com/Programie/TalkingMobs/releases/latest)


## Installation

You can get the latest release from [bukkit.org](https://dev.bukkit.org/projects/talkingmobs) or [GitHub](https://github.com/Programie/TalkingMobs/releases/latest).

You may also check out the project from the repository and build it yourself (See Build section bellow).


## Build

You can build the project in the following 2 steps:

 * Check out the repository
 * Build the jar file using maven: *mvn clean package*

**Note:** JDK 1.8 and Maven is required to build the project!


## Permissions

TalkingMobs knows the following permissions:

 * talkingmobs - Required to access the /talkingmobs command (Default: everyone)
 * talkingmobs.receive - Allow to receive messages from mobs (Default: everyone)
 * talkingmobs.reload - Allow to reload the configuration (Default: op)
 * talkingmobs.* - Allow access to all features (Default: op)


## The /talkingmobs command

The /talkingmobs command is the one and only command provided by this plugin.

*Usage: /talkingmobs [subcommand] [arguments]*

The following sub commands are currently available:

 * help - Show the help of the plugin
 * reload - Reload the configuration
 * toggle [type] - Toggle messages sent by mobs (Type is optional and can be used to only toggle the specified event type)
