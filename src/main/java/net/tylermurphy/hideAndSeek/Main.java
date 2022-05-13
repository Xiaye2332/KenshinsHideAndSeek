/*
 * This file is part of Kenshins Hide and Seek
 *
 * Copyright (c) 2021 Tyler Murphy.
 *
 * Kenshins Hide and Seek free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * he Free Software Foundation version 3.
 *
 * Kenshins Hide and Seek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package net.tylermurphy.hideAndSeek;

import net.tylermurphy.hideAndSeek.configuration.Config;
import net.tylermurphy.hideAndSeek.configuration.Items;
import net.tylermurphy.hideAndSeek.configuration.Localization;
import net.tylermurphy.hideAndSeek.database.Database;
import net.tylermurphy.hideAndSeek.game.Board;
import net.tylermurphy.hideAndSeek.game.CommandHandler;
import net.tylermurphy.hideAndSeek.game.Game;
import net.tylermurphy.hideAndSeek.game.listener.*;
import net.tylermurphy.hideAndSeek.util.PAPIExpansion;
import net.tylermurphy.hideAndSeek.util.TabCompleter;
import net.tylermurphy.hideAndSeek.util.UUIDFetcher;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class Main extends JavaPlugin implements Listener {
	
	public static Main plugin;
	public static File root, data;
	private int onTickTask;

	public void onEnable() {
		plugin = this;
		root = this.getServer().getWorldContainer();
		data = this.getDataFolder();

		this.registerListeners();

		Config.loadConfig();
		Localization.loadLocalization();
		Items.loadItems();

		CommandHandler.registerCommands();
		Board.reload();
		Database.init();
		UUIDFetcher.init();

		onTickTask = Bukkit.getServer().getScheduler().runTaskTimer(this, () -> {
			try{
				Game.onTick();
			} catch (Exception e) {
				e.printStackTrace();
			}
		},0,1).getTaskId();

		Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new PAPIExpansion().register();
		}
	}
	
	public void onDisable() {
		Main.plugin.getServer().getScheduler().cancelTask(onTickTask);
		Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
		UUIDFetcher.cleanup();
		Board.cleanup();
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new BlockedCommandHandler(), this);
		getServer().getPluginManager().registerEvents(new ChatHandler(), this);
		getServer().getPluginManager().registerEvents(new DamageHandler(), this);
		getServer().getPluginManager().registerEvents(new InteractHandler(), this);
		getServer().getPluginManager().registerEvents(new JoinLeaveHandler(), this);
		getServer().getPluginManager().registerEvents(new MovementHandler(), this);
		getServer().getPluginManager().registerEvents(new PlayerHandler(), this);
		getServer().getPluginManager().registerEvents(new RespawnHandler(), this);
	}
	
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		return CommandHandler.handleCommand(sender, args);
	}
	
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		return TabCompleter.handleTabComplete(sender, args);
	}
	
}