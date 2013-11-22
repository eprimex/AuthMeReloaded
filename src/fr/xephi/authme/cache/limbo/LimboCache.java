package fr.xephi.authme.cache.limbo;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.cache.backup.FileCache;
import fr.xephi.authme.events.ResetInventoryEvent;
import fr.xephi.authme.events.StoreInventoryEvent;
import fr.xephi.authme.settings.Settings;


public class LimboCache {

    private static LimboCache singleton = null;
    public HashMap<String, LimboPlayer> cache;
    private FileCache playerData = new FileCache();
    public AuthMe plugin;

    private LimboCache(AuthMe plugin) {
    	this.plugin = plugin;
        this.cache = new HashMap<String, LimboPlayer>();
    }

    public void addLimboPlayer(Player player) {
        String name = player.getName().toLowerCase();
        Location loc = player.getLocation();
        loc.setY(loc.getY() + 0.4D);
        int gameMode = player.getGameMode().getValue();
        ItemStack[] arm;
        ItemStack[] inv;
        boolean operator;
        String playerGroup = "";
        boolean flying;

        if (playerData.doesCacheExist(name)) {
        	StoreInventoryEvent event = new StoreInventoryEvent(player, playerData);
        	Bukkit.getServer().getPluginManager().callEvent(event);
        	if (!event.isCancelled() && event.getInventory() != null && event.getArmor() != null) {
                inv =  event.getInventory();
                arm =  event.getArmor();
        	} else {
        		inv = null;
        		arm = null;
        	}
             playerGroup = playerData.readCache(name).getGroup();
             operator = playerData.readCache(name).getOperator();
             flying = playerData.readCache(name).isFlying();
        } else {
        	StoreInventoryEvent event = new StoreInventoryEvent(player);
        	Bukkit.getServer().getPluginManager().callEvent(event);
        	if (!event.isCancelled() && event.getInventory() != null && event.getArmor() != null) {
                inv =  event.getInventory();
                arm =  event.getArmor();
        	} else {
        		inv = null;
        		arm = null;
        	}
            if(player.isOp())
                operator = true;
            else operator = false;
            if(player.isFlying())
            	flying = true;
            else flying = false;
        }

        if(Settings.isForceSurvivalModeEnabled) {
            if(Settings.isResetInventoryIfCreative && player.getGameMode() == GameMode.CREATIVE ) {
            	ResetInventoryEvent event = new ResetInventoryEvent(player);
            	Bukkit.getServer().getPluginManager().callEvent(event);
            	if (!event.isCancelled()) {
            		player.getInventory().clear();
            		player.sendMessage("Your inventory has been cleaned!");
            	}
            }
            gameMode = 0;
        }
        if(player.isDead()) {
        	loc = plugin.getSpawnLocation(player.getWorld());
        }
        try {
            if(cache.containsKey(name) && playerGroup.isEmpty()) {
                LimboPlayer groupLimbo = cache.get(name);
                playerGroup = groupLimbo.getGroup();
            }
        } catch (NullPointerException ex) {
        }
        cache.put(player.getName().toLowerCase(), new LimboPlayer(name, loc, inv, arm, gameMode, operator, playerGroup, flying));
    }

    public void addLimboPlayer(Player player, String group) {
        cache.put(player.getName().toLowerCase(), new LimboPlayer(player.getName().toLowerCase(), group));
    }

    public void deleteLimboPlayer(String name) {
        cache.remove(name);
    }

    public LimboPlayer getLimboPlayer(String name) {
        return cache.get(name);
    }

    public boolean hasLimboPlayer(String name) {
        return cache.containsKey(name);
    }

    public static LimboCache getInstance() {
        if (singleton == null) {
            singleton = new LimboCache(AuthMe.getInstance());
        }
        return singleton;
    }

	public void updateLimboPlayer(Player player) {
		if (this.hasLimboPlayer(player.getName().toLowerCase())) {
			this.deleteLimboPlayer(player.getName().toLowerCase());
		}
		this.addLimboPlayer(player);
	}

}
