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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Hopper;
import org.bukkit.block.Lectern;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.minecart.HopperMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Dispenser;
import org.bukkit.metadata.MetadataValue;

//event handlers related to blocks
public class BlockEventHandler implements Listener 
{
	//convenience reference to singleton datastore
	private DataStore dataStore;
	
	private ArrayList<Material> trashBlocks;
	
	//constructor
	public BlockEventHandler(DataStore dataStore)
	{
		this.dataStore = dataStore;
		
		//create the list of blocks which will not trigger a warning when they're placed outside of land claims
		this.trashBlocks = new ArrayList<Material>();
		this.trashBlocks.add(Material.COBBLESTONE);
		this.trashBlocks.add(Material.TORCH);
		this.trashBlocks.add(Material.DIRT);
		this.trashBlocks.add(Material.OAK_SAPLING);
		this.trashBlocks.add(Material.SPRUCE_SAPLING);
		this.trashBlocks.add(Material.BIRCH_SAPLING);
		this.trashBlocks.add(Material.JUNGLE_SAPLING);
		this.trashBlocks.add(Material.ACACIA_SAPLING);
		this.trashBlocks.add(Material.DARK_OAK_SAPLING);
		this.trashBlocks.add(Material.GRAVEL);
		this.trashBlocks.add(Material.SAND);
		this.trashBlocks.add(Material.TNT);
		this.trashBlocks.add(Material.CRAFTING_TABLE);
	}
	
	//when a player breaks a block...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent breakEvent)
	{
		Player player = breakEvent.getPlayer();
		Block block = breakEvent.getBlock();
		
		//make sure the player is allowed to break at the location
		String noBuildReason = GriefPrevention.instance.allowBreak(player, block, block.getLocation(), breakEvent);
		if(noBuildReason != null)
		{
			GriefPrevention.sendMessage(player, TextMode.Err, noBuildReason);
			breakEvent.setCancelled(true);
			return;
		}
	}
	
	//when a player places a sign...
	@EventHandler(ignoreCancelled = true)
	public void onSignChanged(SignChangeEvent event)
	{
	    //send sign content to online administrators
	    if(!GriefPrevention.instance.config_signNotifications) return;
	    
	    Player player = event.getPlayer();
		if(player == null) return;
		
		StringBuilder lines = new StringBuilder(" placed a sign @ " + GriefPrevention.getfriendlyLocationString(event.getBlock().getLocation()));
		boolean notEmpty = false;
		for(int i = 0; i < event.getLines().length; i++)
		{
			String withoutSpaces = event.getLine(i).replace(" ", ""); 
		    if(!withoutSpaces.isEmpty())
	        {
		        notEmpty = true;
		        lines.append("\n  " + event.getLine(i));
	        }
		}
		
		String signMessage = lines.toString();
		
		//prevent signs with blocked IP addresses 
		if(!player.hasPermission("griefprevention.spam") && GriefPrevention.instance.containsBlockedIP(signMessage))
        {
            event.setCancelled(true);
            return;
        }
		
		PlayerData playerData = this.dataStore.getPlayerData(player.getUniqueId());
		//if not empty and wasn't the same as the last sign, log it and remember it for later
		//This has been temporarily removed since `signMessage` includes location, not just the message. Waste of memory IMO
		//if(notEmpty && (playerData.lastSignMessage == null || !playerData.lastSignMessage.equals(signMessage)))
		if (notEmpty)
		{		
			GriefPrevention.AddLogEntry(player.getName() + lines.toString().replace("\n  ", ";"), null);
			PlayerEventHandler.makeSocialLogEntry(player.getName(), signMessage);
			//playerData.lastSignMessage = signMessage;
			
			if(!player.hasPermission("griefprevention.eavesdropsigns"))
			{
				@SuppressWarnings("unchecked")
                Collection<Player> players = (Collection<Player>)GriefPrevention.instance.getServer().getOnlinePlayers();
				for(Player otherPlayer : players)
				{
					if(otherPlayer.hasPermission("griefprevention.eavesdropsigns"))
					{
						otherPlayer.sendMessage(ChatColor.GRAY + player.getName() + signMessage);
					}
				}
			}
		}
	}
	
	//when a player places multiple blocks...
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlocksPlace(BlockMultiPlaceEvent placeEvent)
	{
	    Player player = placeEvent.getPlayer();
	    
	    //don't track in worlds where claims are not enabled
        if(!GriefPrevention.instance.claimsEnabledForWorld(placeEvent.getBlock().getWorld())) return;
        
        //make sure the player is allowed to build at the location
        for(BlockState block : placeEvent.getReplacedBlockStates())
        {
            String noBuildReason = GriefPrevention.instance.allowBuild(player, block.getLocation(), block.getType());
            if(noBuildReason != null)
            {
                GriefPrevention.sendMessage(player, TextMode.Err, noBuildReason);
                placeEvent.setCancelled(true);
                return;
            }
        }
	}
	
	private boolean doesAllowFireProximityInWorld(World world) {
		if (GriefPrevention.instance.pvpRulesApply(world)) {
			return GriefPrevention.instance.config_pvp_allowFireNearPlayers;
		} else {
			return GriefPrevention.instance.config_pvp_allowFireNearPlayers_NonPvp;
		}
	}
	
	//when a player places a block...
	@SuppressWarnings("null")
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockPlace(BlockPlaceEvent placeEvent)
	{
		Player player = placeEvent.getPlayer();
		Block block = placeEvent.getBlock();
				
		//FEATURE: limit fire placement, to prevent PvP-by-fire
		
		//if placed block is fire and pvp is off, apply rules for proximity to other players 
		if(block.getType() == Material.FIRE && !doesAllowFireProximityInWorld(block.getWorld()))
		{
			List<Player> players = block.getWorld().getPlayers();
			for(int i = 0; i < players.size(); i++)
			{
				Player otherPlayer = players.get(i);

				// Ignore players in creative or spectator mode to avoid users from checking if someone is spectating near them
				if(otherPlayer.getGameMode() == GameMode.CREATIVE || otherPlayer.getGameMode() == GameMode.SPECTATOR) {
					continue;
				}

				Location location = otherPlayer.getLocation();
				if(!otherPlayer.equals(player) && location.distanceSquared(block.getLocation()) < 9 && player.canSee(otherPlayer))
				{
					GriefPrevention.sendMessage(player, TextMode.Err, Messages.PlayerTooCloseForFire2);
					placeEvent.setCancelled(true);
					return;
				}					
			}
		}
		
		//don't track in worlds where claims are not enabled
        if(!GriefPrevention.instance.claimsEnabledForWorld(placeEvent.getBlock().getWorld())) return;
		
		//make sure the player is allowed to build at the location
		String noBuildReason = GriefPrevention.instance.allowBuild(player, block.getLocation(), block.getType());
		if(noBuildReason != null)
		{
			// Allow players with container trust to place books in lecterns
			PlayerData playerData = this.dataStore.getPlayerData(player.getUniqueId());
			Claim claim = this.dataStore.getClaimAt(block.getLocation(), true, playerData.lastClaim);
			if (block.getType() == Material.LECTERN && placeEvent.getBlockReplacedState().getType() == Material.LECTERN)
			{
				if (claim != null)
				{
					playerData.lastClaim = claim;
					String noContainerReason = claim.allowContainers(player);
					if (noContainerReason == null)
						return;

					placeEvent.setCancelled(true);
					GriefPrevention.sendMessage(player, TextMode.Err, noContainerReason);
					return;
				}
			}
			GriefPrevention.sendMessage(player, TextMode.Err, noBuildReason);
			placeEvent.setCancelled(true);
			return;
		}
		
		//if the block is being placed within or under an existing claim
		PlayerData playerData = this.dataStore.getPlayerData(player.getUniqueId());
		Claim claim = this.dataStore.getClaimAt(block.getLocation(), true, playerData.lastClaim);
		if(claim != null)
		{
		    playerData.lastClaim = claim;
		    
			//warn about TNT not destroying claimed blocks
            if(block.getType() == Material.TNT && !claim.areExplosivesAllowed && playerData.siegeData == null)
            {
                GriefPrevention.sendMessage(player, TextMode.Warn, Messages.NoTNTDamageClaims);
                GriefPrevention.sendMessage(player, TextMode.Instr, Messages.ClaimExplosivesAdvertisement);
            }
			
			//if the player has permission for the claim and he's placing UNDER the claim
			if(block.getY() <= claim.lesserBoundaryCorner.getBlockY() && claim.allowBuild(player, block.getType()) == null)
			{
				//extend the claim downward
				this.dataStore.extendClaim(claim, block.getY() - GriefPrevention.instance.config_claims_claimsExtendIntoGroundDistance);
			}
			
			//allow for a build warning in the future 
			playerData.warnedAboutBuildingOutsideClaims = false;
		}
		
		//FEATURE: automatically create a claim when a player who has no claims places a chest
		
		//otherwise if there's no claim, the player is placing a chest, and new player automatic claims are enabled
		else if(GriefPrevention.instance.config_claims_automaticClaimsForNewPlayersRadius > -1 && player.hasPermission("griefprevention.createclaims") && block.getType() == Material.CHEST)
		{			
			//if the chest is too deep underground, don't create the claim and explain why
			if(GriefPrevention.instance.config_claims_preventTheft && block.getY() < GriefPrevention.instance.config_claims_maxDepth)
			{
				GriefPrevention.sendMessage(player, TextMode.Warn, Messages.TooDeepToClaim);
				return;
			}
			
			int radius = GriefPrevention.instance.config_claims_automaticClaimsForNewPlayersRadius;
			
			//if the player doesn't have any claims yet, automatically create a claim centered at the chest
			if(playerData.getClaims().size() == 0)
			{
				//radius == 0 means protect ONLY the chest
				if(GriefPrevention.instance.config_claims_automaticClaimsForNewPlayersRadius == 0)
				{					
					this.dataStore.createClaim(block.getWorld(), block.getX(), block.getX(), block.getY(), block.getY(), block.getZ(), block.getZ(), player.getUniqueId(), null, null, player);
					GriefPrevention.sendMessage(player, TextMode.Success, Messages.ChestClaimConfirmation);					
				}
				
				//otherwise, create a claim in the area around the chest
				else
				{
				    //if failure due to insufficient claim blocks available
                    if(playerData.getRemainingClaimBlocks() < 1)
                    {
                        GriefPrevention.sendMessage(player, TextMode.Warn, Messages.NoEnoughBlocksForChestClaim);
                        return;
                    }
				    
				    //as long as the automatic claim overlaps another existing claim, shrink it
					//note that since the player had permission to place the chest, at the very least, the automatic claim will include the chest
					CreateClaimResult result = null;
				    while(radius >= 0)
					{
				        int area = (radius * 2 + 1) * (radius * 2 + 1);
				        if(playerData.getRemainingClaimBlocks() >= area)
				        {
			                result = this.dataStore.createClaim(
		                        block.getWorld(), 
	                            block.getX() - radius, block.getX() + radius, 
	                            block.getY() - GriefPrevention.instance.config_claims_claimsExtendIntoGroundDistance, block.getY(), 
	                            block.getZ() - radius, block.getZ() + radius, 
	                            player.getUniqueId(), 
	                            null, null,
	                            player);
			                
			                if(result.succeeded) break;
				        }
				        
				        radius--;
					}
					
					if(result != null && result.succeeded)
					{
    					//notify and explain to player
    					GriefPrevention.sendMessage(player, TextMode.Success, Messages.AutomaticClaimNotification);
    					
    					//show the player the protected area
    					Visualization visualization = Visualization.FromClaim(result.claim, block.getY(), VisualizationType.Claim, player.getLocation());
    					Visualization.Apply(player, visualization);
					}
				}
				
				GriefPrevention.sendMessage(player, TextMode.Instr, Messages.SurvivalBasicsVideo2, DataStore.SURVIVAL_VIDEO_URL);
			}
			
			//check to see if this chest is in a claim, and warn when it isn't
			if(GriefPrevention.instance.config_claims_preventTheft && this.dataStore.getClaimAt(block.getLocation(), false, playerData.lastClaim) == null)
			{
				GriefPrevention.sendMessage(player, TextMode.Warn, Messages.UnprotectedChestWarning);				
			}
		}
		
		//FEATURE: limit wilderness tree planting to grass, or dirt with more blocks beneath it
		else if(Tag.SAPLINGS.isTagged(block.getType()) && GriefPrevention.instance.config_blockSkyTrees && GriefPrevention.instance.claimsEnabledForWorld(player.getWorld()))
		{
			Block earthBlock = placeEvent.getBlockAgainst();
			if(earthBlock.getType() != Material.GRASS)
			{
				if(earthBlock.getRelative(BlockFace.DOWN).getType() == Material.AIR || 
				   earthBlock.getRelative(BlockFace.DOWN).getRelative(BlockFace.DOWN).getType() == Material.AIR)
				{
					placeEvent.setCancelled(true);
				}
			}
		}	
		
		//FEATURE: warn players when they're placing non-trash blocks outside of their claimed areas
		else if(!this.trashBlocks.contains(block.getType()) && GriefPrevention.instance.claimsEnabledForWorld(block.getWorld()))
		{
			if(!playerData.warnedAboutBuildingOutsideClaims && !player.hasPermission("griefprevention.adminclaims")
				&& player.hasPermission("griefprevention.createclaims") && ((playerData.lastClaim == null
				&& playerData.getClaims().size() == 0) || (playerData.lastClaim != null
				&& playerData.lastClaim.isNear(player.getLocation(), 15))))
			{
				Long now = null;
			    if(playerData.buildWarningTimestamp == null || (now = System.currentTimeMillis()) - playerData.buildWarningTimestamp > 600000)  //10 minute cooldown
			    {
    			    GriefPrevention.sendMessage(player, TextMode.Warn, Messages.BuildingOutsideClaims);
    				playerData.warnedAboutBuildingOutsideClaims = true;
    				
    				if(now == null) now = System.currentTimeMillis();
    				playerData.buildWarningTimestamp = now;
    				
    				if(playerData.getClaims().size() < 2)
    				{
    				    GriefPrevention.sendMessage(player, TextMode.Instr, Messages.SurvivalBasicsVideo2, DataStore.SURVIVAL_VIDEO_URL);
    				}
    				
    				if(playerData.lastClaim != null)
    				{
    				    Visualization visualization = Visualization.FromClaim(playerData.lastClaim, block.getY(), VisualizationType.Claim, player.getLocation());
    				    Visualization.Apply(player, visualization);
    				}
			    }
			}
		}
		
		//warn players when they place TNT above sea level, since it doesn't destroy blocks there
		if(	GriefPrevention.instance.config_blockSurfaceOtherExplosions && block.getType() == Material.TNT &&
			block.getWorld().getEnvironment() != Environment.NETHER &&
			block.getY() > GriefPrevention.instance.getSeaLevel(block.getWorld()) - 5 &&
			claim == null &&
			playerData.siegeData == null)
		{
			GriefPrevention.sendMessage(player, TextMode.Warn, Messages.NoTNTDamageAboveSeaLevel);
		}
		
		//warn players about disabled pistons outside of land claims
		if( GriefPrevention.instance.config_pistonsInClaimsOnly && 
	        (block.getType() == Material.PISTON || block.getType() == Material.STICKY_PISTON) &&
	        claim == null )
		{
		    GriefPrevention.sendMessage(player, TextMode.Warn, Messages.NoPistonsOutsideClaims);
		}
		
		//limit active blocks in creative mode worlds
		if(!player.hasPermission("griefprevention.adminclaims") && GriefPrevention.instance.creativeRulesApply(block.getLocation()) && isActiveBlock(block))
		{
    		String noPlaceReason = claim.allowMoreActiveBlocks();
            if(noPlaceReason != null)
            {
                GriefPrevention.sendMessage(player, TextMode.Err, noPlaceReason);
                placeEvent.setCancelled(true);
                return;
            }
		}
	}
	
	static boolean isActiveBlock(Block block)
	{
	    return isActiveBlock(block.getType());
	}
	
	static boolean isActiveBlock(BlockState state)
	{
	    return isActiveBlock(state.getType());
	}
	
	static boolean isActiveBlock(Material type)
	{
	    if(type == Material.HOPPER || type == Material.BEACON || type == Material.SPAWNER) return true;
	    return false;
	}
	
	//blocks "pushing" other players' blocks around (pistons)
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockPistonExtend (BlockPistonExtendEvent event)
	{		
	    //return if piston checks are not enabled
	    if(!GriefPrevention.instance.config_checkPistonMovement) return;
	    
	    //pushing down is ALWAYS safe
	    if(event.getDirection() == BlockFace.DOWN) return;
	    
	    //don't track in worlds where claims are not enabled
        if(!GriefPrevention.instance.claimsEnabledForWorld(event.getBlock().getWorld())) return;
	    
	    Block pistonBlock = event.getBlock();
	    List<Block> blocks = event.getBlocks();
		
		//if no blocks moving, then only check to make sure we're not pushing into a claim from outside
		//this avoids pistons breaking non-solids just inside a claim, like torches, doors, and touchplates
		if(blocks.size() == 0)
		{
			Block invadedBlock = pistonBlock.getRelative(event.getDirection());
			
			//pushing "air" is harmless
			if(invadedBlock.getType() == Material.AIR) return;
			
			if(	this.dataStore.getClaimAt(pistonBlock.getLocation(), false, null) == null && 
				this.dataStore.getClaimAt(invadedBlock.getLocation(), false, null) != null)
			{
				event.setCancelled(true);				
			}
			
			return;
		}
		
		//who owns the piston, if anyone?
		String pistonClaimOwnerName = "_";
		Claim claim = this.dataStore.getClaimAt(event.getBlock().getLocation(), false, null);
		if(claim != null) pistonClaimOwnerName = claim.getOwnerName();
		
		//if pistons are limited to same-claim block movement
		if(GriefPrevention.instance.config_pistonsInClaimsOnly)
		{
		    //if piston is not in a land claim, cancel event
		    if(claim == null)
	        {
		        event.setCancelled(true);
		        return;
	        }
		    
		    for(Block pushedBlock : event.getBlocks())
		    {
		        //if pushing blocks located outside the land claim it lives in, cancel the event
	            if(!claim.contains(pushedBlock.getLocation(), false, false))
		        {
		            event.setCancelled(true);
		            return;
		        }
		        
		        //if pushing a block inside the claim out of the claim, cancel the event
	            //reason: could push into another land claim, don't want to spend CPU checking for that
	            //reason: push ice out, place torch, get water outside the claim
	            if(!claim.contains(pushedBlock.getRelative(event.getDirection()).getLocation(), false, false))
                {
                    event.setCancelled(true);
                    return;
                }
		    }
		}
		
		//otherwise, consider ownership of piston and EACH pushed block
		else
		{
		    //which blocks are being pushed?
		    Claim cachedClaim = claim;
		    for(int i = 0; i < blocks.size(); i++)
    		{
    			//if ANY of the pushed blocks are owned by someone other than the piston owner, cancel the event
    			Block block = blocks.get(i);
    			claim = this.dataStore.getClaimAt(block.getLocation(), false, cachedClaim);
    			if(claim != null)
    			{
    			    cachedClaim = claim;
    			    if(!claim.getOwnerName().equals(pistonClaimOwnerName))
    			    {
        				event.setCancelled(true);
        				pistonBlock.getWorld().createExplosion(pistonBlock.getLocation(), 0);
        				pistonBlock.getWorld().dropItem(pistonBlock.getLocation(), new ItemStack(pistonBlock.getType()));
        				pistonBlock.setType(Material.AIR);
        				return;
    			    }
    			}
    		}
    		
			//if any of the blocks are being pushed into a claim from outside, cancel the event
    		for(int i = 0; i < blocks.size(); i++)
			{
				Block block = blocks.get(i);
				Claim originalClaim = this.dataStore.getClaimAt(block.getLocation(), false, cachedClaim);
				String originalOwnerName = "";
				if(originalClaim != null)
				{
					cachedClaim = originalClaim;
				    originalOwnerName = originalClaim.getOwnerName();
				}
				
				Claim newClaim = this.dataStore.getClaimAt(block.getRelative(event.getDirection()).getLocation(), false, cachedClaim);
				String newOwnerName = "";
				if(newClaim != null)
				{
				    newOwnerName = newClaim.getOwnerName();
				}
				
				//if pushing this block will change ownership, cancel the event and take away the piston (for performance reasons)
				if(!newOwnerName.equals(originalOwnerName) && !newOwnerName.isEmpty())
				{
					event.setCancelled(true);
					pistonBlock.getWorld().createExplosion(pistonBlock.getLocation(), 0);
					pistonBlock.getWorld().dropItem(pistonBlock.getLocation(), new ItemStack(pistonBlock.getType()));
					pistonBlock.setType(Material.AIR);
					return;
				}
			}
		}
	}
	
	//blocks theft by pulling blocks out of a claim (again pistons)
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockPistonRetract (BlockPistonRetractEvent event)
	{
	    //return if piston checks are not enabled
        if(!GriefPrevention.instance.config_checkPistonMovement) return;
        
	    //pulling up is always safe
		if(event.getDirection() == BlockFace.UP) return;
		
		try
		{
    		//don't track in worlds where claims are not enabled
            if(!GriefPrevention.instance.claimsEnabledForWorld(event.getBlock().getWorld())) return;
    		
    		//if pistons limited to only pulling blocks which are in the same claim the piston is in
    		if(GriefPrevention.instance.config_pistonsInClaimsOnly)
    		{
    		    //if piston not in a land claim, cancel event
    		    Claim pistonClaim = this.dataStore.getClaimAt(event.getBlock().getLocation(), false, null);
    		    if(pistonClaim == null && !event.getBlocks().isEmpty())
    		    {
    		        event.setCancelled(true);
    		        return;
    		    }
    		    
    		    for(Block movedBlock : event.getBlocks())
    		    {
    		        //if pulled block isn't in the same land claim, cancel the event
        		    if(!pistonClaim.contains(movedBlock.getLocation(), false, false))
        		    {
        		        event.setCancelled(true);
        		        return;
        		    }
    		    }
    		}
    		
    		//otherwise, consider ownership of both piston and block
    		else
    		{
    		    //who owns the piston, if anyone?
                String pistonOwnerName = "_";
                Block block = event.getBlock();
                Location pistonLocation = block.getLocation();       
                Claim pistonClaim = this.dataStore.getClaimAt(pistonLocation, false, null);
                if(pistonClaim != null) pistonOwnerName = pistonClaim.getOwnerName();
    		    
    		    String movingBlockOwnerName = "_";
        		for(Block movedBlock : event.getBlocks())
        		{
        		    //who owns the moving block, if anyone?
                    Claim movingBlockClaim = this.dataStore.getClaimAt(movedBlock.getLocation(), false, pistonClaim);
            		if(movingBlockClaim != null) movingBlockOwnerName = movingBlockClaim.getOwnerName();
            		
            		//if there are owners for the blocks, they must be the same player
            		//otherwise cancel the event
            		if(!pistonOwnerName.equals(movingBlockOwnerName))
            		{
            			event.setCancelled(true);
            			block.getWorld().createExplosion(block.getLocation(), 0);
            			block.getWorld().dropItem(block.getLocation(), new ItemStack(Material.STICKY_PISTON));
            			block.setType(Material.AIR);
                        return;
            		}
        		}
    		}
		}
		catch(NoSuchMethodError exception)
		{
		    GriefPrevention.AddLogEntry("Your server is running an outdated version of 1.8 which has a griefing vulnerability.  Update your server (reruns buildtools.jar to get an updated server JAR file) to ensure players can't steal claimed blocks using pistons.");
		}
	}
	
	//blocks are ignited ONLY by flint and steel (not by being near lava, open flames, etc), unless configured otherwise
	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockIgnite (BlockIgniteEvent igniteEvent)
	{
	    //don't track in worlds where claims are not enabled
        if(!GriefPrevention.instance.claimsEnabledForWorld(igniteEvent.getBlock().getWorld())) return;

        if(igniteEvent.getCause() == IgniteCause.LIGHTNING && GriefPrevention.instance.dataStore.getClaimAt(igniteEvent.getIgnitingEntity().getLocation(), false, null) != null){
//        	if(igniteEvent.getIgnitingEntity().hasMetadata("GP_TRIDENT")){ //BlockIgniteEvent is called before LightningStrikeEvent. See #532
        		igniteEvent.setCancelled(true);
//			}
        }
	    
	    if(!GriefPrevention.instance.config_fireSpreads && igniteEvent.getCause() != IgniteCause.FLINT_AND_STEEL &&  igniteEvent.getCause() != IgniteCause.LIGHTNING)
		{	
			igniteEvent.setCancelled(true);			
		}
	}
	
	//fire doesn't spread unless configured to, but other blocks still do (mushrooms and vines, for example)
	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockSpread (BlockSpreadEvent spreadEvent)
	{
		if(spreadEvent.getSource().getType() != Material.FIRE) return;
		
		//don't track in worlds where claims are not enabled
        if(!GriefPrevention.instance.claimsEnabledForWorld(spreadEvent.getBlock().getWorld())) return;
        
		if(!GriefPrevention.instance.config_fireSpreads)
		{
			spreadEvent.setCancelled(true);
			
			Block underBlock = spreadEvent.getSource().getRelative(BlockFace.DOWN);
			if(underBlock.getType() != Material.NETHERRACK)
			{
				spreadEvent.getSource().setType(Material.AIR);
			}
			
			return;
		}
		
		//never spread into a claimed area, regardless of settings
		if(this.dataStore.getClaimAt(spreadEvent.getBlock().getLocation(), false, null) != null)
		{
			if(GriefPrevention.instance.config_claims_firespreads) return;
			spreadEvent.setCancelled(true);
			
			//if the source of the spread is not fire on netherrack, put out that source fire to save cpu cycles
			Block source = spreadEvent.getSource();
			if(source.getRelative(BlockFace.DOWN).getType() != Material.NETHERRACK)
			{
				source.setType(Material.AIR);
			}			
		}
	}
	
	//blocks are not destroyed by fire, unless configured to do so
	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBurn (BlockBurnEvent burnEvent)
	{
		//don't track in worlds where claims are not enabled
		if(!GriefPrevention.instance.claimsEnabledForWorld(burnEvent.getBlock().getWorld())) return;
        
		if(!GriefPrevention.instance.config_fireDestroys)
		{
			burnEvent.setCancelled(true);
			Block block = burnEvent.getBlock();
			Block [] adjacentBlocks = new Block []
			{
				block.getRelative(BlockFace.UP),
				block.getRelative(BlockFace.DOWN),
				block.getRelative(BlockFace.NORTH),
				block.getRelative(BlockFace.SOUTH),
				block.getRelative(BlockFace.EAST),
				block.getRelative(BlockFace.WEST)
			};
			
			//pro-actively put out any fires adjacent the burning block, to reduce future processing here
			for(int i = 0; i < adjacentBlocks.length; i++)
			{
				Block adjacentBlock = adjacentBlocks[i];
				if(adjacentBlock.getType() == Material.FIRE && adjacentBlock.getRelative(BlockFace.DOWN).getType() != Material.NETHERRACK)
				{
					adjacentBlock.setType(Material.AIR);
				}
			}
			
			Block aboveBlock = block.getRelative(BlockFace.UP);
			if(aboveBlock.getType() == Material.FIRE)
			{
				aboveBlock.setType(Material.AIR);
			}
			return;
		}
		
		//never burn claimed blocks, regardless of settings
		if(this.dataStore.getClaimAt(burnEvent.getBlock().getLocation(), false, null) != null)
		{
			if(GriefPrevention.instance.config_claims_firedamages) return;
			burnEvent.setCancelled(true);
		}
	}


	
	//ensures fluids don't flow into land claims from outside
	private Claim lastSpreadClaim = null;
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onBlockFromTo (BlockFromToEvent spreadEvent)
	{
	    //always allow fluids to flow straight down
        if(spreadEvent.getFace() == BlockFace.DOWN) return;
        
	    //don't track in worlds where claims are not enabled
		if(!GriefPrevention.instance.claimsEnabledForWorld(spreadEvent.getBlock().getWorld())) return;
		
		//where to?
        Block toBlock = spreadEvent.getToBlock();
        Location toLocation = toBlock.getLocation();
        Claim toClaim = this.dataStore.getClaimAt(toLocation, false, lastSpreadClaim);
        
        //if into a land claim, it must be from the same land claim
        if(toClaim != null)
        {
            this.lastSpreadClaim = toClaim;
            if(!toClaim.contains(spreadEvent.getBlock().getLocation(), false, true))
            {
                //exception: from parent into subdivision
                if(toClaim.parent == null || !toClaim.parent.contains(spreadEvent.getBlock().getLocation(), false, false))
                {
                    spreadEvent.setCancelled(true);
                }
            }
        }
        
        //otherwise if creative mode world, don't flow
        else if(GriefPrevention.instance.creativeRulesApply(toLocation))
        {
            spreadEvent.setCancelled(true);
        }
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onForm(BlockFormEvent event)
	{
	    Block block = event.getBlock();
	    Location location = block.getLocation();
	    
	    if(GriefPrevention.instance.creativeRulesApply(location))
	    {
	        Material type = block.getType();
	        if(type == Material.COBBLESTONE || type == Material.OBSIDIAN || type == Material.LAVA || type == Material.WATER)
	        {
	            Claim claim = GriefPrevention.instance.dataStore.getClaimAt(location, false, null);
	            if(claim == null)
	            {
	                event.setCancelled(true);
	            }
	        }
	    }
	}

	//Stop projectiles from destroying blocks that don't fire a proper event
	@EventHandler(ignoreCancelled = true)
	private void chorusFlower(ProjectileHitEvent event)
	{
		//don't track in worlds where claims are not enabled
		if(!GriefPrevention.instance.claimsEnabledForWorld(event.getEntity().getWorld())) return;

		if (event.getHitBlock() == null || event.getHitBlock().getType() != Material.CHORUS_FLOWER)
			return;

		Block block = event.getHitBlock();

		Claim claim = dataStore.getClaimAt(block.getLocation(), false, null);
		if (claim == null)
			return;

		Player shooter = null;
		Projectile projectile = event.getEntity();

		if (projectile.getShooter() instanceof Player)
			shooter = (Player)projectile.getShooter();

		if (shooter == null)
		{
			event.getHitBlock().setType(Material.AIR);
			Bukkit.getScheduler().runTask(GriefPrevention.instance, () -> event.getHitBlock().setType(Material.CHORUS_FLOWER));
			return;
		}

		String allowContainer = claim.allowContainers(shooter);

		if (allowContainer != null)
		{
			event.getHitBlock().setType(Material.AIR);
			Bukkit.getScheduler().runTask(GriefPrevention.instance, () -> event.getHitBlock().setType(Material.CHORUS_FLOWER));
			GriefPrevention.sendMessage(shooter, TextMode.Err, allowContainer);
			return;
		}
	}
	
	//ensures dispensers can't be used to dispense a block(like water or lava) or item across a claim boundary
	@EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
	public void onDispense(BlockDispenseEvent dispenseEvent)
	{
	    //don't track in worlds where claims are not enabled
        if(!GriefPrevention.instance.claimsEnabledForWorld(dispenseEvent.getBlock().getWorld())) return;
	    
	    //from where?
		Block fromBlock = dispenseEvent.getBlock();
		@SuppressWarnings("deprecation")
        Dispenser dispenser = new Dispenser(Material.DISPENSER, fromBlock.getData());
		
		//to where?
		Block toBlock = fromBlock.getRelative(dispenser.getFacing());
		Claim fromClaim = this.dataStore.getClaimAt(fromBlock.getLocation(), false, null);
		Claim toClaim = this.dataStore.getClaimAt(toBlock.getLocation(), false, fromClaim);
		
		//into wilderness is NOT OK in creative mode worlds
		Material materialDispensed = dispenseEvent.getItem().getType();
		if((materialDispensed == Material.WATER_BUCKET || materialDispensed == Material.LAVA_BUCKET) && GriefPrevention.instance.creativeRulesApply(dispenseEvent.getBlock().getLocation()) && toClaim == null)
		{
			dispenseEvent.setCancelled(true);
			return;
		}
		
		//wilderness to wilderness is OK
		if(fromClaim == null && toClaim == null) return;
		
		//within claim is OK
		if(fromClaim == toClaim) return;
		
		//everything else is NOT OK
		dispenseEvent.setCancelled(true);
	}
	
	@EventHandler(ignoreCancelled = true)
    public void onTreeGrow (StructureGrowEvent growEvent)
    {
        //only take these potentially expensive steps if configured to do so
	    if(!GriefPrevention.instance.config_limitTreeGrowth) return;
	    
	    //don't track in worlds where claims are not enabled
        if(!GriefPrevention.instance.claimsEnabledForWorld(growEvent.getWorld())) return;
	    
	    Location rootLocation = growEvent.getLocation();
        Claim rootClaim = this.dataStore.getClaimAt(rootLocation, false, null);
        String rootOwnerName = null;
        
        //who owns the spreading block, if anyone?
        if(rootClaim != null)
        {
            //tree growth in subdivisions is dependent on who owns the top level claim
            if(rootClaim.parent != null) rootClaim = rootClaim.parent;
            
            //if an administrative claim, just let the tree grow where it wants
            if(rootClaim.isAdminClaim()) return;
            
            //otherwise, note the owner of the claim
            rootOwnerName = rootClaim.getOwnerName();
        }
        
        //for each block growing
        for(int i = 0; i < growEvent.getBlocks().size(); i++)
        {
            BlockState block = growEvent.getBlocks().get(i);
            Claim blockClaim = this.dataStore.getClaimAt(block.getLocation(), false, rootClaim);
            
            //if it's growing into a claim
            if(blockClaim != null)
            {
                //if there's no owner for the new tree, or the owner for the new tree is different from the owner of the claim
                if(rootOwnerName == null  || !rootOwnerName.equals(blockClaim.getOwnerName()))
                {
                    growEvent.getBlocks().remove(i--);
                }
            }
        }
    }
	
	@EventHandler(ignoreCancelled = true)
    public void onInventoryPickupItem (InventoryPickupItemEvent event) 
    {
	    //prevent hoppers from picking-up items dropped by players on death

	    InventoryHolder holder = event.getInventory().getHolder();
	    if(holder instanceof HopperMinecart || holder instanceof Hopper)
	    {
	        Item item = event.getItem();
	        List<MetadataValue> data = item.getMetadata("GP_ITEMOWNER");
	        //if this is marked as belonging to a player
	        if(data != null && data.size() > 0)
	        {
	        	 UUID ownerID = (UUID)data.get(0).value();
	 		    
	 		    //has that player unlocked his drops?
	 		    OfflinePlayer owner = GriefPrevention.instance.getServer().getOfflinePlayer(ownerID);
	 		    if(owner.isOnline())
	 		    {
	 		        PlayerData playerData = this.dataStore.getPlayerData(ownerID);

	                 //if locked, don't allow pickup
	 		        if(!playerData.dropsAreUnlocked)
	 		        {
	 		        	event.setCancelled(true);
	 		        }
	 		    }
	        }
	    }
	}
}
