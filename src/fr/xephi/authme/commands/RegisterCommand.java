package fr.xephi.authme.commands;

import java.security.NoSuchAlgorithmException;
import java.util.Date;

import me.muizers.Notifications.Notification;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.ConsoleLogger;
import fr.xephi.authme.Utils;
import fr.xephi.authme.cache.auth.PlayerAuth;
import fr.xephi.authme.cache.auth.PlayerCache;
import fr.xephi.authme.cache.limbo.LimboCache;
import fr.xephi.authme.cache.limbo.LimboPlayer;
import fr.xephi.authme.datasource.DataSource;
import fr.xephi.authme.events.RegisterTeleportEvent;
import fr.xephi.authme.security.PasswordSecurity;
import fr.xephi.authme.security.RandomString;
import fr.xephi.authme.settings.Messages;
import fr.xephi.authme.settings.PlayersLogs;
import fr.xephi.authme.settings.Settings;
import fr.xephi.authme.task.MessageTask;
import fr.xephi.authme.task.TimeoutTask;


public class RegisterCommand implements CommandExecutor {

    private Messages m = Messages.getInstance();
    private PlayersLogs pllog = PlayersLogs.getInstance();
    private DataSource database;
    public boolean isFirstTimeJoin;
	public PlayerAuth auth;
	public AuthMe plugin;

    public RegisterCommand(DataSource database, AuthMe plugin) {
        this.database = database;
        this.isFirstTimeJoin = false;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmnd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return true;
        }

        if (!plugin.authmePermissible(sender, "authme." + label.toLowerCase())) {
            sender.sendMessage(m._("no_perm"));
            return true;
        }

        final Player player = (Player) sender;
        final String name = player.getName().toLowerCase();
        String ipA = player.getAddress().getAddress().getHostAddress();

        if (Settings.bungee) {
        	if (plugin.realIp.containsKey(name))
        		ipA = plugin.realIp.get(name);
        }

        final String ip = ipA;
        
        	if (PlayerCache.getInstance().isAuthenticated(name)) {
                player.sendMessage(m._("logged_in"));
                return true;
            }

            if (!Settings.isRegistrationEnabled) {
                player.sendMessage(m._("reg_disabled"));
                return true;
            }

            if (database.isAuthAvailable(player.getName().toLowerCase())) {
                player.sendMessage(m._("user_regged"));
                if (pllog.getStringList("players").contains(player.getName())) {
               	 pllog.getStringList("players").remove(player.getName());
                }
                return true;
            }

            if(Settings.getmaxRegPerIp > 0 ){
            	if(!plugin.authmePermissible(sender, "authme.allow2accounts") && database.getAllAuthsByIp(ipA).size() >= Settings.getmaxRegPerIp) {
            		player.sendMessage(m._("max_reg"));
                    return true;
            	}
            }

            if(Settings.emailRegistration && !Settings.getmailAccount.isEmpty()) {
            	if(!args[0].contains("@")) {
                    player.sendMessage(m._("usage_reg"));
                    return true;
            	}
            	if(Settings.doubleEmailCheck) {
            		if(args.length < 2) {
                        player.sendMessage(m._("usage_reg"));
                        return true;
            		}
            		if(!args[0].equals(args[1])) {
                        player.sendMessage(m._("usage_reg"));
                        return true;
            		}
            	}
            	final String email = args[0];
            	if(Settings.getmaxRegPerEmail > 0) {
            		if (!plugin.authmePermissible(sender, "authme.allow2accounts") && database.getAllAuthsByEmail(email).size() >= Settings.getmaxRegPerEmail) {
            			player.sendMessage(m._("max_reg"));
            			return true;
            		}
            	}
    			RandomString rand = new RandomString(Settings.getRecoveryPassLength);
    			final String thePass = rand.nextString();
                if (!thePass.isEmpty()) {
                	Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
    					@Override
    					public void run() {
    		            	if (PasswordSecurity.userSalt.containsKey(name)) {
    		        			try {
    		        				final String hashnew = PasswordSecurity.getHash(Settings.getPasswordHash, thePass, name);
    			            		final PlayerAuth fAuth = new PlayerAuth(name, hashnew, PasswordSecurity.userSalt.get(name), ip, new Date().getTime(), (int) player.getLocation().getX() , (int) player.getLocation().getY(), (int) player.getLocation().getZ(), player.getLocation().getWorld().getName(), email, player.getName());
    		        	            database.saveAuth(fAuth);
    								database.updateEmail(fAuth);
    								database.updateSession(fAuth);
    								plugin.mail.main(fAuth, thePass);
    		        			} catch (NoSuchAlgorithmException e) {
    		        				ConsoleLogger.showError(e.getMessage());
    		        			}
    		            	} else {
    		        			try {
    		        				final String hashnew = PasswordSecurity.getHash(Settings.getPasswordHash, thePass, name);
    		        				final PlayerAuth fAuth = new PlayerAuth(name, hashnew, ip, new Date().getTime(), (int) player.getLocation().getX() , (int) player.getLocation().getY(), (int) player.getLocation().getZ(), player.getLocation().getWorld().getName(), email, player.getName());
    		        				database.saveAuth(fAuth);
    								database.updateEmail(fAuth);
    								database.updateSession(fAuth);
    								plugin.mail.main(fAuth, thePass);
    		        			} catch (NoSuchAlgorithmException e) {
    		        				ConsoleLogger.showError(e.getMessage());
    		        			}
    		            	}
    					}
                	});

                    if(!Settings.getRegisteredGroup.isEmpty()){
                        Utils.getInstance().setGroup(player, Utils.groupType.REGISTERED);
                    }
                	player.sendMessage(m._("vb_nonActiv"));
                	String msg = m._("login_msg");
                	int time = Settings.getRegistrationTimeout * 20;
                	int msgInterval = Settings.getWarnMessageInterval;
                    if (time != 0) {
                    	Bukkit.getScheduler().cancelTask(LimboCache.getInstance().getLimboPlayer(name).getTimeoutTaskId());
                        BukkitTask id = Bukkit.getScheduler().runTaskLater(plugin, new TimeoutTask(plugin, name), time);
                        LimboCache.getInstance().getLimboPlayer(name).setTimeoutTaskId(id.getTaskId());
                    }

                    Bukkit.getScheduler().cancelTask(LimboCache.getInstance().getLimboPlayer(name).getMessageTaskId());
                    BukkitTask nwMsg = Bukkit.getScheduler().runTask(plugin, new MessageTask(plugin, name, msg, msgInterval));
                    LimboCache.getInstance().getLimboPlayer(name).setMessageTaskId(nwMsg.getTaskId());

                	LimboCache.getInstance().deleteLimboPlayer(name);
                    if (Settings.isTeleportToSpawnEnabled) {
                    	World world = player.getWorld();
                    	Location loca = plugin.getSpawnLocation(world);
                        RegisterTeleportEvent tpEvent = new RegisterTeleportEvent(player, loca);
                        plugin.getServer().getPluginManager().callEvent(tpEvent);
                        if(!tpEvent.isCancelled()) {
                        	if (!tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).isLoaded()) {
                        		tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).load();
                        	}
                      	  	player.teleport(tpEvent.getTo());
                        }
                    }
                    this.isFirstTimeJoin = true;
                    if (player.getGameMode() != GameMode.CREATIVE && !Settings.isMovementAllowed) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                    }
                    player.saveData();
                    if (!Settings.noConsoleSpam)
                    ConsoleLogger.info(player.getName() + " registered "+player.getAddress().getAddress().getHostAddress());
                    if(plugin.notifications != null) {
                    	plugin.notifications.showNotification(new Notification("[AuthMe] " + player.getName() + " has registered!"));
                    }
                	return true;
                }
            }

            if (args.length == 0 || (Settings.getEnablePasswordVerifier && args.length < 2) ) {
                player.sendMessage(m._("usage_reg"));
                return true;
            }

            if(args[0].length() < Settings.getPasswordMinLen || args[0].length() > Settings.passwordMaxLength) {
                player.sendMessage(m._("pass_len"));
                return true;
            }
            try {
                String hash;
                if(Settings.getEnablePasswordVerifier) {
                    if (args[0].equals(args[1])) {
                        hash = PasswordSecurity.getHash(Settings.getPasswordHash, args[0], name);
                     } else {
                        player.sendMessage(m._("password_error"));
                        return true;
                      }
                } else
                    hash = PasswordSecurity.getHash(Settings.getPasswordHash, args[0], name);
                if (Settings.getMySQLColumnSalt.isEmpty())
                {
                	auth = new PlayerAuth(name, hash, ip, new Date().getTime(), "your@email.com",  player.getName());
                } else {
                	auth = new PlayerAuth(name, hash, PasswordSecurity.userSalt.get(name), ip, new Date().getTime(), player.getName());
                }
                if (!database.saveAuth(auth)) {
                    player.sendMessage(m._("error"));
                    return true;
                }
                PlayerCache.getInstance().addPlayer(auth);
                LimboPlayer limbo = LimboCache.getInstance().getLimboPlayer(name);
                if (limbo != null) {
                    player.setGameMode(GameMode.getByValue(limbo.getGameMode()));      
                    if (Settings.isTeleportToSpawnEnabled) {
                    	World world = player.getWorld();
                    	Location loca = plugin.getSpawnLocation(world);
                        RegisterTeleportEvent tpEvent = new RegisterTeleportEvent(player, loca);
                        plugin.getServer().getPluginManager().callEvent(tpEvent);
                        if(!tpEvent.isCancelled()) {
                        	if (!tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).isLoaded()) {
                        		tpEvent.getTo().getWorld().getChunkAt(tpEvent.getTo()).load();
                        	}
                      	  	player.teleport(tpEvent.getTo());
                        }
                    }
                    sender.getServer().getScheduler().cancelTask(limbo.getTimeoutTaskId());
                    sender.getServer().getScheduler().cancelTask(limbo.getMessageTaskId());
                    LimboCache.getInstance().deleteLimboPlayer(name);
                }

                if(!Settings.getRegisteredGroup.isEmpty()){
                    Utils.getInstance().setGroup(player, Utils.groupType.REGISTERED);
                }
                player.sendMessage(m._("registered"));
                if (!Settings.getmailAccount.isEmpty())
                player.sendMessage(m._("add_email"));
                this.isFirstTimeJoin = true;
                if (player.getGameMode() != GameMode.CREATIVE && !Settings.isMovementAllowed) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
                player.saveData();
                if (!Settings.noConsoleSpam)
                ConsoleLogger.info(player.getName() + " registered "+player.getAddress().getAddress().getHostAddress());
                if(plugin.notifications != null) {
                	plugin.notifications.showNotification(new Notification("[AuthMe] " + player.getName() + " has registered!"));
                }
            } catch (NoSuchAlgorithmException ex) {
                ConsoleLogger.showError(ex.getMessage());
                sender.sendMessage(m._("error"));
            }
        return true;
    }
}
