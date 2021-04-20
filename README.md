# MC-Slice
Conversion tool for 21w15a, making heavy use of the Java [NBT library](https://github.com/Querz/NBT).

[Do I Need to Use This?](#do-i-need-to-use-this)

[How Does It Work?](#how-does-it-work)

[Usage](#usage)

[Changelog](#changelog)

## Do I Need to Use This?
-------------------------
This application is intended for a very specific use case.

If you have a Minecraft world created before version 21w06a (with the old 256-block tall world generation) that you upgraded to a snapshot after 21w06a and before 21w15a (against Mojang's advice) to get the new 384-block tall world generation, this tool can help you convert the world back to the old-style generation, enabling the world to be used again in snapshot 21w15a and beyond without the use of Mojang's CavesAndCliffsPreview datapack (which, in my experience, causes some instability).

Opening a world with this extended height in 21w15a without running this tool on it will result in the game failing to load chunks, effectively deleting everything. (The game doesn't allow worlds generated between 06a and 14a to be loaded in versions past 15a, but by modifying `level.dat`, its possible.)

I have a Minecraft world that I've played on for many years (since 2013), and it's been through many snapshot cycles. 

When 21w06a came out, it introduced the new cave generation and world height, but Mojang locked the ability to load pre-existing worlds in that snapshot, since there was no proper conversion method. The world played fine when I forced the upgrade, allowing for building below 0 and above 255, and even generation of new chunks.

When Mojang announced the 1.17 delay, and rolled back world generation to the old 256-block tall style in 21w15a, I knew I was in trouble. Mojang released a datapack that they intended for people to use on newly-created worlds, not existing ones that enabled the extended world generation. This datapack allowed me to continue playing on my world, but it lead to a lot of instability. Biome data would get corrupted, random crashes would occur. 

This datapack was not a good solution to the problem I created by doing the exact opposite of what Mojang wanted, and I knew the only way forward was to revert to the old generation.

I had 2 options on how to revert the world back to 256-blocks tall:
    - Rollback my world to the backup I took before 21w06a, loosing over 2 months of building.
    - Find a way to convert the world.

Since this is a rather unique problem, there weren't any tools already out there to fix my problem, and rolling back to an old backup was simply not an option, so I spent an afternoon creating this tool.

You can also run this tool on a world that was created with the new 384-block generation, so that it can be opened in 21w15a and beyond, but I'm not sure why you'd want to do that, since it would delete all the blocks below Y=0, leaving you with no bedrock layer and many holes into the void. 

## How Does It Work?
--------------------
TL;DR: The tool goes through every chunk, removing terrain below Y=0 and above Y=255, effectively returning it to normal size.

The tool will read all of the overworld region files, and begin making modifications to all of the chunks contained within.

In the old 256-block generation, biome data for a chunk is stored as an array of 1024 biome IDs, starting from the bottom of the world.

In the new 384-block generation, the array is lengthened to 1536 values, meaning we can strip out the last 512 values and be left with the original 1024 values for that chunk.

Chunks are 16x16 areas in the world that extend across the entire vertical height, and are divided into 16-block tall subchunks.

In the old 256-block generation, these subchunks are indexed 0 - 15.

The new 384-block generation adds 8 subchunks to each chunk (4 on the bottom, 4 on the top).

The new subchunks carry the indexes -4 - -1 and 16 - 19, meaning we can simply remove those and be left with the original terrain from Y=0 to Y=256.

Finally, the tool modifies `level.dat`'s `Version` tag, enabling Minecraft to load the world in 21w15a. It also removes the experimental preview datapack Mojang released, if it's found in that world's datapack directory. 

## Usage
--------
1. Download the latest release from the [Releases](https://www.github.com/the404devs/mc-slice/releases) page.
2. **Take a backup of the world you're planning on using this tool on.**
    - This step may save you a lot of tears if something goes wrong.
    - You can do this from within the game itself.
        - `Singleplayer` -> `WorldYouWantToUse` -> `Edit` -> `Make Backup`
3. Move the `mc-slice-vX.X.X.jar` file to your world save directory.
    - On Linux, this is `~/.minecraft/saves/<name-of-world>`
    - On Windows, this is `%AppData%\Roaming\.minecraft\saves\<name-of-world>`
    - This can also be opened from the game itself.
        - From the main menu, select `Singleplayer` -> `WorldYouWantToUse` -> `Edit` -> `Open World Folder`
4. Run mc-slice
    - On Linux: `java -jar mc-slice-vX.X.X.jar`
    - On Windows: Right click on `mc-slice-vX.X.X.jar`, choose `Run with Java` (This assumes Java is installed on your system.)

5. Wait for the tool to do it's thing.
    - Once complete, verify that the datapack has been removed (the tool renames it to `CavesAndCliffsPreview.zip.bak`, instead of outright deleting it).
    - Open the world in 21w15a, verify that the world is normal.

If problems occur, please let me know by creating an issue on the [Issues](https://www.github.com/the404devs/mc-slice/issues) page.

## Changelog
------------
### *0.0.1 (04/19/2021)*
------------------------
- Initial version
- Created to convert my 7+ year Minecraft world from the 384-block tall world height introduced in 21w06a to the old 256-block tall height that 21w15a rolls back to.