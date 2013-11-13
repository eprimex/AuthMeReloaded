package fr.xephi.authme.datasource;

import java.util.HashMap;
import java.util.List;

import org.bukkit.entity.Player;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.cache.auth.PlayerAuth;
import fr.xephi.authme.cache.auth.PlayerCache;


public class CacheDataSource implements DataSource {

    private DataSource source;
    public AuthMe plugin;
    private final HashMap<String, PlayerAuth> cache = new HashMap<String, PlayerAuth>();

    public CacheDataSource(AuthMe plugin, DataSource source) {
    	this.plugin = plugin;
        this.source = source;
    }

    @Override
    public synchronized boolean isAuthAvailable(String user) {
        return cache.containsKey(user) ? true : source.isAuthAvailable(user);
    }

    @Override
    public synchronized PlayerAuth getAuth(String user) {
        if(cache.containsKey(user)) {
            return cache.get(user);
        } else {
            PlayerAuth auth = source.getAuth(user);
            cache.put(user, auth);
            return auth;
        }
    }

    @Override
    public synchronized boolean saveAuth(PlayerAuth auth) {
        if (source.saveAuth(auth)) {
            cache.put(auth.getNickname(), auth);
            return true;
        }
        return false;
    }

    @Override
    public synchronized boolean updatePassword(PlayerAuth auth) {
        if (source.updatePassword(auth)) {
            cache.get(auth.getNickname()).setHash(auth.getHash());
            return true;
        }
        return false;
    }

    @Override
    public boolean updateSession(PlayerAuth auth) {
        if (source.updateSession(auth)) {
            cache.get(auth.getNickname()).setIp(auth.getIp());
            cache.get(auth.getNickname()).setLastLogin(auth.getLastLogin());
            return true;
        }
        return false;
    }

    @Override
    public boolean updateQuitLoc(PlayerAuth auth) {
        if (source.updateQuitLoc(auth)) {
            cache.get(auth.getNickname()).setQuitLocX(auth.getQuitLocX());
            cache.get(auth.getNickname()).setQuitLocY(auth.getQuitLocY());
            cache.get(auth.getNickname()).setQuitLocZ(auth.getQuitLocZ());
            cache.get(auth.getNickname()).setWorld(auth.getWorld());
            return true;
        }
        return false;
    }

    @Override
    public int getIps(String ip) {
        return source.getIps(ip);
    }

    @Override
    public int purgeDatabase(long until) {
        int cleared = source.purgeDatabase(until);
        if (cleared > 0) {
            for (PlayerAuth auth : cache.values()) {
                if(auth.getLastLogin() < until) {
                    cache.remove(auth.getNickname());
                }
            }
        }
        return cleared;
    }

    @Override
    public List<String> autoPurgeDatabase(long until) {
        List<String> cleared = source.autoPurgeDatabase(until);
        if (cleared.size() > 0) {
            for (PlayerAuth auth : cache.values()) {
                if(auth.getLastLogin() < until) {
                    cache.remove(auth.getNickname());
                }
            }
        }
        return cleared;
    }

    @Override
    public synchronized boolean removeAuth(String user) {
        if (source.removeAuth(user)) {
            cache.remove(user);
            return true;
        }
        return false;
    }

    @Override
    public synchronized void close() {
        source.close();
    }

    @Override
    public void reload() {
    	cache.clear();
    	for (Player player : plugin.getServer().getOnlinePlayers()) {
    		String user = player.getName().toLowerCase();
    		if (PlayerCache.getInstance().isAuthenticated(user)) {
    			try {
                    PlayerAuth auth = source.getAuth(user);
                    cache.put(user, auth);
    			} catch (NullPointerException npe) {
    			}

    		}
    	}
    }

	@Override
	public synchronized boolean updateEmail(PlayerAuth auth) {
		if(source.updateEmail(auth)) {
			cache.get(auth.getNickname()).setEmail(auth.getEmail());
			return true;
		}
		return false;
	}

	@Override
	public synchronized boolean updateSalt(PlayerAuth auth) {
		if(source.updateSalt(auth)) {
			cache.get(auth.getNickname()).setSalt(auth.getSalt());
			return true;
		}
		return false;
	}

	@Override
	public synchronized List<String> getAllAuthsByName(PlayerAuth auth) {
		return source.getAllAuthsByName(auth);
	}

	@Override
	public synchronized List<String> getAllAuthsByIp(String ip) {
		return source.getAllAuthsByIp(ip);
	}

	@Override
	public synchronized List<String> getAllAuthsByEmail(String email) {
		return source.getAllAuthsByEmail(email);
	}

	@Override
	public synchronized void purgeBanned(List<String> banned) {
		source.purgeBanned(banned);
		for (PlayerAuth auth : cache.values()) {
			if (banned.contains(auth.getNickname())) {
				cache.remove(auth.getNickname());
			}
		}
	}

}
