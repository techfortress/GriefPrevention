/*
    GriefPrevention Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ryanhamshire.GriefPrevention.storage;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import me.ryanhamshire.GriefPrevention.CustomLogEntryTypes;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.claim.Claim;
import me.ryanhamshire.GriefPrevention.claim.ClaimPermission;
import me.ryanhamshire.GriefPrevention.player.PlayerData;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

//manages storage stored in the file system
public class FlatFileStorage implements Storage
{
    private GriefPrevention plugin;

    private File claimDataFolder;
    private File playerDataFolder;

    FlatFileStorage(GriefPrevention griefPrevention)
    {
        plugin = griefPrevention;
        claimDataFolder = new File(plugin.getDataFolder(), "ClaimData");
        playerDataFolder = new File(plugin.getDataFolder(), "PlayerData");

        //Generate respective storage folders
        boolean newDataStore = false;
        if(!playerDataFolder.exists() || !claimDataFolder.exists())
        {
            newDataStore = true;
            playerDataFolder.mkdirs();
            claimDataFolder.mkdirs();
        }
    }

	public Set<Claim> getClaims()
	{
        File[] files = claimDataFolder.listFiles();
        Set<Claim> claims = new HashSet<>();

        for(int i = 0; i < files.length; i++)
        {           
            if(!files[i].isFile())  //avoids folders
                continue;

            try
            {
                claims.add(this.loadClaim(files[i]));
            }
            //if there's any problem with the file's content, log an error message and skip it
            catch(Exception e)
            {
                plugin.getLogger().severe("Could not load claim " + files[i].getName() + " " );
                continue;
            }
        }
        return claims;
    }
	
	Claim loadClaim(File file) throws ClassCastException, NumberFormatException
	{
        Claim claim;
        long claimID = Long.parseLong(file.getName().split("\\.")[0]);
	    YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        
        //boundaries
        Location lesserBoundaryCorner = (Location)yaml.get("lesserBoundaryCorner");
        Location greaterBoundaryCorner = (Location)yaml.get("greaterBoundaryCorner");
        
        //owner
        String ownerIdentifier = yaml.getString("owner");
        UUID ownerID = null;
        if(!ownerIdentifier.isEmpty())
        {
            try
            {
                ownerID = UUID.fromString(ownerIdentifier);
            }
            catch(Exception ex)
            {
                GriefPrevention.AddLogEntry("Error - this is not a valid UUID: " + ownerIdentifier + ".");
                GriefPrevention.AddLogEntry("  Converted land claim to administrative @ " + lesserBoundaryCorner.toString());
            }
        }

        Map<UUID, ClaimPermission> trustees = new HashMap<>();
        ConfigurationSection trusteesSection = yaml.getConfigurationSection("trustees");

        for (String trustee : trusteesSection.getKeys(false))
        {
            trustees.put(UUID.fromString(trustee), ClaimPermission.valueOf(trusteesSection.getString(trustee)));
        }
        
        //instantiate
        claim = new Claim(lesserBoundaryCorner, greaterBoundaryCorner, ownerID, trustees, claimID);
        
        return claim;
	}

	private File getClaimFile(Claim claim) throws NumberFormatException
    {
        return new File(claimDataFolder.getPath() + File.separator + Long.toString(claim.getID()) + ".yml");
    }

	public void saveClaim(Claim claim) throws Exception
	{
		YamlConfiguration yaml = new YamlConfiguration();
		yaml.set("lesserBoundaryCorner", claim.getLesserBoundaryCorner().toString());
        yaml.set("greaterBoundaryCorner", claim.getGreaterBoundaryCorner().toString());
        yaml.set("owner", claim.getOwnerUUID());
        yaml.set("trustees", claim.getTrustees()); //TODO: does this store enum's string or int value??
        yaml.save(getClaimFile(claim));
	}

	public boolean deleteClaim(Claim claim)
	{
		File claimFile = getClaimFile(claim);
		return !claimFile.exists() || claimFile.delete();
	}

	public synchronized PlayerData getPlayerData(UUID uuid)
	{
		File playerFile = new File(playerDataFolder.getPath() + File.separator + uuid.toString());
					
		PlayerData playerData = new PlayerData();

		int accrued;
		int bonus;
		
		//Return new PlayerData file if none exists
		if(!playerFile.exists())
        {
            return new PlayerData(uuid, 0, 0);
        }

        //read the file content and immediately close it
        List<String> lines = Files.readAllLines(playerFile.toPath());
        Iterator<String> iterator = lines.iterator();

        //second line is accrued claim blocks
        String accruedBlocksString = iterator.next();

        //convert that to a number and store it
        accrued = Integer.parseInt(accruedBlocksString);

        //third line is any bonus claim blocks granted by administrators
        String bonusBlocksString = iterator.next();

        //convert that to a number and store it
        bonus = Integer.parseInt(bonusBlocksString);
			
		return playerData;
	}
	
	//saves changes to player storage.
	public void savePlayerDataSync(UUID playerID, PlayerData playerData)
	{
		//never save storage for the "administrative" account.  null for claim owner ID indicates administrative account
		if(playerID == null) return;
		
		StringBuilder fileContent = new StringBuilder();
		try
		{
			//first line is accrued claim blocks
			fileContent.append(String.valueOf(playerData.getAccruedClaimBlocks()));
			fileContent.append("\n");			
			
			//second line is bonus claim blocks
			fileContent.append(String.valueOf(playerData.getBonusClaimBlocks()));
			fileContent.append("\n");
			
			//third line is blank
			fileContent.append("\n");
			
			//write storage to file
            File playerDataFile = new File(playerDataFolderPath + File.separator + playerID.toString());
            Files.write(fileContent.toString().getBytes("UTF-8"), playerDataFile);
		}		
		
		//if any problem, log it
		catch(Exception e)
		{
			GriefPrevention.AddLogEntry("GriefPrevention: Unexpected exception saving storage for player \"" + playerID.toString() + "\": " + e.getMessage());
			e.printStackTrace();
		}
	}

    @Override
    public void savePlayerData(UUID playerID, PlayerData playerData)
    {
        new SavePlayerDataThread(playerID, playerData).start();
    }

    private class SavePlayerDataThread extends Thread
    {
        private UUID playerID;
        private PlayerData playerData;

        SavePlayerDataThread(UUID playerID, PlayerData playerData)
        {
            this.playerID = playerID;
            this.playerData = playerData;
        }

        public void run()
        {
            //ensure player storage is already read from file before trying to save
            playerData.getAccruedClaimBlocks();
            playerData.getClaims();
            savePlayerDataSync(this.playerID, this.playerData);
        }
    }
}

