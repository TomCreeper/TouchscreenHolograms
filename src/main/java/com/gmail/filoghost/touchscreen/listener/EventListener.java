/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package com.gmail.filoghost.touchscreen.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.gmail.filoghost.holographicdisplays.event.HolographicDisplaysReloadEvent;
import com.gmail.filoghost.holographicdisplays.event.NamedHologramEditedEvent;
import com.gmail.filoghost.touchscreen.Perms;
import com.gmail.filoghost.touchscreen.TouchscreenHolograms;
import com.gmail.filoghost.touchscreen.touch.TouchHologram;

public class EventListener implements Listener {
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if (TouchscreenHolograms.getNewVersion() != null && event.getPlayer().hasPermission(Perms.MAIN_PERMISSION) && TouchscreenHolograms.getSettings().updateNotification) {
			event.getPlayer().sendMessage(ChatColor.DARK_GREEN + "[Touchscreen Holograms] " + ChatColor.GREEN + "Found an update: " + TouchscreenHolograms.getNewVersion() + ". Download:");
			event.getPlayer().sendMessage(ChatColor.DARK_GREEN + ">> " + ChatColor.GREEN + "https://dev.bukkit.org/projects/touchscreen-holograms");
		}
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		TouchHologram.removeCooldown(event.getPlayer());
	}
	
	@EventHandler
	public void onHologramEdit(NamedHologramEditedEvent event) {
		TouchscreenHolograms.getTouchManager().refreshHolograms();
	}
	
	@EventHandler
	public void onHolographicDisplaysReload(HolographicDisplaysReloadEvent event) {
		TouchscreenHolograms.getTouchManager().refreshHolograms();
	}
	
	
}
