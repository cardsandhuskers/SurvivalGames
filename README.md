# SurvivalGames
Minecraft survival games and skywars plugin for 1.20.1
Survival Games follows standard rules, only breakable blocks are leaves.
Skywars lets you break and place any and all blocks.
Chest loot, game timers, and points can be edited in the config to let you change them however you want.

## Commands:
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

##Setup:
1. use the setsgpos commands to mark the corners of an arena
2. make sure to use /savesgarena to create a schematic of the area inside the corners that the game will use to reset the arena
3. use /setsgspawnbox to set each of the spawn points in the game, it's designed to support up to 12

## PlaceholderAPI Hooks:
%Survivalgames_timer% - returns current time remaining
<br>%Survivalgames_timerstage% - returns the stage of the game
<br>%Survivalgames_teamsleft% - returns the number of teams left alive
<br>%Survivalgames_playersleft% - returns the number of players left alive
<br>%Survivalgames_border% - returns the size of the border
<br>%Survivalgames_playerkills% - returns the number of kills the player has
<br>%Survivalgames_round% - returns the round number (used in skywars which has multiple rounds to a game)

### Stat Leaderboard Hooks:
%Survivalgames_playerkills_[sg or skywars]_[index]% - returns the number of single-game kills the person in that place has (single-game kills leaderboard)
<br>%Survivalgames_totalKills_[sg or skywars]_[index]% - returns the number of total kills the person in that place has (lifetime kills leaderboard)
<br>%Survivalgames_wins_[sg or skywars]_[index]% - returns the number of wins the person in that place has (lifetime wins leaderboard)


## Dependencies:
- Teams Plugin (https://github.com/cardsandhuskers/TeamsPlugin)
  - note: this must be manually set up as a local library on your machine to build this plugin
- Protocollib
- FastAsyncWorldedit
- optional: PlaceholderAPI for scoreboard placeholders