package fr.xephi.authme.task;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import fr.xephi.authme.AuthMe;
import fr.xephi.authme.cache.auth.PlayerCache;
import fr.xephi.authme.cache.limbo.LimboCache;


public class MessageTask implements Runnable {

    private AuthMe plugin;
    private String name;
    private String msg;
    private int interval;

    public MessageTask(AuthMe plugin, String name, String msg, int interval) {
        this.plugin = plugin;
        this.name = name;
        this.msg = msg;
        this.interval = interval;
    }

    @Override
    public void run() {
        if (PlayerCache.getInstance().isAuthenticated(name))
            return;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.getName().toLowerCase().equals(name)) {
                player.sendMessage(msg);
                BukkitScheduler sched = plugin.getServer().getScheduler();
                BukkitTask late = sched.runTaskLater(plugin, this, interval * 20);
                if(LimboCache.getInstance().hasLimboPlayer(name)) {
                	LimboCache.getInstance().getLimboPlayer(name).setMessageTaskId(late.getTaskId());
                }
            }
        }
    }
}
