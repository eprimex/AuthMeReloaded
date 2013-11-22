package fr.xephi.authme.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.Utils;
import fr.xephi.authme.api.API;
import fr.xephi.authme.cache.auth.PlayerAuth;
import fr.xephi.authme.cache.auth.PlayerCache;
import fr.xephi.authme.converter.FlatToSql;
import fr.xephi.authme.converter.FlatToSqlite;
import fr.xephi.authme.converter.RakamakConverter;
import fr.xephi.authme.converter.newxAuthToFlat;
import fr.xephi.authme.converter.oldxAuthToFlat;
import fr.xephi.authme.datasource.DataSource;
import fr.xephi.authme.security.PasswordSecurity;
import fr.xephi.authme.settings.Messages;
import fr.xephi.authme.settings.Settings;
import fr.xephi.authme.settings.Spawn;
import fr.xephi.authme.settings.SpoutCfg;


public class AdminCommand implements CommandExecutor {

	public AuthMe plugin;
    private Messages m = Messages.getInstance();
    private SpoutCfg s = SpoutCfg.getInstance();
    public DataSource database;

    public AdminCommand(AuthMe plugin, DataSource database) {
        this.database = database;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Usage: /authme reload - Reload the config");
            sender.sendMessage("/authme register <playername> <password> - Register a player");
            sender.sendMessage("/authme changepassword <playername> <password> - Change player password");
            sender.sendMessage("/authme unregister <playername> - Unregister a player");
            sender.sendMessage("/authme purge <days> - Purge Database");
            sender.sendMessage("/authme version - Get AuthMe version infos");
            sender.sendMessage("/authme lastlogin <playername> - Display Date about the Player's LastLogin");
            sender.sendMessage("/authme accounts <playername> - Display all player's accounts");
            sender.sendMessage("/authme setSpawn - Set AuthMe spawn to your current pos");
            sender.sendMessage("/authme spawn - Teleport you to the AuthMe SpawnPoint");
            sender.sendMessage("/authme chgemail <playername> <email> - Change player email");
            sender.sendMessage("/authme getemail <playername> - Get player email");
            sender.sendMessage("/authme purgelastpos <playername> - Purge last position for a player");
            sender.sendMessage("/authme switchantibot on/off - Enable/Disable antibot method");
            return true;
        }

       if((sender instanceof ConsoleCommandSender) && args[0].equalsIgnoreCase("passpartuToken")) {
           if(args.length > 1) {  
               System.out.println("[AuthMe] command usage: /authme passpartuToken");
               return true;
           }
           if(Utils.getInstance().obtainToken()) {
                System.out.println("[AuthMe] You have 30s for insert this token ingame with /passpartu [token]");
            } else {
                System.out.println("[AuthMe] Security error on passpartu token, redo it. ");
            }
            return true;
       }

       if (!plugin.authmePermissible(sender, "authme.admin." + args[0].toLowerCase())) {
            sender.sendMessage(m._("no_perm"));
            return true;
        }

        if (args[0].equalsIgnoreCase("version")) {
            sender.sendMessage("AuthMe Version: "+AuthMe.getInstance().getDescription().getVersion());
            return true;
        }

        if (args[0].equalsIgnoreCase("purge")) {
            if (args.length != 2) {
                sender.sendMessage("Usage: /authme purge <DAYS>");
                return true;
            }
            try {
                long days = Long.parseLong(args[1]) * 86400000;
                long until = new Date().getTime() - days;
                List<String> purged = database.autoPurgeDatabase(until);
                sender.sendMessage("Deleted " + purged.size() + " user accounts");
        		if (Settings.purgeEssentialsFile && plugin.ess != null)
        			plugin.purgeEssentials(purged);
        		if (Settings.purgePlayerDat)
        			plugin.purgeDat(purged);
        		if (Settings.purgeLimitedCreative)
        			plugin.purgeLimitedCreative(purged);
        		if (Settings.purgeAntiXray)
        			plugin.purgeAntiXray(purged);
                return true;
            } catch (NumberFormatException e) {
                sender.sendMessage("Usage: /authme purge <DAYS>");
                return true;
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            database.reload();
            File newConfigFile = new File("plugins/AuthMe","config.yml");
            if (!newConfigFile.exists()) {
            	InputStream fis = getClass().getResourceAsStream("/config.yml");
            	FileOutputStream fos = null;
            	try {
            		fos = new FileOutputStream(newConfigFile);
            		byte[] buf = new byte[1024];
            		int i = 0;
        
            		while ((i = fis.read(buf)) != -1) {
            			fos.write(buf, 0, i);
            		}
            	} catch (Exception e) {
            		Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Failed to load config from JAR");
            	} finally {
            		try {
            			if (fis != null) {
            				fis.close();
            			}
            			if (fos != null) {
            				fos.close();
            			}
            		} catch (Exception e) {                         
            		}
            	}
            }
            YamlConfiguration newConfig = YamlConfiguration.loadConfiguration(newConfigFile);
            Settings.reloadConfigOptions(newConfig);
            m.reLoad();
            s.reLoad();
            sender.sendMessage(m._("reload"));
        } else if (args[0].equalsIgnoreCase("lastlogin")) {
        	if (args.length != 2) {
        		sender.sendMessage("Usage: /authme lastlogin <playername>");
        		return true;
        	}
        	try {
        		if (database.getAuth(args[1].toLowerCase()) != null) {
                    PlayerAuth player = database.getAuth(args[1].toLowerCase());
                    long lastLogin = player.getLastLogin();
                    Date d = new Date(lastLogin);
                    final long diff = System.currentTimeMillis() - lastLogin;
                    final String msg = (int)(diff / 86400000) + " days " + (int)(diff / 3600000 % 24) + " hours " + (int)(diff / 60000 % 60) + " mins " + (int)(diff / 1000 % 60) + " secs.";
                    String lastIP = player.getIp();
                    sender.sendMessage("[AuthMe] " + args[1].toLowerCase() + " lastlogin : " + d.toString());
                    sender.sendMessage("[AuthMe] The player : " + player.getNickname() + " is unlogged since " + msg);
                    sender.sendMessage("[AuthMe] LastPlayer IP : " + lastIP);
        		} else {
            		sender.sendMessage("This player does not exist");
            		return true;
        		}
        	} catch (NullPointerException e) {
        		sender.sendMessage("This player does not exist");
        		return true;
        	}
        } else if (args[0].equalsIgnoreCase("accounts")) {
        	if (args.length != 2) {
        		sender.sendMessage("Usage: /authme accounts <playername>");
        		sender.sendMessage("Or: /authme accounts <ip>");
        		return true;
        	}
        	if (!args[1].contains(".")) {
            	final CommandSender fSender = sender;
            	final String[] arguments = args;
            	Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
    				@Override
    				public void run() {
    		        	PlayerAuth pAuth = null;
    		        	String message = "[AuthMe] ";
    		        	try {
    		        		pAuth = database.getAuth(arguments[1].toLowerCase());
    		        	} catch (NullPointerException npe){
    		        		fSender.sendMessage("[AuthMe] This player is unknown");
    		        		return;
    		        	}
    		        	if (pAuth != null) {
    		        		List<String> accountList = database.getAllAuthsByName(pAuth);
    		        		if (accountList.isEmpty() || accountList == null) {
    		            		fSender.sendMessage("[AuthMe] This player is unknown");
    		            		return;
    		        		}
    		        		if (accountList.size() == 1) {
    		            		fSender.sendMessage("[AuthMe] " + arguments[1] + " is a single account player");
    		            		return;
    		        		}
    		            	int i = 0;
    		            	for (String account : accountList) {
    		            		i++;
    		            		message = message + account;
    		            		if (i != accountList.size()) {
    		            			message = message + ", ";
    		            		} else {
    		            			message = message + ".";
    		            		}
    		            	}
    		            	fSender.sendMessage("[AuthMe] " + arguments[1] + " has " + String.valueOf(accountList.size()) + " accounts");
    		            	fSender.sendMessage(message);
    		        	} else {
    		        		fSender.sendMessage("[AuthMe] This player is unknown");
    		        		return;
    		        	}
    				}
            	});
            	return true;
        	} else {
            	final CommandSender fSender = sender;
            	final String[] arguments = args;
            	Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
    				@Override
    				public void run() {
    		        	String message = "[AuthMe] ";
    		        	if (arguments[1] != null) {
    		        		List<String> accountList = database.getAllAuthsByIp(arguments[1]);
    		        		if (accountList.isEmpty() || accountList == null) {
    		            		fSender.sendMessage("[AuthMe] Please put a valid IP");
    		            		return;
    		        		}
    		        		if (accountList.size() == 1) {
    		            		fSender.sendMessage("[AuthMe] " + arguments[1] + " is a single account player");
    		            		return;
    		        		}
    		            	int i = 0;
    		            	for (String account : accountList) {
    		            		i++;
    		            		message = message + account;
    		            		if (i != accountList.size()) {
    		            			message = message + ", ";
    		            		} else {
    		            			message = message + ".";
    		            		}
    		            	}
    		            	fSender.sendMessage("[AuthMe] " + arguments[1] + " has " + String.valueOf(accountList.size()) + " accounts");
    		            	fSender.sendMessage(message);
    		        	} else {
    		        		fSender.sendMessage("[AuthMe] Please put a valid IP");
    		        		return;
    		        	}
    				}
            	});
            	return true;
        	}
        } else if (args[0].equalsIgnoreCase("register") || args[0].equalsIgnoreCase("reg")) {
            if (args.length != 3) {
                sender.sendMessage("Usage: /authme register playername password");
                return true;
            }
            try {
                String name = args[1].toLowerCase();
                if (database.isAuthAvailable(name)) {
                    sender.sendMessage(m._("user_regged"));
                    return true;
                }
                String hash = PasswordSecurity.getHash(Settings.getPasswordHash, args[2], name);
                PlayerAuth auth = new PlayerAuth(name, hash, "198.18.0.1", 0L, "your@email.com", API.getPlayerRealName(name));
                if (PasswordSecurity.userSalt.containsKey(name) && PasswordSecurity.userSalt.get(name) != null)
                	auth.setSalt(PasswordSecurity.userSalt.get(name));
                else
                	auth.setSalt("");
                if (!database.saveAuth(auth)) {
                    sender.sendMessage(m._("error"));
                    return true;
                }
                sender.sendMessage(m._("registered"));
                ConsoleLogger.info(args[1] + " registered");
            } catch (NoSuchAlgorithmException ex) {
                ConsoleLogger.showError(ex.getMessage());
                sender.sendMessage(m._("error"));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("convertflattosql")) {
        		try {
        			FlatToSql.FlatToSqlConverter();
        			if (sender instanceof Player)
        				sender.sendMessage("[AuthMe] FlatFile converted to authme.sql file");
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NullPointerException ex) {
					System.out.println(ex.getMessage());
				}
        } else if (args[0].equalsIgnoreCase("flattosqlite")) {
    		try {
    			String s = FlatToSqlite.convert();
    			if (sender instanceof Player)
    				sender.sendMessage(s);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException ex) {
				System.out.println(ex.getMessage());
			}
			return true;
        } else if (args[0].equalsIgnoreCase("xauthimport")) {
        	try {
        		Class.forName("com.cypherx.xauth.xAuth");
            	oldxAuthToFlat converter = new oldxAuthToFlat(plugin, database, sender);
            	converter.run();
        	} catch (ClassNotFoundException e) {
        		try {
        			Class.forName("de.luricos.bukkit.xAuth.xAuth");
        			newxAuthToFlat converter = new newxAuthToFlat(plugin, database, sender);
        			converter.run();
        		} catch (ClassNotFoundException ce) {
        			sender.sendMessage("[AuthMe] No version of xAuth found or xAuth isn't enable! ");
        		}
        	}
        	return true;
        } else if (args[0].equalsIgnoreCase("getemail")) {
            if (args.length != 2) {
                sender.sendMessage("Usage: /authme getemail playername");
                return true;
            }
    		String playername = args[1].toLowerCase();
    		PlayerAuth getAuth = database.getAuth(playername);
    		if (getAuth == null) {
        		sender.sendMessage("This player does not exist");
        		return true;
    		}
    		sender.sendMessage("[AuthMe] " + args[1] + " email : " + getAuth.getEmail());
    		return true;
        } else if (args[0].equalsIgnoreCase("chgemail")) {
            if (args.length != 3) {
                sender.sendMessage("Usage: /authme chgemail playername email");
                return true;
            }
    		String playername = args[1].toLowerCase();
    		PlayerAuth getAuth = database.getAuth(playername);
    		if (getAuth == null) {
    			sender.sendMessage("This player does not exist");
    			return true;
    		}
    		getAuth.setEmail(args[2]);
            if (!database.updateEmail(getAuth)) {
                sender.sendMessage(m._("error"));
                return true;
            }
            if (PlayerCache.getInstance().getAuth(playername) != null)
            	PlayerCache.getInstance().updatePlayer(getAuth);
    		return true;
        } else if (args[0].equalsIgnoreCase("convertfromrakamak")) {
    		try {
    			RakamakConverter.RakamakConvert();
    			if (sender instanceof Player)
    				sender.sendMessage("[AuthMe] Rakamak database converted to auths.db");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException ex) {
				ConsoleLogger.showError(ex.getMessage());
			}
			return true;
        } else if (args[0].equalsIgnoreCase("setspawn")) {
    		try {
    			if (sender instanceof Player) {
    				if (Spawn.getInstance().setSpawn(((Player) sender).getLocation()))
    				sender.sendMessage("[AuthMe] Correctly define new spawn");
    				else sender.sendMessage("[AuthMe] SetSpawn fail , please retry");
    			} else {
    				sender.sendMessage("[AuthMe] Please use that command in game");
    			}
			} catch (NullPointerException ex) {
				ConsoleLogger.showError(ex.getMessage());
			}
			return true;
        } else if (args[0].equalsIgnoreCase("purgebannedplayers")) {
        	List<String> bannedPlayers = new ArrayList<String>();
        	for (OfflinePlayer off : plugin.getServer().getBannedPlayers()) {
        		bannedPlayers.add(off.getName().toLowerCase());
        	}
        	final List<String> bP = bannedPlayers;
        	if (database instanceof Thread) {
        		database.purgeBanned(bP);
        	} else {
            	Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
    				@Override
    				public void run() {
    					database.purgeBanned(bP);
    				}
            	});
        	}
    		if (Settings.purgeEssentialsFile && plugin.ess != null)
    			plugin.purgeEssentials(bannedPlayers);
    		if (Settings.purgePlayerDat)
    			plugin.purgeDat(bannedPlayers);
    		if (Settings.purgeLimitedCreative)
    			plugin.purgeLimitedCreative(bannedPlayers);
    		if (Settings.purgeAntiXray)
    			plugin.purgeAntiXray(bannedPlayers);
    		return true;
        } else if (args[0].equalsIgnoreCase("spawn")) {
    		try {
    			if (sender instanceof Player) {
    				if (Spawn.getInstance().getLocation() != null)
    				((Player) sender).teleport(Spawn.getInstance().getLocation());
    				else sender.sendMessage("[AuthMe] Spawn fail , please try to define the spawn");
    			} else {
    				sender.sendMessage("[AuthMe] Please use that command in game");
    			}
			} catch (NullPointerException ex) {
				ConsoleLogger.showError(ex.getMessage());
			}
			return true;
        } else if (args[0].equalsIgnoreCase("changepassword") || args[0].equalsIgnoreCase("cp")) {
            if (args.length != 3) {
                sender.sendMessage("Usage: /authme changepassword playername newpassword");
                return true;
            }
            try {
                String name = args[1].toLowerCase();
                String hash = PasswordSecurity.getHash(Settings.getPasswordHash, args[2], name);
                PlayerAuth auth = null;
                if (PlayerCache.getInstance().isAuthenticated(name)) {
                    auth = PlayerCache.getInstance().getAuth(name);
                } else if (database.isAuthAvailable(name)) {
                    auth = database.getAuth(name);
                } else {
                    sender.sendMessage(m._("unknown_user"));
                    return true;
                }
                auth.setHash(hash);
                auth.setSalt(PasswordSecurity.userSalt.get(name));
                if (!database.updatePassword(auth)) {
                    sender.sendMessage(m._("error"));
                    return true;
                }
                database.updateSalt(auth);
                sender.sendMessage("pwd_changed");
                ConsoleLogger.info(args[1] + "'s password changed");
            } catch (NoSuchAlgorithmException ex) {
                ConsoleLogger.showError(ex.getMessage());
                sender.sendMessage(m._("error"));
            }
            return true;
        } else if (args[0].equalsIgnoreCase("unregister") || args[0].equalsIgnoreCase("unreg") || args[0].equalsIgnoreCase("del") ) {
            if (args.length != 2) {
                sender.sendMessage("Usage: /authme unregister playername");
                return true;
            }
            String name = args[1].toLowerCase();
            if (!database.removeAuth(name)) {
                sender.sendMessage(m._("error"));
                return true;
            }
            PlayerCache.getInstance().removePlayer(name);
            sender.sendMessage("unregistered");
            ConsoleLogger.info(args[1] + " unregistered");
            return true;
        } else if (args[0].equalsIgnoreCase("purgelastpos")){
            if (args.length != 2) {
                sender.sendMessage("Usage: /authme purgelastpos playername");
                return true;
            }
            try {
                String name = args[1].toLowerCase();
                PlayerAuth auth = database.getAuth(name);
                if (auth == null) {
                	sender.sendMessage("The player " + name + " is not registered ");
                	return true;
                }
                auth.setQuitLocX(0);
                auth.setQuitLocY(0);
                auth.setQuitLocZ(0);
                auth.setWorld("world");
                database.updateQuitLoc(auth);
                sender.sendMessage(name + " 's last pos location is now reset");
            } catch (Exception e) {
            	ConsoleLogger.showError("An error occured while trying to reset location or player do not exist, please see below: ");
            	ConsoleLogger.showError(e.getMessage());
            	if (sender instanceof Player)
            		sender.sendMessage("An error occured while trying to reset location or player do not exist, please see logs");
            }
            return true;
        } else if (args[0].equalsIgnoreCase("switchantibot")) {
        	if (args.length != 2) {
        		sender.sendMessage("Usage : /authme switchantibot on/off");
        		return true;
        	}
        	if (args[1].equalsIgnoreCase("on")) {
        		plugin.switchAntiBotMod(true);
        		sender.sendMessage("[AuthMe] AntiBotMod enabled");
        		return true;
        	}
        	if (args[1].equalsIgnoreCase("off")) {
        		plugin.switchAntiBotMod(false);
        		sender.sendMessage("[AuthMe] AntiBotMod disabled");
        		return true;
        	}
    		sender.sendMessage("Usage : /authme switchantibot on/off");
    		return true;
        } else {
            sender.sendMessage("Usage: /authme reload|register playername password|changepassword playername password|unregister playername");
        }
        return true;
    }
}
