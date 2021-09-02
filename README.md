<h1 align="center">DashDynamicDaemon</h1>

<h4 align="center">DashDynamicDaemon: A Sharded Minecraft Server hosting system.</h4>

<p align="center">
  <a href="https://twitter.com/BattleDashBR"><img src="https://img.shields.io/badge/Twitter-@BattleDashBR-1da1f2.svg?logo=twitter"></a>
</p>

------

## Purpose

This system was originally created to host automatically scalable Minigame and Lobby servers for the [Enlighten](https://enlightenmc.net) Minecraft Network.
It relies on certain proprietary Network Control systems and a plugin that connects to the daemon's socket, so if you want to actually use this you will need to recreate those yourself.

I'm putting a modified version of this on GitHub as an example of my recent work, but you are completely free to use this yourself as well.

## How it Works

Pretend you have 4 dedicated servers.

Server 1 is running Network Control, and the other 3 are running Dynamic Daemon nodes.

Server 1 is responsible for telling the nodes what to do, and when, and the nodes are responsible for creating and keeping track of the game servers themselves.

Servers and running nodes are all stored in MongoDB, which is queried by Network Control for matchmaking and load balancing.


## How Server Types are stored

Network Control has a directory containing server binaries, as well as Game Types containing plugins and pre-set server configurations.

Every minute Network Control generates binaries using that data, and stores them in-memory, to be easily distributed to the daemon nodes (this program).

The daemon stores the archives in a cache folder. Every minute the daemon will make a request to Network Control asking for hashes of its archives. If it contains mismatched hashes, we download/update/delete our caches archives as necessary.

## Join Ticketing

DashDynamicDaemon contains a W.I.P. Join Ticketing system. The structure is as follows:

Network Control finds the server it wants to send someone to, and asks the Node the server is running on for a join ticket.
The Join Ticket Request contains the player ID, and any other data that may go along with it, like a world name. This data is
then passed on to the game server itself, which validates the request's data (like a world name), and checks if the player should
be able to join (so that we don't try to send a player to a full server, or an already-started game).

## Logging

For Logging, we're using Log4j2, with almost the same setup that Minecraft servers use. A logs folder is created, with the latest log file and archives of past logs.

## Closing

Feel free to have a look around the code, and pull requests are always welcome.

Common Code Acronyms:<br>
NCP - Network Control Program