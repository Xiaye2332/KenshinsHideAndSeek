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
import net.tylermurphy.hideAndSeek.game.*;
import net.tylermurphy.hideAndSeek.game.util.Status;
import net.tylermurphy.hideAndSeek.util.CommandHandler;
import net.tylermurphy.hideAndSeek.game.listener.*;
import net.tylermurphy.hideAndSeek.util.PAPIExpansion;
import net.tylermurphy.hideAndSeek.util.TabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.tylermurphy.hideAndSeek.configuration.Config.exitPosition;
import static net.tylermurphy.hideAndSeek.configuration.Config.exitWorld;

public class Main extends JavaPlugin implements Listener {
	
	private static Main instance;
	private static int version;

	private Database database;
	private Board board;
	private Disguiser disguiser;
	private EntityHider entityHider;
	private Game game;

	public void onEnable() {
		Main.instance = this;
		this.updateVersion();

		Config.loadConfig();
		Localization.loadLocalization();
		Items.loadItems();

		this.board = new Board();
		this.database = new Database();
		this.disguiser = new Disguiser();
		this.entityHider = new EntityHider(this, EntityHider.Policy.BLACKLIST);
		this.registerListeners();

		CommandHandler.registerCommands();

		game = new Game(board);

		getServer().getScheduler().runTaskTimer(this, this::onTick,0,1).getTaskId();

		Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
			new PAPIExpansion().register();
		}
	}

	public void onDisable() {

		version = 0;

		board.getPlayers().forEach(player -> {
			board.removeBoard(player);
			PlayerLoader.unloadPlayer(player);
			player.teleport(new Location(Bukkit.getWorld(exitWorld), exitPosition.getX(), exitPosition.getY(), exitPosition.getZ()));
		});

		Bukkit.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
		board.cleanup();
		disguiser.cleanUp();
	}

	private void onTick() {
		if(game.getStatus() == Status.ENDED) game = new Game(board);
		game.onTick();
		disguiser.check();
	}

	private void registerListeners() {
		getServer().getPluginManager().registerEvents(new BlockedCommandHandler(), this);
		getServer().getPluginManager().registerEvents(new ChatHandler(), this);
		getServer().getPluginManager().registerEvents(new DamageHandler(), this);
		getServer().getPluginManager().registerEvents(new DisguiseHandler(), this);
		getServer().getPluginManager().registerEvents(new InteractHandler(), this);
		getServer().getPluginManager().registerEvents(new InventoryHandler(), this);
		getServer().getPluginManager().registerEvents(new JoinLeaveHandler(), this);
		getServer().getPluginManager().registerEvents(new MovementHandler(), this);
		getServer().getPluginManager().registerEvents(new PlayerHandler(), this);
		getServer().getPluginManager().registerEvents(new RespawnHandler(), this);
	}

	private void updateVersion(){
		Matcher matcher = Pattern.compile("MC: \\d\\.(\\d+)").matcher(Bukkit.getVersion());
		if (matcher.find()) {
			version = Integer.parseInt(matcher.group(1));
		} else {
			throw new IllegalArgumentException("Failed to parse server version from: " + Bukkit.getVersion());
		}
	}
	
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
		return CommandHandler.handleCommand(sender, args);
	}
	
	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		return TabCompleter.handleTabComplete(sender, args);
	}

	public static Main getInstance() {
		return instance;
	}

	public File getWorldContainer() {
		return this.getServer().getWorldContainer();
	}

	public Database getDatabase() {
		return database;
	}

	public Board getBoard(){
		return board;
	}

	public Game getGame(){
		return game;
	}

	public Disguiser getDisguiser() { return disguiser; }

	public EntityHider getEntityHider() { return entityHider; }

	public boolean supports(int v){
		return version >= v;
	}
	
}