package me.ryanhamshire.GriefPrevention;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Listener for events which may result in a change in the active Economy.
 */
public class EconomyHandler implements Listener
{

    private final GriefPrevention instance;
    private boolean setupDone = false;
    private EconomyWrapper economy = null;

    public EconomyHandler(GriefPrevention instance)
    {
        this.instance = instance;
    }

    /**
     * Gets the current Economy inside of a wrapper class.
     *
     * @return the current wrapped Economy or null if no Economy is active
     */
    EconomyWrapper getWrapper()
    {
        // Attempt to load the Economy if it is not already set up.
        loadEconomy(false);

        return economy;
    }

    /**
     * EventHandler for PluginEnableEvents in case of an Economy being enabled.
     *
     * @param event the PluginEnableEvent
     */
    @EventHandler
    private void onPluginEnable(PluginEnableEvent event)
    {
        loadEconomy(true);
    }

    /**
     * EventHandler for PluginDisableEvents in case of an Economy being disabled.
     *
     * @param event the PluginDisableEvent
     */
    @EventHandler
    private void onPluginDisable(PluginDisableEvent event)
    {
        loadEconomy(true);
    }

    /**
     * Attempt to change economy. If the setup state does not match the
     * provided value this does nothing to prevent unnecessary loads.
     *
     * @param setupState the expected setup state
     */
    private void loadEconomy(boolean setupState)
    {
        // If no change is likely, have we already obtained the Economy?
        if (setupState != setupDone) return;

        // Are we configured to allow transactions?
        if (!(instance.config_economy_claimBlocksPurchaseCost > 0 || instance.config_economy_claimBlocksSellValue > 0))
        {
            finishSetup(false, null);
            return;
        }

        // Ensure Vault present.
        try
        {
            Class.forName("net.milkbowl.vault.economy.Economy");
        }
        catch (ClassNotFoundException e)
        {
            finishSetup(false, "ERROR: GriefPrevention requires Vault for economy integration.");
            return;
        }

        RegisteredServiceProvider<Economy> registration = instance.getServer().getServicesManager().getRegistration(Economy.class);

        // Ensure an Economy is available.
        if (registration == null)
        {
            finishSetup(false, "ERROR: Vault was unable to find a supported economy plugin.  Either install a Vault-compatible economy plugin, or set both of the economy config variables to zero.");
            return;
        }

        Economy newEconomy = registration.getProvider();

        // If Economy hasn't changed, do nothing.
        if (economy != null && economy.getEconomy().equals(newEconomy)) return;

        // Set setupDone false to force log line for changing Economy.
        setupDone = false;
        economy = new EconomyWrapper(newEconomy);

        finishSetup(true, "Hooked into economy: " + economy.economy.getName() + ". Ready to buy/sell claim blocks!");
    }

    private void finishSetup(boolean ready, String log) {
        if (!ready) this.economy = null;

        if (log != null && !setupDone) GriefPrevention.AddLogEntry(log);

        this.setupDone = true;
    }

    /**
     * Wrapper class used to prevent Bukkit from logging an error and
     * preventing registering events for the listener when Vault is not loaded.
     */
    static class EconomyWrapper
    {

        private final Economy economy;

        private EconomyWrapper(Economy economy)
        {
            this.economy = economy;
        }

        Economy getEconomy()
        {
            return this.economy;
        }

    }

}
