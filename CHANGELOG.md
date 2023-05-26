# Changelog

## 1.4 (2018-03-30)

Send message if a mob gets killed by something else than a player (e.g. by another mob or plugin).

**Note:** The `killed` event type is now called `killed_player`. The old one now exists as a short hand in the messages configuration to define a message for both event types (player and other). From now on, use `killed_player` in the `/talkingmobs toggle` command and for the global events configuration in the `config.yml`.

## 1.3 (2018-02-13)

* Added "tamed" event showing a message once a mob is tamed
* Added "version" sub command
* Prevent spamming of messages (e.g. while attacking mobs or interacting with them)
* Send messages if a mob is looking at the player (and the player is looking at the mob)
* **BREAKING CHANGE**: Moved messages and per player configuration to separate files - Mob messages are now configured in `messages.yml`, per player configuration is saved in `players.yml`

## 1.2 (2018-02-01)

Allow to configure which events should show a message (attacked, interacted, killed, spawned). The "spawned" event requires a list of spawn reasons for which a message should be shown.

## 1.1 (2018-01-29)

* Support for Bukkit 1.12 (not sure whether it will still work with older Bukkit versions as well)
* Only react to events triggered by real mobs (e.g. minecarts or other players are not handled as mobs)

## 1.0 (2018-01-29)

Initial release