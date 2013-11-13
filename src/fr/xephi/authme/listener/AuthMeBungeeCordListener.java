package fr.xephi.authme.listener;

import net.md_5.bungee.api.event.ChatEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import fr.xephi.authme.Utils;
import fr.xephi.authme.cache.auth.PlayerCache;
import fr.xephi.authme.datasource.DataSource;
import fr.xephi.authme.settings.Settings;


public class AuthMeBungeeCordListener implements Listener {
	private DataSource data;

    public AuthMeBungeeCordListener(DataSource data) {
        this.data = data; 
    }

	@EventHandler (priority = EventPriority.LOWEST)
	public void onBungeeChatEvent(ChatEvent event) {
		if (!Settings.bungee) return;
		if (event.isCancelled()) return;
		if (!event.isCommand()) return;
		Player player = null;
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (p.getAddress().getAddress().equals(event.getReceiver().getAddress().getAddress())) {
				player = p;
			}
		}
        String name = player.getName().toLowerCase();

        if (Utils.getInstance().isUnrestricted(player)) {
            return;
        }

        if (PlayerCache.getInstance().isAuthenticated(name)) {
            return;
        }

        if (!data.isAuthAvailable(name)) {
            if (!Settings.isForcedRegistrationEnabled) {
                return;
            }
        }
        event.setMessage("/unreacheablecommand");
        event.setCancelled(true);
	}
}
