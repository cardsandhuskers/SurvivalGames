# SurvivalGames
Minecraft survival games and skywars plugin for 1.19.x
Survival Games follows standard rules, only breakable blocks are leaves.
Skywars lets you break and place any and all blocks.
Chest loot, game timers, and points can be edited in the config to let you change them however you want.

# Commands:
note: GameType refers to either SURVIVAL_GAMES or SKYWARS, this lets you choose which game to execute the command for.

**/startsurvivalgames [multiplier] [GameType]** 
- Starts the GameType with a multiplier, which can be any double.

**/setsgpos1 [GameType]**
- Sets pos1 at player's location, should be 1 corner of the map (doesn't matter which one)

**/setsgpos1 [GameType]**
- Sets pos2 at player's location, should be the opposite corner from pos1 (on all 3 axes)

**/setlobby**
- set the lobby to return players to after the game concludes

**/setsgspawnpoint [GameType]**
- set the in-world spawn point for the game, i.e. the game specific lobby point

**/setsgspawnbox [number] [GameType]**
- sets the location of a team spawn point (spawn pad/spawnpoint on skywars island) to where the player is standing
- number represents island/platform number

**/reloadsgarena [GameType]**
- places in the saved schematic to regen the game map
- this process happens automatically at the beginning of both games

**/savesgarena [GameType]**
- saves a schematic of the arena, which is the area bounded by the pos1 and pos2

# Dependencies:
- Teams Plugin (https://github.com/cardsandhuskers/TeamsPlugin)
- PlaceholderAPI for scoreboard placeholders
- Protocollib
- FastAsyncWorldedit
