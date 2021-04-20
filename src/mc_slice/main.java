package mc_slice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.ArrayList;
import net.querz.mca.Chunk;
import net.querz.mca.MCAFile;
import net.querz.mca.MCAUtil;
import net.querz.nbt.io.NBTUtil;
import net.querz.nbt.io.NamedTag;
import net.querz.nbt.tag.CompoundTag;

public class main {

	public static void main(String[] args) {
		final String worldDir = System.getProperty("user.dir");
		final String regionDir = worldDir + "/region";
		if (!Files.exists(Paths.get(regionDir))) {
			System.out.println("Current directory is not a Minecraft world!");
			System.out.println(worldDir);
			System.out.println("Place the .jar file inside your world directory before running.");
			System.out.println("See README.md for help if you are confused.");
			System.exit(1);
		}
		intro(worldDir);
		
		ArrayList<File> regions = getListOfRegionFiles(regionDir);
		double totalChunks = regions.size() * 1024;
		double completedChunks = 0.0;
		double completion  = 0.0;
		System.out.println((int) totalChunks + " chunks to slice.");
		System.out.println("Press \"ENTER\" to continue...");
		Scanner s = new Scanner(System.in);
		s.nextLine();
		
		try {
			// Iterate through all the region files in the directory.
			for (File region : regions) {
				// Open an mca file for reading.
				System.out.println("Starting " + region.getName() + "!");
				MCAFile mcaFile = MCAUtil.read(region);
				//This WILL fail if it comes across a 0-byte region file.
				for (int i = 0; i < 1024; i++) {
					Chunk c = mcaFile.getChunk(i);
					completedChunks++;
					completion = completedChunks / totalChunks;
					if (c != null) {
						int[] biomes = c.getBiomes();
						if (biomes.length > 1024) {
							c.setBiomes(cullBiomes(biomes));
						}
						sectionRecreate(c);
						
						System.out.println(biomes.length + " biome entries in chunk.");
						System.out.println(region.getName() + ": Reading index " + i + " (" + completion*100 + "% done.)");
					} else {
						 System.out.println("Nothing at index " + i + " (" + completion*100 + "% done.)");
					}
				}
				MCAUtil.write(mcaFile, region.getAbsolutePath());
				System.out.println("Finished " + region.getName() + "!");	
			}
		}
		catch(IOException e){
			System.out.println("Error loading file: "+e);
			System.out.println("If you think this error is my fault, create an issue at");
			System.out.println("https://www.github.com/the404devs/mc-slice/issues");
			System.out.println("Make sure to attach the file that caused the error, and give a full description of the problem.");
		}
		
		System.out.println("All finished!");
		readLevelDat(worldDir);
		removeDatapack(worldDir);
		System.out.println("You may safely open your world in 21w15a+ now.");
	}
	
	public static ArrayList<File> getListOfRegionFiles(String path) {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();
		ArrayList<File> listOfNonZeroByteFiles = new ArrayList<File>();
		long totalSize = 0;
		for (int i = 0; i < listOfFiles.length; i++) {
			//System.out.println(listOfFiles[i].getName() + " size: " + listOfFiles[i].length());
			if (listOfFiles[i].isFile() && listOfFiles[i].length() > 0 && listOfFiles[i].getName().substring(listOfFiles[i].getName().lastIndexOf(".")).toLowerCase().equals(".mca")) {
				//System.out.println("File " + listOfFiles[i].getName());
				listOfNonZeroByteFiles.add(listOfFiles[i]);
				totalSize += listOfFiles[i].length();
			}
		}
		System.out.println(totalSize + " bytes to slice.");
		System.out.println("Time estimate: " + (double) totalSize / 124821504.0 + " minutes.");
		return listOfNonZeroByteFiles;
	} 
	
	public static int[] cullBiomes(int[] biomesIn) {
		int[] biomesOut = new int[1024];
		System.out.println("BIOME LENGTH: " + biomesIn.length);
		for (int i = 0; i < 1024; i++) {
			biomesOut[i] = biomesIn[i];
		}
		return biomesOut;
	}
	
	public static void sectionRecreate(Chunk c) {
		for (int i = 0; i < 16; i++) {
			c.setSection(i, c.getSection(i));			
		}
	}
	
	public static void readLevelDat(String path) {
		try {
			NamedTag levelRoot = NBTUtil.read(path + "/level.dat");
			String tagText = levelRoot.getTag() + "";
			int start = tagText.indexOf("\"Version\"");
			CompoundTag t = (CompoundTag) levelRoot.getTag();
			CompoundTag data = (CompoundTag) t.get("Data");
			CompoundTag version = (CompoundTag) data.get("Version");
			version.putByte("Snapshot", (byte) 1);
			version.putInt("Id", 2709);
			version.putString("Name", "21w15a");
			NBTUtil.write(levelRoot, path + "/level.dat");
			System.out.println("Modified level.dat version tag for 21w15a");
		} catch (IOException e) {
			System.out.println("Could not read level.dat, does it exist?");
		}
	}
	
	public static void removeDatapack(String path) {
		Path datapackDir = Paths.get(path + "/datapacks/CavesAndCliffsPreview.zip");
		try {
			Files.move(datapackDir, datapackDir.resolveSibling("CavesAndCliffsPreview.zip.bak"));
			System.out.println("Renamed CavesAndCliffsPreview.zip to CavesAndCliffsPreview.zip.bak, it is no longer active");
		} catch (IOException e) {
			System.out.println("Could not find CavesAndCliffsPreview.zip datapack, you need to manually remove it.");
			//System.out.println(e);
		}
	}
	
	public static void intro(String world) {
		System.out.println("MC-Slice");
		System.out.println("v0.0.1 (04/19/2021) - the404");
		System.out.println("----------------------------");
		
		System.out.println("WARNING: This application has the potential to be very destructive to your Minecraft world.");
		System.out.println("Before you proceed, confirm you have done the following.");
		System.out.println("1: You have read README.md and understand why you need to use this application");
		System.out.println("\tThis can be found online at https://www.github.com/the404devs/mc-slice");
		System.out.println("2: You have made a backup of your Minecraft world.");
		System.out.println("\tThis can be done in-game: Edit World -> Make Backup");
		System.out.println("3: The following directory is the world you wish to run this application on.");
		System.out.println("\t" + world);
		
		Boolean waiting = true;
		String confirm = "";
		Scanner s = new Scanner(System.in);
		while(waiting){
	      System.out.println("Please confirm: Y/N");
	      confirm = s.nextLine().toLowerCase();
	      if(confirm.equals("n")){
	    	  System.out.println("Aborting!");
	    	  System.exit(0);
	      }else if (confirm.equals("y")){
	    	  System.out.println("Proceeding, this is the point of no return.");
	    	  waiting = false;
	      }else{
	        System.out.println("Bad input.");
	      }
		}
		System.out.println("Depending on the size of your world, this may take a while.");
		System.out.println("My 4GB world took about 30 minutes to complete.");
		System.out.println("----------------------------");
	}
}
