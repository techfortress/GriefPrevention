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

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

//represents a player claim
//creating an instance doesn't make an effective claim
//only claims which have been added to the datastore have any effect
public class Claim
{
    //two locations, which together define the boundaries of the claim
    //note that the upper Y value is always ignored, because claims ALWAYS extend up to the sky
    @NotNull Location lesserBoundaryCorner;
    @NotNull Location greaterBoundaryCorner;

    //modification date.  this comes from the file timestamp during load, and is updated with runtime changes
    public @NotNull Date modifiedDate;

    //id number.  unique to this claim, never changes.
    long id;

    //ownerID.  for admin claims, this is NULL
    //use getOwnerName() to get a friendly name (will be "an administrator" for admin claims)
    public @Nullable UUID ownerID;

    //list of players who (beyond the claim owner) have permission to grant permissions in this claim
    public @NotNull ArrayList<String> managers = new ArrayList<>();

    //permissions for this claim, see ClaimPermission class
    private HashMap<String, ClaimPermission> playerIDToClaimPermissionMap = new HashMap<>();

    //whether or not this claim is in the data store
    //if a claim instance isn't in the data store, it isn't "active" - players can't interract with it
    //why keep this?  so that claims which have been removed from the data store can be correctly
    //ignored even though they may have references floating around
    public boolean inDataStore = false;

    public boolean areExplosivesAllowed = false;

    //parent claim
    //only used for claim subdivisions.  top level claims have null here
    public @Nullable Claim parent = null;

    // intended for subclaims - they inherit no permissions
    private boolean inheritNothing = false;

    //children (subdivisions)
    //note subdivisions themselves never have children
    public @NotNull ArrayList<Claim> children = new ArrayList<>();

    //information about a siege involving this claim.  null means no siege is impacting this claim
    public @Nullable SiegeData siegeData = null;

    //following a siege, buttons/levers are unlocked temporarily.  this represents that state
    public boolean doorsOpen = false;

    //whether or not this is an administrative claim
    //administrative claims are created and maintained by players with the griefprevention.adminclaims permission.
    public boolean isAdminClaim()
    {
        if (this.parent != null) return this.parent.isAdminClaim();

        return (this.ownerID == null);
    }

    //accessor for ID
    public @NotNull Long getID()
    {
        return this.id;
    }

    //players may only siege someone when he's not in an admin claim
    //and when he has some level of permission in the claim
    public boolean canSiege(@NotNull Player defender)
    {
        if (this.isAdminClaim()) return false;

        if (this.allowAccess(defender) != null) return false;

        return true;
    }

    //removes any lava above sea level in a claim
    //exclusionClaim is another claim indicating an sub-area to be excluded from this operation
    //it may be null
    public void removeSurfaceFluids(@Nullable Claim exclusionClaim)
    {
        //don't do this for administrative claims
        if (this.isAdminClaim()) return;

        //don't do it for very large claims
        if (this.getArea() > 10000) return;

        //only in creative mode worlds
        if (!GriefPrevention.instance.creativeRulesApply(this.lesserBoundaryCorner)) return;

        Location lesser = this.getLesserBoundaryCorner();
        Location greater = this.getGreaterBoundaryCorner();

        // TODO claim world handling
        if (Objects.requireNonNull(lesser.getWorld()).getEnvironment() == Environment.NETHER) return;  //don't clean up lava in the nether

        int seaLevel = 0;  //clean up all fluids in the end

        //respect sea level in normal worlds
        if (lesser.getWorld().getEnvironment() == Environment.NORMAL)
            seaLevel = GriefPrevention.instance.getSeaLevel(lesser.getWorld());

        for (int x = lesser.getBlockX(); x <= greater.getBlockX(); x++)
        {
            for (int z = lesser.getBlockZ(); z <= greater.getBlockZ(); z++)
            {
                for (int y = seaLevel - 1; y <= lesser.getWorld().getMaxHeight(); y++)
                {
                    //dodge the exclusion claim
                    Block block = lesser.getWorld().getBlockAt(x, y, z);
                    if (exclusionClaim != null && exclusionClaim.contains(block.getLocation(), true, false)) continue;

                    if (block.getType() == Material.LAVA || block.getType() == Material.WATER)
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
        if (this.getArea() > 10000) return false;

        int seaLevel = 0;  //clean up all fluids in the end

        //respect sea level in normal worlds
        // TODO claim world handling
        if (Objects.requireNonNull(lesser.getWorld()).getEnvironment() == Environment.NORMAL)
            seaLevel = GriefPrevention.instance.getSeaLevel(lesser.getWorld());

        for (int x = lesser.getBlockX(); x <= greater.getBlockX(); x++)
        {
            for (int z = lesser.getBlockZ(); z <= greater.getBlockZ(); z++)
            {
                for (int y = seaLevel - 1; y <= lesser.getWorld().getMaxHeight(); y++)
                {
                    //dodge the exclusion claim
                    Block block = lesser.getWorld().getBlockAt(x, y, z);

                    if (block.getType() == Material.WATER || block.getType() == Material.LAVA)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    //main constructor.  note that only creating a claim instance does nothing - a claim must be added to the data store to be effective
    Claim(@NotNull Location lesserBoundaryCorner, @NotNull Location greaterBoundaryCorner, @Nullable UUID ownerID,
          @NotNull List<String> builderIDs, @NotNull List<String> containerIDs, @NotNull List<String> accessorIDs,
          @NotNull List<String> managerIDs, boolean inheritNothing, @Nullable Long id)
    {
        //modification date
        this.modifiedDate = Calendar.getInstance().getTime();

        //id
        this.id = id == null ? -1L : id;

        //store corners
        this.lesserBoundaryCorner = lesserBoundaryCorner;
        this.greaterBoundaryCorner = greaterBoundaryCorner;

        //owner
        this.ownerID = ownerID;

        //other permissions
        for (String builderID : builderIDs)
        {
            this.setPermission(builderID, ClaimPermission.Build);
        }

        for (String containerID : containerIDs)
        {
            this.setPermission(containerID, ClaimPermission.Inventory);
        }

        for (String accessorID : accessorIDs)
        {
            this.setPermission(accessorID, ClaimPermission.Access);
        }

        for (String managerID : managerIDs)
        {
            if (managerID != null && !managerID.isEmpty())
            {
                this.managers.add(managerID);
            }
        }

        this.inheritNothing = inheritNothing;
    }

    Claim(@NotNull Location lesserBoundaryCorner, @NotNull Location greaterBoundaryCorner, @Nullable UUID ownerID,
          @NotNull List<String> builderIDs, @NotNull List<String> containerIDs, @NotNull List<String> accessorIDs,
          @NotNull List<String> managerIDs, @Nullable Long id)
    {
        this(lesserBoundaryCorner, greaterBoundaryCorner, ownerID, builderIDs, containerIDs, accessorIDs, managerIDs, false, id);
    }

    //produces a copy of a claim.
    public Claim(@NotNull Claim claim) {
        this.modifiedDate = claim.modifiedDate;
        this.lesserBoundaryCorner = claim.greaterBoundaryCorner.clone();
        this.greaterBoundaryCorner = claim.greaterBoundaryCorner.clone();
        this.id = claim.id;
        this.ownerID = claim.ownerID;
        this.managers = new ArrayList<>(claim.managers);
        this.playerIDToClaimPermissionMap = new HashMap<>(claim.playerIDToClaimPermissionMap);
        this.inDataStore = false; //since it's a copy of a claim, not in datastore!
        this.areExplosivesAllowed = claim.areExplosivesAllowed;
        this.parent = claim.parent;
        this.inheritNothing = claim.inheritNothing;
        this.children = new ArrayList<>(claim.children);
        this.siegeData = claim.siegeData;
        this.doorsOpen = claim.doorsOpen;
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
    public boolean isNear(@NotNull Location location, int howNear)
    {
        Claim claim = new Claim
                (new Location(this.lesserBoundaryCorner.getWorld(), this.lesserBoundaryCorner.getBlockX() - howNear, this.lesserBoundaryCorner.getBlockY(), this.lesserBoundaryCorner.getBlockZ() - howNear),
                        new Location(this.greaterBoundaryCorner.getWorld(), this.greaterBoundaryCorner.getBlockX() + howNear, this.greaterBoundaryCorner.getBlockY(), this.greaterBoundaryCorner.getBlockZ() + howNear),
                        null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), -1L);

        return claim.contains(location, false, true);
    }

    //permissions.  note administrative "public" claims have different rules than other claims
    //all of these return NULL when a player has permission, or a String error message when the player doesn't have permission
    public @Nullable String allowEdit(@NotNull Player player)
    {
        //if we don't know who's asking, always say no (i've been told some mods can make this happen somehow)
        //noinspection ConstantConditions // Defensive coding
        if (player == null) return "";

        //special cases...

        //admin claims need adminclaims permission only.
        if (this.isAdminClaim())
        {
            if (player.hasPermission("griefprevention.adminclaims")) return null;
        }

        //anyone with deleteclaims permission can modify non-admin claims at any time
        else
        {
            if (player.hasPermission("griefprevention.deleteclaims")) return null;
        }

        //no resizing, deleting, and so forth while under siege
        if (player.getUniqueId().equals(this.ownerID))
        {
            if (this.siegeData != null)
            {
                return GriefPrevention.instance.dataStore.getMessage(Messages.NoModifyDuringSiege);
            }

            //otherwise, owners can do whatever
            return null;
        }

        //permission inheritance for subdivisions
        if (this.parent != null)
        {
            if (player.getUniqueId().equals(this.parent.ownerID))
                return null;
            if (!inheritNothing)
                return this.parent.allowEdit(player);
        }

        //error message if all else fails
        return GriefPrevention.instance.dataStore.getMessage(Messages.OnlyOwnersModifyClaims, this.getOwnerName());
    }

    private static final EnumSet<Material> PLACEABLE_FARMING_BLOCKS = EnumSet.of(
            Material.PUMPKIN_STEM,
            Material.WHEAT,
            Material.MELON_STEM,
            Material.CARROTS,
            Material.POTATOES,
            Material.NETHER_WART,
            Material.BEETROOTS);

    private static boolean placeableForFarming(Material material)
    {
        return PLACEABLE_FARMING_BLOCKS.contains(material);
    }

    //build permission check
    public @Nullable String allowBuild(@NotNull Player player, @NotNull Material material)
    {
        //if we don't know who's asking, always say no (i've been told some mods can make this happen somehow)
        //noinspection ConstantConditions // Defensive coding
        if (player == null) return "";

        //when a player tries to build in a claim, if he's under siege, the siege may extend to include the new claim
        GriefPrevention.instance.dataStore.tryExtendSiege(player, this);

        //admin claims can always be modified by admins, no exceptions
        if (this.isAdminClaim())
        {
            if (player.hasPermission("griefprevention.adminclaims")) return null;
        }

        //no building while under siege
        if (this.siegeData != null)
        {
            return GriefPrevention.instance.dataStore.getMessage(Messages.NoBuildUnderSiege, this.siegeData.attacker.getName());
        }

        //no building while in pvp combat
        PlayerData playerData = GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId());
        if (playerData.inPvpCombat())
        {
            return GriefPrevention.instance.dataStore.getMessage(Messages.NoBuildPvP);
        }

        //owners can make changes, or admins with ignore claims mode enabled
        if (player.getUniqueId().equals(this.ownerID) || GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId()).ignoreClaims)
            return null;

        //anyone with explicit build permission can make changes
        if (this.hasExplicitPermission(player, ClaimPermission.Build)) return null;

        //also everyone is a member of the "public", so check for public permission
        if (ClaimPermission.Build.isGrantedBy(this.playerIDToClaimPermissionMap.get("public"))) return null;

        //allow for farming with /containertrust permission
        if (this.allowContainers(player) == null)
        {
            //do allow for farming, if player has /containertrust permission
            if (placeableForFarming(material))
            {
                return null;
            }
        }

        //subdivision permission inheritance
        if (this.parent != null)
        {
            if (player.getUniqueId().equals(this.parent.ownerID))
                return null;
            if (!inheritNothing)
                return this.parent.allowBuild(player, material);
        }

        //failure message for all other cases
        String reason = GriefPrevention.instance.dataStore.getMessage(Messages.NoBuildPermission, this.getOwnerName());
        if (player.hasPermission("griefprevention.ignoreclaims"))
            reason += "  " + GriefPrevention.instance.dataStore.getMessage(Messages.IgnoreClaimsAdvertisement);

        return reason;
    }

    public boolean hasExplicitPermission(@NotNull UUID uuid, @NotNull ClaimPermission level)
    {
        return level.isGrantedBy(this.playerIDToClaimPermissionMap.get(uuid.toString()));
    }

    public boolean hasExplicitPermission(@NotNull Player player, @NotNull ClaimPermission level)
    {
        // Check explicit ClaimPermission for UUID
        if (this.hasExplicitPermission(player.getUniqueId(), level)) return true;

        // Check permission-based ClaimPermission
        for (Map.Entry<String, ClaimPermission> stringToPermission : this.playerIDToClaimPermissionMap.entrySet())
        {
            String node = stringToPermission.getKey();
            // Ensure valid permission format for permissions - [permission.node]
            if (node.length() < 3 || node.charAt(0) != '[' || node.charAt(node.length() - 1) != ']')
            {
                continue;
            }

            // Check if level is high enough and player has node
            if (level.isGrantedBy(stringToPermission.getValue())
                    && player.hasPermission(node.substring(1, node.length() - 1)))
            {
                return true;
            }
        }

        return false;
    }

    //break permission check
    public @Nullable String allowBreak(@NotNull Player player, @NotNull Material material)
    {
        //if under siege, some blocks will be breakable
        if (this.siegeData != null || this.doorsOpen)
        {
            boolean breakable = false;

            //search for block type in list of breakable blocks
            for (int i = 0; i < GriefPrevention.instance.config_siege_blocks.size(); i++)
            {
                Material breakableMaterial = GriefPrevention.instance.config_siege_blocks.get(i);
                if (breakableMaterial == material)
                {
                    breakable = true;
                    break;
                }
            }

            //custom error messages for siege mode
            if (!breakable)
            {
                return GriefPrevention.instance.dataStore.getMessage(Messages.NonSiegeMaterial);
            }
            else if (player.getUniqueId().equals(this.ownerID))
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
    public @Nullable String allowAccess(@NotNull Player player)
    {
        //following a siege where the defender lost, the claim will allow everyone access for a time
        if (this.doorsOpen) return null;

        //admin claims need adminclaims permission only.
        if (this.isAdminClaim())
        {
            if (player.hasPermission("griefprevention.adminclaims")) return null;
        }

        //claim owner and admins in ignoreclaims mode have access
        if (player.getUniqueId().equals(this.ownerID) || GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId()).ignoreClaims)
            return null;

        //look for explicit individual access, inventory, or build permission
        if (this.hasExplicitPermission(player, ClaimPermission.Access)) return null;

        //also check for public permission
        if (ClaimPermission.Access.isGrantedBy(this.playerIDToClaimPermissionMap.get("public"))) return null;

        //permission inheritance for subdivisions
        if (this.parent != null)
        {
            if (player.getUniqueId().equals(this.parent.ownerID))
                return null;
            if (!inheritNothing)
                return this.parent.allowAccess(player);
        }

        //catch-all error message for all other cases
        String reason = GriefPrevention.instance.dataStore.getMessage(Messages.NoAccessPermission, this.getOwnerName());
        if (player.hasPermission("griefprevention.ignoreclaims"))
            reason += "  " + GriefPrevention.instance.dataStore.getMessage(Messages.IgnoreClaimsAdvertisement);
        return reason;
    }

    //inventory permission check
    public @Nullable String allowContainers(@NotNull Player player)
    {
        //if we don't know who's asking, always say no (i've been told some mods can make this happen somehow)
        //noinspection ConstantConditions // Defensive coding
        if (player == null) return "";

        //trying to access inventory in a claim may extend an existing siege to include this claim
        GriefPrevention.instance.dataStore.tryExtendSiege(player, this);

        //if under siege, nobody accesses containers
        if (this.siegeData != null)
        {
            return GriefPrevention.instance.dataStore.getMessage(Messages.NoContainersSiege, siegeData.attacker.getName());
        }

        //owner and administrators in ignoreclaims mode have access
        if (player.getUniqueId().equals(this.ownerID) || GriefPrevention.instance.dataStore.getPlayerData(player.getUniqueId()).ignoreClaims)
            return null;

        //admin claims need adminclaims permission only.
        if (this.isAdminClaim())
        {
            if (player.hasPermission("griefprevention.adminclaims")) return null;
        }

        //check for explicit individual container or build permission
        if (this.hasExplicitPermission(player, ClaimPermission.Inventory)) return null;

        //check for public container or build permission
        if (ClaimPermission.Inventory.isGrantedBy(this.playerIDToClaimPermissionMap.get("public"))) return null;

        //permission inheritance for subdivisions
        if (this.parent != null)
        {
            if (player.getUniqueId().equals(this.parent.ownerID))
                return null;
            if (!inheritNothing)
                return this.parent.allowContainers(player);
        }

        //error message for all other cases
        String reason = GriefPrevention.instance.dataStore.getMessage(Messages.NoContainersPermission, this.getOwnerName());
        if (player.hasPermission("griefprevention.ignoreclaims"))
            reason += "  " + GriefPrevention.instance.dataStore.getMessage(Messages.IgnoreClaimsAdvertisement);
        return reason;
    }

    //grant permission check, relatively simple
    public @Nullable String allowGrantPermission(@NotNull Player player)
    {
        //if we don't know who's asking, always say no (i've been told some mods can make this happen somehow)
        //noinspection ConstantConditions // Defensive coding
        if (player == null) return "";

        //anyone who can modify the claim can do this
        if (this.allowEdit(player) == null) return null;

        //anyone who's in the managers (/PermissionTrust) list can do this
        for (String managerID : this.managers)
        {
            if (managerID == null) continue;
            if (player.getUniqueId().toString().equals(managerID)) return null;

            else if (managerID.startsWith("[") && managerID.endsWith("]"))
            {
                managerID = managerID.substring(1, managerID.length() - 1);
                if (managerID.isEmpty()) continue;
                if (player.hasPermission(managerID)) return null;
            }
        }

        //permission inheritance for subdivisions
        if (this.parent != null)
        {
            if (player.getUniqueId().equals(this.parent.ownerID))
                return null;
            if (!inheritNothing)
                return this.parent.allowGrantPermission(player);
        }

        //generic error message
        String reason = GriefPrevention.instance.dataStore.getMessage(Messages.NoPermissionTrust, this.getOwnerName());
        if (player.hasPermission("griefprevention.ignoreclaims"))
            reason += "  " + GriefPrevention.instance.dataStore.getMessage(Messages.IgnoreClaimsAdvertisement);
        return reason;
    }

    @Contract("null -> null")
    public @Nullable ClaimPermission getPermission(@Nullable String playerID)
    {
        if (playerID == null || playerID.isEmpty())
        {
            return null;
        }

        return this.playerIDToClaimPermissionMap.get(playerID.toLowerCase());
    }

    //grants a permission for a player or the public
    public void setPermission(@NotNull String playerID, @Nullable ClaimPermission permissionLevel)
    {
        //noinspection ConstantConditions // Defensive coding
        if (playerID == null || playerID.isEmpty())
        {
            return;
        }

        this.playerIDToClaimPermissionMap.put(playerID.toLowerCase(), permissionLevel);
    }

    //revokes a permission for a player or the public
    public void dropPermission(@NotNull String playerID)
    {
        this.playerIDToClaimPermissionMap.remove(playerID.toLowerCase());

        for (Claim child : this.children)
        {
            child.dropPermission(playerID);
        }
    }

    //clears all permissions (except owner of course)
    public void clearPermissions()
    {
        this.playerIDToClaimPermissionMap.clear();
        this.managers.clear();

        for (Claim child : this.children)
        {
            child.clearPermissions();
        }
    }

    //gets ALL permissions
    //useful for  making copies of permissions during a claim resize and listing all permissions in a claim
    public void getPermissions(@NotNull ArrayList<String> builders, @NotNull ArrayList<String> containers,
                               @NotNull ArrayList<String> accessors, @NotNull ArrayList<String> managers)
    {
        //loop through all the entries in the hash map
        for (Map.Entry<String, ClaimPermission> entry : this.playerIDToClaimPermissionMap.entrySet())
        {
            //build up a list for each permission level
            if (entry.getValue() == ClaimPermission.Build)
            {
                builders.add(entry.getKey());
            }
            else if (entry.getValue() == ClaimPermission.Inventory)
            {
                containers.add(entry.getKey());
            }
            else
            {
                accessors.add(entry.getKey());
            }
        }

        //managers are handled a little differently
        managers.addAll(this.managers);
    }

    //returns a copy of the location representing lower x, y, z limits
    public @NotNull Location getLesserBoundaryCorner()
    {
        return this.lesserBoundaryCorner.clone();
    }

    //returns a copy of the location representing upper x, y, z limits
    //NOTE: remember upper Y will always be ignored, all claims always extend to the sky
    public @NotNull Location getGreaterBoundaryCorner()
    {
        return this.greaterBoundaryCorner.clone();
    }

    //returns a friendly owner name (for admin claims, returns "an administrator" as the owner)
    public @NotNull String getOwnerName()
    {
        if (this.parent != null)
            return this.parent.getOwnerName();

        if (this.ownerID == null)
            return GriefPrevention.instance.dataStore.getMessage(Messages.OwnerNameForAdminClaims);

        return GriefPrevention.lookupPlayerName(this.ownerID);
    }

    //whether or not a location is in a claim
    //ignoreHeight = true means location UNDER the claim will return TRUE
    //excludeSubdivisions = true means that locations inside subdivisions of the claim will return FALSE
    public boolean contains(@NotNull Location location, boolean ignoreHeight, boolean excludeSubdivisions)
    {
        //not in the same world implies false
        if (!Objects.equals(location.getWorld(), this.lesserBoundaryCorner.getWorld())) return false;

        double x = location.getX();
        double y = location.getY();
        double z = location.getZ();

        //main check
        boolean inClaim = (ignoreHeight || y >= this.lesserBoundaryCorner.getY()) &&
                x >= this.lesserBoundaryCorner.getX() &&
                x < this.greaterBoundaryCorner.getX() + 1 &&
                z >= this.lesserBoundaryCorner.getZ() &&
                z < this.greaterBoundaryCorner.getZ() + 1;

        if (!inClaim) return false;

        //additional check for subdivisions
        //you're only in a subdivision when you're also in its parent claim
        //NOTE: if a player creates subdivions then resizes the parent claim, it's possible that
        //a subdivision can reach outside of its parent's boundaries.  so this check is important!
        if (this.parent != null)
        {
            return this.parent.contains(location, ignoreHeight, false);
        }

        //code to exclude subdivisions in this check
        else if (excludeSubdivisions)
        {
            //search all subdivisions to see if the location is in any of them
            for (Claim child : this.children)
            {
                //if we find such a subdivision, return false
                if (child.contains(location, ignoreHeight, true))
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
    boolean overlaps(@NotNull Claim otherClaim)
    {
        // For help visualizing test cases, try https://silentmatt.com/rectangle-intersection/

        if (!Objects.equals(this.lesserBoundaryCorner.getWorld(), otherClaim.getLesserBoundaryCorner().getWorld())) return false;

        return !(this.getGreaterBoundaryCorner().getX() < otherClaim.getLesserBoundaryCorner().getX() ||
                this.getLesserBoundaryCorner().getX() > otherClaim.getGreaterBoundaryCorner().getX() ||
                this.getGreaterBoundaryCorner().getZ() < otherClaim.getLesserBoundaryCorner().getZ() ||
                this.getLesserBoundaryCorner().getZ() > otherClaim.getGreaterBoundaryCorner().getZ());

    }

    //whether more entities may be added to a claim
    public @Nullable String allowMoreEntities(boolean remove)
    {
        if (this.parent != null) return this.parent.allowMoreEntities(remove);

        //this rule only applies to creative mode worlds
        if (!GriefPrevention.instance.creativeRulesApply(this.getLesserBoundaryCorner())) return null;

        //admin claims aren't restricted
        if (this.isAdminClaim()) return null;

        //don't apply this rule to very large claims
        if (this.getArea() > 10000) return null;

        //determine maximum allowable entity count, based on claim size
        int maxEntities = this.getArea() / 50;
        if (maxEntities == 0) return GriefPrevention.instance.dataStore.getMessage(Messages.ClaimTooSmallForEntities);

        //count current entities (ignoring players)
        int totalEntities = 0;
        ArrayList<Chunk> chunks = this.getChunks();
        for (Chunk chunk : chunks)
        {
            Entity[] entities = chunk.getEntities();
            for (Entity entity : entities)
            {
                if (!(entity instanceof Player) && this.contains(entity.getLocation(), false, false))
                {
                    totalEntities++;
                    if (remove && totalEntities > maxEntities) entity.remove();
                }
            }
        }

        if (totalEntities >= maxEntities)
            return GriefPrevention.instance.dataStore.getMessage(Messages.TooManyEntitiesInClaim);

        return null;
    }

    public @Nullable String allowMoreActiveBlocks()
    {
        if (this.parent != null) return this.parent.allowMoreActiveBlocks();

        //determine maximum allowable entity count, based on claim size
        int maxActives = this.getArea() / 100;
        if (maxActives == 0)
            return GriefPrevention.instance.dataStore.getMessage(Messages.ClaimTooSmallForActiveBlocks);

        //count current actives
        int totalActives = 0;
        ArrayList<Chunk> chunks = this.getChunks();
        for (Chunk chunk : chunks)
        {
            BlockState[] actives = chunk.getTileEntities();
            for (BlockState active : actives)
            {
                if (BlockEventHandler.isActiveBlock(active))
                {
                    if (this.contains(active.getLocation(), false, false))
                    {
                        totalActives++;
                    }
                }
            }
        }

        if (totalActives >= maxActives)
            return GriefPrevention.instance.dataStore.getMessage(Messages.TooManyActiveBlocksInClaim);

        return null;
    }

    //implements a strict ordering of claims, used to keep the claims collection sorted for faster searching
    boolean greaterThan(@NotNull Claim otherClaim)
    {
        Location thisCorner = this.getLesserBoundaryCorner();
        Location otherCorner = otherClaim.getLesserBoundaryCorner();

        if (thisCorner.getBlockX() > otherCorner.getBlockX()) return true;

        if (thisCorner.getBlockX() < otherCorner.getBlockX()) return false;

        if (thisCorner.getBlockZ() > otherCorner.getBlockZ()) return true;

        if (thisCorner.getBlockZ() < otherCorner.getBlockZ()) return false;

        // TODO claim world handling (also this is an unused method, delete)
        return Objects.requireNonNull(thisCorner.getWorld()).getName().compareTo(Objects.requireNonNull(otherCorner.getWorld()).getName()) < 0;
    }


    long getPlayerInvestmentScore()
    {
        //decide which blocks will be considered player placed
        // TODO claim world handling
        ArrayList<Material> playerBlocks = RestoreNatureProcessingTask.getPlayerBlocks(Objects.requireNonNull(this.lesserBoundaryCorner.getWorld()).getEnvironment(), lesserBoundaryCorner.getBlock().getBiome());

        //scan the claim for player placed blocks
        double score = 0;

        boolean creativeMode = GriefPrevention.instance.creativeRulesApply(this.lesserBoundaryCorner);

        for (int x = this.lesserBoundaryCorner.getBlockX(); x <= this.greaterBoundaryCorner.getBlockX(); x++)
        {
            for (int z = this.lesserBoundaryCorner.getBlockZ(); z <= this.greaterBoundaryCorner.getBlockZ(); z++)
            {
                int y = this.lesserBoundaryCorner.getBlockY();
                for (; y < GriefPrevention.instance.getSeaLevel(this.lesserBoundaryCorner.getWorld()) - 5; y++)
                {
                    Block block = this.lesserBoundaryCorner.getWorld().getBlockAt(x, y, z);
                    if (playerBlocks.contains(block.getType()))
                    {
                        if (block.getType() == Material.CHEST && !creativeMode)
                        {
                            score += 10;
                        }
                        else
                        {
                            score += .5;
                        }
                    }
                }

                for (; y < this.lesserBoundaryCorner.getWorld().getMaxHeight(); y++)
                {
                    Block block = this.lesserBoundaryCorner.getWorld().getBlockAt(x, y, z);
                    if (playerBlocks.contains(block.getType()))
                    {
                        if (block.getType() == Material.CHEST && !creativeMode)
                        {
                            score += 10;
                        }
                        else if (creativeMode && (block.getType() == Material.LAVA))
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

        return (long) score;
    }

    public @NotNull ArrayList<Chunk> getChunks()
    {
        ArrayList<Chunk> chunks = new ArrayList<>();

        // TODO claim world handling
        World world = Objects.requireNonNull(this.getLesserBoundaryCorner().getWorld());
        Chunk lesserChunk = this.getLesserBoundaryCorner().getChunk();
        Chunk greaterChunk = this.getGreaterBoundaryCorner().getChunk();

        for (int x = lesserChunk.getX(); x <= greaterChunk.getX(); x++)
        {
            for (int z = lesserChunk.getZ(); z <= greaterChunk.getZ(); z++)
            {
                chunks.add(world.getChunkAt(x, z));
            }
        }

        return chunks;
    }

    @NotNull ArrayList<Long> getChunkHashes()
    {
        return DataStore.getChunkHashes(this);
    }
}
