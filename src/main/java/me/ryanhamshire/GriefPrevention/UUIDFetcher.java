//BIG THANKS to EvilMidget38 for providing this handy UUID lookup tool to the Bukkit community!  :)

package me.ryanhamshire.GriefPrevention;

import com.google.common.base.Charsets;
import org.bukkit.OfflinePlayer;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
 
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;
 
class UUIDFetcher {
    private static int PROFILES_PER_REQUEST = 100;
    private static final String PROFILE_URL = "https://api.mojang.com/profiles/minecraft";
    private final JSONParser jsonParser = new JSONParser();
    private final List<String> names;
    private final boolean rateLimiting;
    
    //cache for username -> uuid lookups
    static HashMap<String, UUID> lookupCache;
    
    //record of username -> proper casing updates
    static HashMap<String, String> correctedNames;
 
    public UUIDFetcher(List<String> names, boolean rateLimiting) {
        this.names = names;
        this.rateLimiting = rateLimiting;
    }
 
    public UUIDFetcher(List<String> names) {
        this(names, true);
    }
 
    public void call() throws Exception
    {
        if(lookupCache == null)
        {
            lookupCache = new HashMap<String, UUID>();
        }
        
        if(correctedNames == null)
        {
            correctedNames = new HashMap<String, String>();
        }
        
        GriefPrevention.AddLogEntry("UUID conversion process started.  Please be patient - this may take a while.");
        
        GriefPrevention.AddLogEntry("Mining your local world data to save calls to Mojang...");
        OfflinePlayer [] players = GriefPrevention.instance.getServer().getOfflinePlayers();
        for(OfflinePlayer player : players)
        {
            if(player.getName() != null && player.getUniqueId() != null)
            {
                lookupCache.put(player.getName(), player.getUniqueId());
                lookupCache.put(player.getName().toLowerCase(), player.getUniqueId());
                correctedNames.put(player.getName().toLowerCase(), player.getName());
            }
        }
        
        //try to get correct casing from local data
        GriefPrevention.AddLogEntry("Checking local server data to get correct casing for player names...");
        for(int i = 0; i < names.size(); i++)
        {
            String name = names.get(i);
            String correctCasingName = correctedNames.get(name);
            if(correctCasingName != null && !name.equals(correctCasingName))
            {
                GriefPrevention.AddLogEntry(name + " --> " + correctCasingName);
                names.set(i, correctCasingName); 
            }
        }
        
        //look for local uuid's first
        GriefPrevention.AddLogEntry("Checking local server data for UUIDs already seen...");
        for(int i = 0; i < names.size(); i++)
        {
            String name = names.get(i);
            UUID uuid = lookupCache.get(name);
            if(uuid != null)
            {
                GriefPrevention.AddLogEntry(name + " --> " + uuid.toString());
                names.remove(i--);
            }
        }
        
        //for online mode, call Mojang to resolve the rest
        if(GriefPrevention.instance.getServer().getOnlineMode())
        {
            GriefPrevention.AddLogEntry("Calling Mojang to get UUIDs for remaining unresolved players (this is the slowest step)...");
            
            for (int i = 0; i * PROFILES_PER_REQUEST < names.size(); i++)
            {
                boolean retry = false;
                JSONArray array = null;
                do
                {
                    HttpURLConnection connection = createConnection();
                    String body = JSONArray.toJSONString(names.subList(i * PROFILES_PER_REQUEST, Math.min((i + 1) * PROFILES_PER_REQUEST, names.size())));
                    writeBody(connection, body);
                    retry = false;
                    array = null;
                    try
                    {
                        array = (JSONArray) jsonParser.parse(new InputStreamReader(connection.getInputStream()));
                    }
                    catch(Exception e)
                    {
                        //in case of error 429 too many requests, pause and then retry later
                        if(e.getMessage().contains("429"))
                        {
                            retry = true;
                            
                            //if this is the first time we're sending anything, the batch size must be too big
                            //try reducing it
                            if(i == 0 && PROFILES_PER_REQUEST > 1)
                            {
                                GriefPrevention.AddLogEntry("Batch size " + PROFILES_PER_REQUEST + " seems too large.  Looking for a workable batch size...");
                                PROFILES_PER_REQUEST = Math.max(PROFILES_PER_REQUEST - 5, 1);
                            }
                            
                            //otherwise, keep the batch size which has worked for previous iterations
                            //but wait a little while before trying again.
                            else
                            {
                                GriefPrevention.AddLogEntry("Mojang says we're sending requests too fast.  Will retry every 30 seconds until we succeed...");
                                Thread.sleep(30000);
                            }
                        }
                        else
                        {
                            throw e;
                        }
                    }
                }while(retry);
                
                for (Object profile : array) {
                    JSONObject jsonProfile = (JSONObject) profile;
                    String id = (String) jsonProfile.get("id");
                    String name = (String) jsonProfile.get("name");
                    UUID uuid = UUIDFetcher.getUUID(id);
                    GriefPrevention.AddLogEntry(name + " --> " + uuid.toString());
                    lookupCache.put(name, uuid);
                    lookupCache.put(name.toLowerCase(), uuid);
                }
                if (rateLimiting) {
                    Thread.sleep(200L);
                }
            }
        }
        
        //for offline mode, generate UUIDs for the rest
        else
        {
            GriefPrevention.AddLogEntry("Generating offline mode UUIDs for remaining unresolved players...");
            
            for(int i = 0; i < names.size(); i++)
            {
                String name = names.get(i);
                UUID uuid = java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
                GriefPrevention.AddLogEntry(name + " --> " + uuid.toString());
                lookupCache.put(name, uuid);
                lookupCache.put(name.toLowerCase(), uuid);
            }
        }
    }
 
    private static void writeBody(HttpURLConnection connection, String body) throws Exception {
        OutputStream stream = connection.getOutputStream();
        stream.write(body.getBytes());
        stream.flush();
        stream.close();
    }
 
    private static HttpURLConnection createConnection() throws Exception {
        URL url = new URL(PROFILE_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setUseCaches(false);
        connection.setDoInput(true);
        connection.setDoOutput(true);
        return connection;
    }
 
    private static UUID getUUID(String id) {
        return UUID.fromString(id.substring(0, 8) + "-" + id.substring(8, 12) + "-" + id.substring(12, 16) + "-" + id.substring(16, 20) + "-" +id.substring(20, 32));
    }
 
    public static byte[] toBytes(UUID uuid) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
        byteBuffer.putLong(uuid.getMostSignificantBits());
        byteBuffer.putLong(uuid.getLeastSignificantBits());
        return byteBuffer.array();
    }
 
    public static UUID fromBytes(byte[] array) {
        if (array.length != 16) {
            throw new IllegalArgumentException("Illegal byte array length: " + array.length);
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(array);
        long mostSignificant = byteBuffer.getLong();
        long leastSignificant = byteBuffer.getLong();
        return new UUID(mostSignificant, leastSignificant);
    }
 
    public static UUID getUUIDOf(String name) throws Exception
    {
        UUID result = lookupCache.get(name);
        if(result == null)
        {
            //throw up our hands and report the problem in the logs
            //this player will lose his land claim blocks, but claims will stay in place as admin claims
            throw new IllegalArgumentException(name);
        }
        
        return result;
    }
}