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

package me.ryanhamshire.GriefPrevention;

import java.util.*;

import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

//represents a player claim
//creating an instance doesn't make an effective claim
//only claims which have been added to the datastore have any effect
public class Claim
{
	//two locations, which together define the boundaries of the claim
	//note that the upper Y value is always ignored, because claims ALWAYS extend up to the sky
	Location lesserBoundaryCorner;
	Location greaterBoundaryCorner;
	
	//modification date.  this comes from the file timestamp during load, and is updated with runtime changes
	public Date modifiedDate;
	
	//id number.  unique to this claim, never changes.
	Long id = null;
	
	//ownerID.  for admin claims, this is NULL
	//use getOwnerName() to get a friendly name (will be "an administrator" for admin claims)
	public UUID ownerID;
	
	//list of players who (beyond the claim owner) have permission to grant permissions in this claim
	public ArrayList<String> managers = new ArrayList<String>();
	
	//permissions for this claim, see ClaimPermission class
	private HashMap<String, ClaimPermission> playerIDToClaimPermissionMap = new HashMap<String, ClaimPermission>();
	
	//whether or not this claim is in the data store
	//if a claim instance isn't in the data store, it isn't "active" - players can't interract with it 
	//why keep this?  so that claims which have been removed from the data store can be correctly 
	//ignored even though they may have references floating around
	public boolean inDataStore = false;
	
	public boolean areExplosivesAllowed = false;
	
	//parent claim
	//only used for claim subdivisions.  top level claims have null here
	public Claim parent = null;
	
	// intended for subclaims - they inherit no permissions
	private boolean inheritNothing = false;

	//children (subdivisions)
	//note subdivisions themselves never have children
	public ArrayList<Claim> children = new ArrayList<Claim>();
	
	//information about a siege involving this claim.  null means no siege is impacting this claim
	public SiegeData siegeData = null;
	
	//following a siege, buttons/levers are unlocked temporarily.  this represents that state
	public boolean doorsOpen = false;
	
	//whether or not this is an administrative claim
	//administrative claims are created and maintained by players with the griefprevention.adminclaims permission.
	public boolean isAdminClaim()
	{
		if(this.parent != null) return this.parent.isAdminClaim();
	    
		return (this.ownerID == null);
	}
	
	//accessor for ID
	public Long getID()
	{
		return this.id;
	}
	
	//basic constructor, just notes the creation time
	//see above declarations for other defaults
	Claim()
	{
		this.modifiedDate = Calendar.getInstance().getTime();
	}
	
	//players may only siege someone when he's not in an admin claim 
	//and when he has some level of permission in the claim
	public boolean canSiege(Player defender)
	{
		if(this.isAdminClaim()) return false;
		
		if(this.allowAccess(defender) != null) return false;
		
		return true;
	}
	
	//removes any lava above sea level in a claim
	//exclusionClaim is another claim indicating an sub-area to be excluded from this operation
	//it may be null
	public void removeSurfaceFluids(Claim exclusionClaim)
	{
		//don't do this for administrative claims
		if(this.isAdminClaim()) return;
		
		//don't do it for very large claims
		if(this.getArea() > 10000) return;
		
		//only in creative mode worlds
		if(!GriefPrevention.instance.creativeRulesApply(this.lesserBoundaryCorner)) return;
		
		Location lesser = this.getLesserBoundaryCorner();
		Location greater = this.getGreaterBoundaryCorner();

		if(lesser.getWorld().getEnvironment() == Environment.NETHER) return;  //don't clean up lava in the nether
		
		int seaLevel = 0;  //clean up all fluids in the end
		
		//respect sea level in normal worlds
		if(lesser.getWorld().getEnvironment() == Environment.NORMAL) seaLevel = GriefPrevention.instance.getSeaLevel(lesser.getWorld());
		
		for(int x = lesser.getBlockX(); x <= greater.getBlockX(); x++)
		{
			for(int z = lesser.getBlockZ(); z <= greater.getBlockZ(); z++)
			{
				for(int y = seaLevel - 1; y <= lesser.getWorld().getMaxHeight(); y++)
				{
					//dodge the exclusion claim
					Block block = lesser.getWorld().getBlockAt(x, y, z);
					if(exclusionClaim != null && exclusionClaim.contains(block.getLocation(), true, false)) continue;
					
					if(block.getType() == Material.LAVA || block.getType() == Material.WATER)
					{
						block.setType(Material.AIR);
					}
				}
			}
		}		
	}
	
	//determines whether or not a claim has surface lava
	//used to warn players when they abandon their claims about automatic fluid cleanup
	boolean hasSurfaceFluids()
	{
		Location lesser = this.getLesserBoundaryCorner();
		Location greater = this.getGreaterBoundaryCorner();

		//don't bother for very large claims, too expensive
		if(this.getArea() > 10000) return false;
		
		int seaLevel = 0;  //clean up all fluids in the end
		
		//respect sea level in normal worlds
		if(lesser.getWorld().getEnvironment() == Environment.NORMAL) seaLevel = GriefPrevention.instance.getSeaLevel(lesser.getWorld());
		
		for(int x = lesser.getBlockX(); x <= greater.getBlockX(); x++)
		{
			for(int z = lesser.getBlockZ(); z <= greater.getBlockZ(); z++)
			{
				for(int y = seaLevel - 1; y <= lesser.getWorld().getMaxHeight(); y++)
				{
					//dodge the exclusion claim
					Block block = lesser.getWorld().getBlockAt(x, y, z);
					
					if(block.getType() == Material.WATER || block.getType() == Material.LAVA)
					{
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	//main constructor.  note that only creating a claim instance does nothing - a claim must be added to the data store to be effective
	Claim(Location lesserBoundaryCorner, Location greaterBoundaryCorner, UUID ownerID, List<String> builderIDs, List<String> containerIDs, List<String> accessorIDs, List<String> managerIDs, boolean inheritNothing, Long id)
	{
		//modification date
		this.modifiedDate = Calendar.getInstance().getTime();
		
		//id
		this.id = id;
		
		//store corners
		this.lesserBoundaryCorner = lesserBoundaryCorner;
		this.greaterBoundaryCorner = greaterBoundaryCorner;
		
		//owner
		this.ownerID = ownerID;
		
		//other permissions
		for(String builderID : builderIDs)
		{
			if(builderID != null && !builderID.isEmpty())
			{
				this.playerIDToClaimPermissionMap.put(builderID, ClaimPermission.Build);
			}
		}
		
		for(String containerID : containerIDs)
		{
			if(containerID != null && !containerID.isEmpty())
			{
				this.playerIDToClaimPermissionMap.put(containerID, ClaimPermission.Inventory);
			}
		}
		
		for(String accessorID : accessorIDs)
		{
			if(accessorID != null && !accessorID.isEmpty())
			{
				this.playerIDToClaimPermissionMap.put(accessorID, ClaimPermission.Access);
			}
		}
		
		for(String managerID : managerIDs)
		{
			if(managerID != null && !managerID.isEmpty())
			{
				this.managers.add(managerID);
			}
		}

		this.inheritNothing = inheritNothing;
	}

	Claim(Location lesserBoundaryCorner, Location greaterBoundaryCorner, UUID ownerID, List<String> builderIDs, List<String> containerIDs, List<String> accessorIDs, List<String> managerIDs, Long id)
	{
		this(lesserBoundaryCorner, greaterBoundaryCorner, ownerID, builderIDs, containerIDs, accessorIDs, managerIDs, false, id);
	}
	
	//measurements.  all measurements are in blocks
	public int getArea()
	{
		int claimWidth = this.greaterBoundaryCorner.getBlockX() - this.lesserBoundaryCorner.getBlockX() + 1;
		int claimHeight = this.greaterBoundaryCorner.getBlockZ() - this.lesserBoundaryCorner.getBlockZ() + 1;
		
		return claimWidth * claimHeight;		
	}
	
	public int getWidth()
	{
		return this.greaterBoundaryCorner.getBlockX() - this.lesserBoundaryCorner.getBlockX() + 1;		
	}
	
	public int getHeight()
	{
		return this.greaterBoundaryCorner.getBlockZ() - this.lesserBoundaryCorner.getBlockZ() + 1;		
	}

	public boolean getSubclaimRestrictions()
	{
		return inheritNothing;
	}

	public void setSubclaimRestrictions(boolean inheritNothing)
	{
		this.inheritNothing = inheritNothing;
	}

	//distance check for claims, distance in this case is a band around the outside of the claim rather then euclidean distance
	public boolean isNear(Location location, int howNear)
	{
		Claim claim = new Claim
			(new Location(this.lesserBoundaryCorner.getWorld(), this.lesserBoundaryCorner.getBlockX() - howNear, this.lesserBoundaryCorner.getBlockY(), this.lesserBoundaryCorner.getBlockZ() - howNear),
			 new Location(this.greaterBoundaryCorner.getWorld(), this.greaterBoundaryCorner.getBlockX() + howNear, this.greaterBoundaryCorner.getBlockY(), this.greaterBoundaryCorner.getBlockZ() + howNear),
			 null, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>(), null);
		
		return claim.contains(location, false, true);
	}
	
	//permissions.  note administrative "public" claims have different rules than other claims
	//all of these return NULL when a player has permission, or a String error message when the player doesn't have permission
	public String allowEdit(Player player)
	{
		//if we don't know who's asking, always say no (i've been told some mods can make this happen somehow)
		if(player == null) return "";
		
		//special cases...
		
		//admin claims need adminclaims permission only.
		if(this.isAdminClaim())
		{
			if(player.hasPermission("griefprevention.adminclaims")) return null;
		}
		
		//anyone with deleteclaims permission can modify non-admin claims at any time
		else
		{
			if(player.hasPermission("griefprevention.deleteclaims")) return null;
		}
		
		//no resizing, deleting, and so forth while under siege
		if(player.getUniqueId().equals(this.ownerID))
		{
			if(this.siegeData != null)
			{
				return GriefPrevention.instance.dataStore.getMessage(Messages.NoModifyDuringSiege);
			}
			
			//otherwise, owners can do whatever
			return null;
		}
		
		//permission inheritance for subdivisions
		if(this.parent != null)
		{
			if (player.getUniqueId().equals(this.parent.ownerID))
				return null;
			if (!inheritNothing)
				return this.parent.allowEdit(player);
		}
		
		//error message if all else fails
		return GriefPrevention.instance.dataStore.getMessage(Messages.OnlyOwnersModifyClaims, this.getOwnerName());
	}
	
	private List<Material> placeableFarmingBlocksList = Arrays.asList(
	        Material.PUMPKIN_STEM,
	        Material.WHEAT,
	        Material.MELON_STEM,
	        Material.CARROTS,
	        Material.POTATOES,
	        Material.NETHER_WART,
	        Material.BEETROOTS);
	    
    private boolean placeableForFarming(Material material)
    {
        return this.placeableFarmingBlocksList.contains(material);
    }
	
	//build permission check
	public String allowBuild(Player player, Material material)
	{
		//if we don't know who's asking, always say no (i've been told some mods can make this happen somehow)
		if(player == null) return "";
		
		//when a player tries to build in a claim, if he's under siege, the siege may extend to include the new claim
		GriefPrevention.instance.dataStore.tryExtendSiege(player, this);
		
		//admin claims can always be modified by admins, no exceptions
		if(this.isAdminClaim())
		{
			if(player.hasPermission("griefprevention.adminclaims")) return null;
		}
		
		//no building while under siege
		if(this.siegeData != null)
		{
			return GriefPrevention.instance.dataStore.getMessage(Messages.NoBuildUnderSiege, this.siegeData.attacker.getName());
		}
		
		//no building while in pvp combat
		PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
		if(playerData.inPvpCombat())
		{
			return GriefPrevention.instance.dataStore.getMessage(Messages.NoBuildPvP);			
		}
		
		//owners can make changes, or admins with ignore claims mode enabled
		if(player.getUniqueId().equals(this.ownerID) || GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId()).ignoreClaims) return null;
		
		//anyone with explicit build permission can make changes
		if(this.hasExplicitPermission(player, ClaimPermission.Build)) return null;
		
		//also everyone is a member of the "public", so check for public permission
		ClaimPermission permissionLevel = this.playerIDToClaimPermissionMap.get("public");
		if(ClaimPermission.Build == permissionLevel) return null;
		
		//allow for farming with /containertrust permission
		if(this.allowContainers(player) == null)
		{
			//do allow for farming, if player has /containertrust permission
			if(this.placeableForFarming(material))
			{
				return null;
			}
		}
		
		//subdivision permission inheritance
		if(this.parent != null)
		{
			if (player.getUniqueId().equals(this.parent.ownerID))
				return null;
			if (!inheritNothing)
				return this.parent.allowBuild(player, material);
		}
		
		//failure message for all other cases
		String reason = GriefPrevention.instance.dataStore.getMessage(Messages.NoBuildPermission, this.getOwnerName());
		if(player.hasPermission("griefprevention.ignoreclaims"))
				reason += "  " + GriefPrevention.instance.dataStore.getMessage(Messages.IgnoreClaimsAdvertisement);
		
		return reason;
	}
	
	private boolean hasExplicitPermission(Player player, ClaimPermission level)
	{
		String playerID = player.getUniqueId().toString();
		Set<String> keys = this.playerIDToClaimPermissionMap.keySet();
		Iterator<String> iterator = keys.iterator();
		while(iterator.hasNext())
		{
			String identifier = iterator.next();
			if(playerID.equalsIgnoreCase(identifier) && this.playerIDToClaimPermissionMap.get(identifier) == level) return true;
			
			else if(identifier.startsWith("[") && identifier.endsWith("]"))
			{
				//drop the brackets
				String permissionIdentifier = identifier.substring(1, identifier.length() - 1);
				
				//defensive coding
				if(permissionIdentifier == null || permissionIdentifier.isEmpty()) continue;
				
				//check permission
				if(player.hasPermission(permissionIdentifier) && this.playerIDToClaimPermissionMap.get(identifier) == level) return true;
			}
		}
		
		return false;			
	}
	
	//break permission check
	public String allowBreak(Player player, Material material)
	{
		//if under siege, some blocks will be breakable
		if(this.siegeData != null || this.doorsOpen)
		{
			boolean breakable = false;
			
			//search for block type in list of breakable blocks
			for(int i = 0; i < GriefPrevention.instance.config_siege_blocks.size(); i++)
			{
				Material breakableMaterial = GriefPrevention.instance.config_siege_blocks.get(i);
				if(breakableMaterial == material)
				{
					breakable = true;
					break;
				}
			}
			
			//custom error messages for siege mode
			if(!breakable)
			{
				return GriefPrevention.instance.dataStore.getMessage(Messages.NonSiegeMaterial);
			}
			else if(player.getUniqueId().equals(this.ownerID))
			{
				return GriefPrevention.instance.dataStore.getMessage(Messages.NoOwnerBuildUnderSiege);
			}
			else
			{
				return null;
			}
		}
		
		//if not under siege, build rules apply
		return this.allowBuild(player, material);
	}
	
	//access permission check
	public String allowAccess(Player player)
	{
	    //following a siege where the defender lost, the claim will allow everyone access for a time
		if(this.doorsOpen) return null;
		
		//admin claims need adminclaims permission only.
		if(this.isAdminClaim())
		{
			if(player.hasPermission("griefprevention.adminclaims")) return null;
		}
		
		//claim owner and admins in ignoreclaims mode have access
		if(player.getUniqueId().equals(this.ownerID) || GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId()).ignoreClaims) return null;
		
		//look for explicit individual access, inventory, or build permission
		if(this.hasExplicitPermission(player, ClaimPermission.Access)) return null;
		if(this.hasExplicitPermission(player, ClaimPermission.Inventory)) return null;
		if(this.hasExplicitPermission(player, ClaimPermission.Build)) return null;
		
		//also check for public permission
		ClaimPermission permissionLevel = this.playerIDToClaimPermissionMap.get("public");
		if(ClaimPermission.Build == permissionLevel || ClaimPermission.Inventory == permissionLevel || ClaimPermission.Access == permissionLevel) return null;		
		
		//permission inheritance for subdivisions
		if(this.parent != null)
		{
			if (player.getUniqueId().equals(this.parent.ownerID))
				return null;
			if (!inheritNothing)
				return this.parent.allowAccess(player);
		}
		
		//catch-all error message for all other cases
		String reason = GriefPrevention.instance.dataStore.getMessage(Messages.NoAccessPermission, this.getOwnerName());
		if(player.hasPermission("griefprevention.ignoreclaims"))
			reason += "  " + GriefPrevention.instance.dataStore.getMessage(Messages.IgnoreClaimsAdvertisement);
		return reason;
	}
	
	//inventory permission check
	public String allowContainers(Player player)
	{		
		//if we don't know who's asking, always say no (i've been told some mods can make this happen somehow)
		if(player == null) return "";
		
		//trying to access inventory in a claim may extend an existing siege to include this claim
		GriefPrevention.instance.dataStore.tryExtendSiege(player, this);
		
		//if under siege, nobody accesses containers
		if(this.siegeData != null)
		{
			return GriefPrevention.instance.dataStore.getMessage(Messages.NoContainersSiege, siegeData.attacker.getName());
		}
		
		//owner and administrators in ignoreclaims mode have access
		if(player.getUniqueId().equals(this.ownerID) || GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId()).ignoreClaims) return null;
		
		//admin claims need adminclaims permission only.
		if(this.isAdminClaim())
		{
			if(player.hasPermission("griefprevention.adminclaims")) return null;
		}
		
		//check for explicit individual container or build permission 
		if(this.hasExplicitPermission(player, ClaimPermission.Inventory)) return null;
		if(this.hasExplicitPermission(player, ClaimPermission.Build)) return null;
		
		//check for public container or build permission
		ClaimPermission permissionLevel = this.playerIDToClaimPermissionMap.get("public");
		if(ClaimPermission.Build == permissionLevel || ClaimPermission.Inventory == permissionLevel) return null;
		
		//permission inheritance for subdivisions
		if(this.parent != null)
		{
			if (player.getUniqueId().equals(this.parent.ownerID))
				return null;
			if (!inheritNothing)
				return this.parent.allowContainers(player);
		}
		
		//error message for all other cases
		String reason = GriefPrevention.instance.dataStore.getMessage(Messages.NoContainersPermission, this.getOwnerName());
		if(player.hasPermission("griefprevention.ignoreclaims"))
			reason += "  " + GriefPrevention.instance.dataStore.getMessage(Messages.IgnoreClaimsAdvertisement);
		return reason;
	}
	
	//grant permission check, relatively simple
	public String allowGrantPermission(Player player)
	{
		//if we don't know who's asking, always say no (i've been told some mods can make this happen somehow)
		if(player == null) return "";
		
		//anyone who can modify the claim can do this
		if(this.allowEdit(player) == null) return null;
		
		//anyone who's in the managers (/PermissionTrust) list can do this
		for(int i = 0; i < this.managers.size(); i++)
		{
			String managerID = this.managers.get(i);
			if(player.getUniqueId().toString().equals(managerID)) return null;
			
			else if(managerID.startsWith("[") && managerID.endsWith("]"))
			{
				managerID = managerID.substring(1, managerID.length() - 1);
				if(managerID == null || managerID.isEmpty()) continue;
				if(player.hasPermission(managerID)) return null;
			}
		}
		
		//permission inheritance for subdivisions
		if(this.parent != null)
		{
			if (player.getUniqueId().equals(this.parent.ownerID))
				return null;
			if (!inheritNothing)
				return this.parent.allowGrantPermission(player);
		}
		
		//generic error message
		String reason = GriefPrevention.instance.dataStore.getMessage(Messages.NoPermissionTrust, this.getOwnerName());
		if(player.hasPermission("griefprevention.ignoreclaims"))
			reason += "  " + GriefPrevention.instance.dataStore.getMessage(Messages.IgnoreClaimsAdvertisement);
		return reason;
	}
	
	//grants a permission for a player or the public
	public void setPermission(String playerID, ClaimPermission permissionLevel)
	{
		this.playerIDToClaimPermissionMap.put(playerID.toLowerCase(),  permissionLevel);
	}
	
	//revokes a permission for a player or the public
	public void dropPermission(String playerID)
	{
		this.playerIDToClaimPermissionMap.remove(playerID.toLowerCase());
		
		for(Claim child : this.children)
		{
		    child.dropPermission(playerID);
		}
	}
	
	//clears all permissions (except owner of course)
	public void clearPermissions()
	{
		this.playerIDToClaimPermissionMap.clear();
		this.managers.clear();
		
		for(Claim child : this.children)
        {
            child.clearPermissions();
        }
	}
	
	//gets ALL permissions
	//useful for  making copies of permissions during a claim resize and listing all permissions in a claim
	public void getPermissions(ArrayList<String> builders, ArrayList<String> containers, ArrayList<String> accessors, ArrayList<String> managers)
	{
		//loop through all the entries in the hash map
		Iterator<Map.Entry<String, ClaimPermission>> mappingsIterator = this.playerIDToClaimPermissionMap.entrySet().iterator(); 
		while(mappingsIterator.hasNext())
		{
			Map.Entry<String, ClaimPermission> entry = mappingsIterator.next();
			
			//build up a list for each permission level
			if(entry.getValue() == ClaimPermission.Build)
			{
				builders.add(entry.getKey());
			}
			else if(entry.getValue() == ClaimPermission.Inventory)
			{
				containers.add(entry.getKey());
			}
			else
			{
				accessors.add(entry.getKey());
			}			
		}
		
		//managers are handled a little differently
		for(int i = 0; i < this.managers.size(); i++)
		{
			managers.add(this.managers.get(i));
		}
	}
	
	//returns a copy of the location representing lower x, y, z limits
	public Location getLesserBoundaryCorner()
	{
		return this.lesserBoundaryCorner.clone();
	}
	
	//returns a copy of the location representing upper x, y, z limits
	//NOTE: remember upper Y will always be ignored, all claims always extend to the sky
	public Location getGreaterBoundaryCorner()
	{
		return this.greaterBoundaryCorner.clone();
	}
	
	//returns a friendly owner name (for admin claims, returns "an administrator" as the owner)
	public String getOwnerName()
	{
		if(this.parent != null)
			return this.parent.getOwnerName();
		
		if(this.ownerID == null)
			return GriefPrevention.instance.dataStore.getMessage(Messages.OwnerNameForAdminClaims);
		
		return GriefPrevention.lookupPlayerName(this.ownerID);
	}	
	
	//whether or not a location is in a claim
	//ignoreHeight = true means location UNDER the claim will return TRUE
	//excludeSubdivisions = true means that locations inside subdivisions of the claim will return FALSE
	public boolean contains(Location location, boolean ignoreHeight, boolean excludeSubdivisions)
	{
	    //not in the same world implies false
		if(!location.getWorld().equals(this.lesserBoundaryCorner.getWorld())) return false;
		
		double x = location.getX();
		double y = location.getY();
		double z = location.getZ();
		
		//main check
		boolean inClaim = (ignoreHeight || y >= this.lesserBoundaryCorner.getY()) &&
				x >= this.lesserBoundaryCorner.getX() &&
				x < this.greaterBoundaryCorner.getX() + 1 &&
				z >= this.lesserBoundaryCorner.getZ() &&
				z < this.greaterBoundaryCorner.getZ() + 1;
				
		if(!inClaim) return false;
				
	    //additional check for subdivisions
		//you're only in a subdivision when you're also in its parent claim
		//NOTE: if a player creates subdivions then resizes the parent claim, it's possible that
		//a subdivision can reach outside of its parent's boundaries.  so this check is important!
		if(this.parent != null)
	    {
	    	return this.parent.contains(location, ignoreHeight, false);
	    }
		
		//code to exclude subdivisions in this check
		else if(excludeSubdivisions)
		{
			//search all subdivisions to see if the location is in any of them
			for(int i = 0; i < this.children.size(); i++)
			{
				//if we find such a subdivision, return false
				if(this.children.get(i).contains(location, ignoreHeight, true))
				{
					return false;
				}
			}
		}
		
		//otherwise yes
		return true;				
	}
	
	//whether or not two claims overlap
	//used internally to prevent overlaps when creating claims
	boolean overlaps(Claim otherClaim)
	{
		//NOTE:  if trying to understand this makes your head hurt, don't feel bad - it hurts mine too.  
		//try drawing pictures to visualize test cases.
		
		if(!this.lesserBoundaryCorner.getWorld().equals(otherClaim.getLesserBoundaryCorner().getWorld())) return false;
		
		//first, check the corners of this claim aren't inside any existing claims
		if(otherClaim.contains(this.lesserBoundaryCorner, true, false)) return true;
		if(otherClaim.contains(this.greaterBoundaryCorner, true, false)) return true;
		if(otherClaim.contains(new Location(this.lesserBoundaryCorner.getWorld(), this.lesserBoundaryCorner.getBlockX(), 0, this.greaterBoundaryCorner.getBlockZ()), true, false)) return true;
		if(otherClaim.contains(new Location(this.lesserBoundaryCorner.getWorld(), this.greaterBoundaryCorner.getBlockX(), 0, this.lesserBoundaryCorner.getBlockZ()), true, false)) return true;
		
		//verify that no claim's lesser boundary point is inside this new claim, to cover the "existing claim is entirely inside new claim" case
		if(this.contains(otherClaim.getLesserBoundaryCorner(), true, false)) return true;
		
		//verify this claim doesn't band across an existing claim, either horizontally or vertically		
		if(	this.getLesserBoundaryCorner().getBlockZ() <= otherClaim.getGreaterBoundaryCorner().getBlockZ() && 
			this.getLesserBoundaryCorner().getBlockZ() >= otherClaim.getLesserBoundaryCorner().getBlockZ() && 
			this.getLesserBoundaryCorner().getBlockX() < otherClaim.getLesserBoundaryCorner().getBlockX() &&
			this.getGreaterBoundaryCorner().getBlockX() > otherClaim.getGreaterBoundaryCorner().getBlockX() )
			return true;
		
		if(	this.getGreaterBoundaryCorner().getBlockZ() <= otherClaim.getGreaterBoundaryCorner().getBlockZ() && 
			this.getGreaterBoundaryCorner().getBlockZ() >= otherClaim.getLesserBoundaryCorner().getBlockZ() && 
			this.getLesserBoundaryCorner().getBlockX() < otherClaim.getLesserBoundaryCorner().getBlockX() &&
			this.getGreaterBoundaryCorner().getBlockX() > otherClaim.getGreaterBoundaryCorner().getBlockX() )
			return true;
		
		if(	this.getLesserBoundaryCorner().getBlockX() <= otherClaim.getGreaterBoundaryCorner().getBlockX() && 
			this.getLesserBoundaryCorner().getBlockX() >= otherClaim.getLesserBoundaryCorner().getBlockX() && 
			this.getLesserBoundaryCorner().getBlockZ() < otherClaim.getLesserBoundaryCorner().getBlockZ() &&
			this.getGreaterBoundaryCorner().getBlockZ() > otherClaim.getGreaterBoundaryCorner().getBlockZ() )
			return true;
			
		if(	this.getGreaterBoundaryCorner().getBlockX() <= otherClaim.getGreaterBoundaryCorner().getBlockX() && 
			this.getGreaterBoundaryCorner().getBlockX() >= otherClaim.getLesserBoundaryCorner().getBlockX() && 
			this.getLesserBoundaryCorner().getBlockZ() < otherClaim.getLesserBoundaryCorner().getBlockZ() &&
			this.getGreaterBoundaryCorner().getBlockZ() > otherClaim.getGreaterBoundaryCorner().getBlockZ() )
			return true;
		
		return false;
	}
	
	//whether more entities may be added to a claim
	public String allowMoreEntities(boolean remove)
	{
		if(this.parent != null) return this.parent.allowMoreEntities(remove);
		
		//this rule only applies to creative mode worlds
		if(!GriefPrevention.instance.creativeRulesApply(this.getLesserBoundaryCorner())) return null;
		
		//admin claims aren't restricted
		if(this.isAdminClaim()) return null;
		
		//don't apply this rule to very large claims
		if(this.getArea() > 10000) return null;
		
		//determine maximum allowable entity count, based on claim size
		int maxEntities = this.getArea() / 50;		
		if(maxEntities == 0) return GriefPrevention.instance.dataStore.getMessage(Messages.ClaimTooSmallForEntities);
		
		//count current entities (ignoring players)
		int totalEntities = 0;
		ArrayList<Chunk> chunks = this.getChunks();
		for(Chunk chunk : chunks)
		{
			Entity [] entities = chunk.getEntities();
			for(int i = 0; i < entities.length; i++)
			{
				Entity entity = entities[i];
				if(!(entity instanceof Player) && this.contains(entity.getLocation(), false, false))
				{
					totalEntities++;
					if(remove && totalEntities > maxEntities) entity.remove();
				}
			}
		}

		if(totalEntities >= maxEntities) return GriefPrevention.instance.dataStore.getMessage(Messages.TooManyEntitiesInClaim);
		
		return null;
	}
	
	public String allowMoreActiveBlocks()
    {
	    if(this.parent != null) return this.parent.allowMoreActiveBlocks();
	    
	    //determine maximum allowable entity count, based on claim size
        int maxActives = this.getArea() / 100;      
        if(maxActives == 0) return GriefPrevention.instance.dataStore.getMessage(Messages.ClaimTooSmallForActiveBlocks);
        
        //count current actives
        int totalActives = 0;
        ArrayList<Chunk> chunks = this.getChunks();
        for(Chunk chunk : chunks)
        {
            BlockState [] actives = chunk.getTileEntities();
            for(int i = 0; i < actives.length; i++)
            {
                BlockState active = actives[i];
                if(BlockEventHandler.isActiveBlock(active))
                {
                    if(this.contains(active.getLocation(), false, false))
                    {
                        totalActives++;
                    }
                }
            }
        }

        if(totalActives >= maxActives) return GriefPrevention.instance.dataStore.getMessage(Messages.TooManyActiveBlocksInClaim);
        
        return null;
    }
	
	//implements a strict ordering of claims, used to keep the claims collection sorted for faster searching
	boolean greaterThan(Claim otherClaim)
	{
		Location thisCorner = this.getLesserBoundaryCorner();
		Location otherCorner = otherClaim.getLesserBoundaryCorner();
		
		if(thisCorner.getBlockX() > otherCorner.getBlockX()) return true;
		
		if(thisCorner.getBlockX() < otherCorner.getBlockX()) return false;
		
		if(thisCorner.getBlockZ() > otherCorner.getBlockZ()) return true;
		
		if(thisCorner.getBlockZ() < otherCorner.getBlockZ()) return false;
		
		return thisCorner.getWorld().getName().compareTo(otherCorner.getWorld().getName()) < 0;
	}
	
	@SuppressWarnings("deprecation")
    long getPlayerInvestmentScore()
	{
		//decide which blocks will be considered player placed
		Location lesserBoundaryCorner = this.getLesserBoundaryCorner();
		ArrayList<Material> playerBlocks = RestoreNatureProcessingTask.getPlayerBlocks(lesserBoundaryCorner.getWorld().getEnvironment(), lesserBoundaryCorner.getBlock().getBiome());
		
		//scan the claim for player placed blocks
		double score = 0;
		
		boolean creativeMode = GriefPrevention.instance.creativeRulesApply(lesserBoundaryCorner);
		
		for(int x = this.lesserBoundaryCorner.getBlockX(); x <= this.greaterBoundaryCorner.getBlockX(); x++)
		{
			for(int z = this.lesserBoundaryCorner.getBlockZ(); z <= this.greaterBoundaryCorner.getBlockZ(); z++)
			{
				int y = this.lesserBoundaryCorner.getBlockY();
				for(; y < GriefPrevention.instance.getSeaLevel(this.lesserBoundaryCorner.getWorld()) - 5; y++)
				{
					Block block = this.lesserBoundaryCorner.getWorld().getBlockAt(x, y, z);
					if(playerBlocks.contains(block.getType()))
					{
						if(block.getType() == Material.CHEST && !creativeMode)
						{
							score += 10;
						}
						else
						{
							score += .5;
						}						
					}
				}
				
				for(; y < this.lesserBoundaryCorner.getWorld().getMaxHeight(); y++)
				{
					Block block = this.lesserBoundaryCorner.getWorld().getBlockAt(x, y, z);
					if(playerBlocks.contains(block.getType()))
					{
						if(block.getType() == Material.CHEST && !creativeMode)
						{
							score += 10;
						}
						else if(creativeMode && (block.getType() == Material.LAVA))
						{
							score -= 10;
						}
						else 
						{
							score += 1;
						}						
					}
				}
			}
		}
		
		return (long)score;
	}

    public ArrayList<Chunk> getChunks()
    {
        ArrayList<Chunk> chunks = new ArrayList<Chunk>();
        
        World world = this.getLesserBoundaryCorner().getWorld();
        Chunk lesserChunk = this.getLesserBoundaryCorner().getChunk();
        Chunk greaterChunk = this.getGreaterBoundaryCorner().getChunk();
        
        for(int x = lesserChunk.getX(); x <= greaterChunk.getX(); x++)
        {
            for(int z = lesserChunk.getZ(); z <= greaterChunk.getZ(); z++)
            {
                chunks.add(world.getChunkAt(x, z));
            }
        }
        
        return chunks;
    }

    ArrayList<Long> getChunkHashes()
    {
        ArrayList<Long> hashes = new ArrayList<Long>();
        int smallX = this.getLesserBoundaryCorner().getBlockX() >> 4;
        int smallZ = this.getLesserBoundaryCorner().getBlockZ() >> 4;
		int largeX = this.getGreaterBoundaryCorner().getBlockX() >> 4;
		int largeZ = this.getGreaterBoundaryCorner().getBlockZ() >> 4;
		
		for(int x = smallX; x <= largeX; x++)
		{
		    for(int z = smallZ; z <= largeZ; z++)
		    {
		        hashes.add(DataStore.getChunkHash(x, z));
		    }
		}
		
		return hashes;
    }
}
