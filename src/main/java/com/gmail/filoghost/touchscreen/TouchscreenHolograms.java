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
package com.gmail.filoghost.touchscreen;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.bstats.bukkit.MetricsLite;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.gmail.filoghost.holographicdisplays.object.NamedHologramManager;
import com.gmail.filoghost.touchscreen.command.RootCommandHandler;
import com.gmail.filoghost.touchscreen.disk.Settings;
import com.gmail.filoghost.touchscreen.disk.TouchHologramStorage;
import com.gmail.filoghost.touchscreen.listener.EventListener;
import com.gmail.filoghost.touchscreen.touch.TouchHologram;
import com.gmail.filoghost.touchscreen.touch.TouchManager;
import com.gmail.filoghost.touchscreen.utils.ConsoleLogger;
import com.gmail.filoghost.updater.ResponseHandler;
import com.gmail.filoghost.updater.UpdateChecker;

public class TouchscreenHolograms extends JavaPlugin {
	
	private static TouchscreenHolograms instance;
	private static Settings settings;
	private static TouchHologramStorage fileStorage;
	private static TouchManager touchManager;
	private static String newVersion;
	
	@Override
	public void onEnable() {
		instance = this;
		
		// Load the settings
		settings = new Settings(new File(getDataFolder(), "config.yml"));
		try {
			settings.load();
		} catch (IOException ex) {
			ex.printStackTrace();
			printErrorAndDisable(
				"******************************************************",
				"     Could not load config.yml.",
				"     " + getDescription().getName() + " will be disabled.",
				"******************************************************"
			);
			return;
		}
		
		// Check for updates
		if (settings.updateNotification) {
			UpdateChecker.run(this, 77049, new ResponseHandler() {
				
				@Override
				public void onUpdateFound(final String newVersion) {
					TouchscreenHolograms.newVersion = newVersion;
					ConsoleLogger.log(Level.INFO, "Found a new version available: " + newVersion);
					ConsoleLogger.log(Level.INFO, "Download it on Bukkit Dev:");
					ConsoleLogger.log(Level.INFO, "https://dev.bukkit.org/projects/touchscreen-holograms");
				}
				
			});
		}
		
		// Check that Holographic Displays is present and enabled
		Plugin holographicDisplaysPlugin = Bukkit.getPluginManager().getPlugin("HolographicDisplays");
		try {
			Class.forName("com.gmail.filoghost.holographicdisplays.HolographicDisplays");
		} catch (Exception e) {
			holographicDisplaysPlugin = null;
		}
		
		if (holographicDisplaysPlugin == null || !holographicDisplaysPlugin.isEnabled()) {
			printErrorAndDisable(
				"******************************************************",
				"     " + getDescription().getName() + " requires the plugin",
				"     HolographicDisplays v2.0+ enabled to run.",
				"     This plugin will be disabled.",
				"******************************************************"
			);
			return;
		}
		
		// Load the database
		fileStorage = new TouchHologramStorage(new File(getDataFolder(), "database.yml"));
		try {
			fileStorage.load();
		} catch (IOException ex) {
			ex.printStackTrace();
			printErrorAndDisable(
				"******************************************************",
				"     Could not load database.yml.",
				"     " + getDescription().getName() + " will be disabled.",
				"******************************************************"
			);
			return;
		}
		
		// Register the touch holograms into the TouchManager
		touchManager = new TouchManager();
		for (String touchHologramName : fileStorage.getTouchHolograms()) {
			touchManager.add(fileStorage.loadTouchHologram(touchHologramName));
		}		
		
		// Set the command handler
		getCommand("touch").setExecutor(new RootCommandHandler());
		
		// Register events
		Bukkit.getPluginManager().registerEvents(new EventListener(), this);
		
		// bStats metrics
		new MetricsLite(this);		
		
		// The entities are loaded when the server is ready
		Bukkit.getScheduler().runTaskLater(this, new Runnable() {
			
			@Override
			public void run() {
				// Check that holograms still exist in Holographic Displays
				boolean fileStorageUpdated = false;
				
				for (TouchHologram touchHologram : touchManager.getTouchHolograms()) {
					String name = touchHologram.getLinkedHologramName();
					if (NamedHologramManager.getHologram(name) == null) {
						fileStorage.deleteTouchHologram(name);
						fileStorageUpdated = true;
						ConsoleLogger.log(Level.WARNING, "Cannot find the hologram '" + name + "'. It was probably deleted from HolographicDisplays, commands have been removed too.");
					}
				}
				
				if (fileStorageUpdated) {
					fileStorage.trySaveToDisk();
				}
				
				// Update touch handlers
				touchManager.refreshHolograms();
			}
			
		}, 20L);
	}
	
	
	private static void printErrorAndDisable(String... messages) {
		StringBuffer buffer = new StringBuffer("\n ");
		for (String message : messages) {
			buffer.append('\n');
			buffer.append(message);
		}
		buffer.append('\n');
		System.out.println(buffer.toString());
		try {
			Thread.sleep(5000);
		} catch (InterruptedException ex) { }
		instance.setEnabled(false);
	}
	
	
	public static TouchscreenHolograms getInstance() {
		return instance;
	}
	
	public static Settings getSettings() {
		return settings;
	}
	
	public static TouchHologramStorage getFileStorage() {
		return fileStorage;
	}
	
	public static TouchManager getTouchManager() {
		return touchManager;
	}
	
	public static String getNewVersion() {
		return newVersion;
	}
	
}
