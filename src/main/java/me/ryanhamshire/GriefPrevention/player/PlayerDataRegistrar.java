package me.ryanhamshire.GriefPrevention.player;

import me.ryanhamshire.GriefPrevention.GriefPrevention;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author RoboMWM
 * Created on 5/25/2017.
 */
public class PlayerDataRegistrar
{
    //in-memory cache for player storage
    private ConcurrentHashMap<UUID, PlayerData> playerNameToPlayerDataMap = new ConcurrentHashMap<UUID, PlayerData>();

    //retrieves player storage from memory or secondary storage, as necessary
    //if the player has never been on the server before, this will return a fresh player storage with default values
    public PlayerData getPlayerData(UUID playerID)
    {
        //first, look in memory
        PlayerData playerData = this.playerNameToPlayerDataMap.get(playerID);

        //if not there, build a fresh instance with some blanks for what may be in secondary storage
        //TODO: why are we doing this
        if(playerData == null)
        {
            playerData = new PlayerData();
            playerData.playerID = playerID;

            //shove that new player storage into the hash map cache
            this.playerNameToPlayerDataMap.put(playerID, playerData);
        }

        return playerData;
    }

    private void loadDataFromSecondaryStorage()
    {
        //reach out to secondary storage to get any storage there
        PlayerData storageData = GriefPrevention.instance.storage.getPlayerData(this.playerID);

        if(this.accruedClaimBlocks == null)
        {
            if(storageData.accruedClaimBlocks != null)
            {
                this.accruedClaimBlocks = storageData.accruedClaimBlocks;

                //ensure at least minimum accrued are accrued (in case of settings changes to increase initial amount)
                if(this.accruedClaimBlocks < GriefPrevention.instance.config_claims_initialBlocks)
                {
                    this.accruedClaimBlocks = GriefPrevention.instance.config_claims_initialBlocks;
                }

            }
            else
            {
                this.accruedClaimBlocks = GriefPrevention.instance.config_claims_initialBlocks;
            }
        }

        if(this.bonusClaimBlocks == null)
        {
            if(storageData.bonusClaimBlocks != null)
            {
                this.bonusClaimBlocks = storageData.bonusClaimBlocks;
            }
            else
            {
                this.bonusClaimBlocks = 0;
            }
        }
    }
}
