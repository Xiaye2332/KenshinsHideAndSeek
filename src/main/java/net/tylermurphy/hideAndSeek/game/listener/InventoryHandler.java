/*
 * This file is part of Kenshins Hide and Seek
 *
 * Copyright (c) 2022 Tyler Murphy.
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

package net.tylermurphy.hideAndSeek.game.listener;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;

import com.cryptomorin.xseries.XMaterial;
import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.command.Debug;
import net.tylermurphy.hideAndSeek.game.util.Status;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        checkForInventoryMove(event);
        checkForSpectatorTeleportMenu(event);
        checkForDebugMenu(event);
        checkForBlockHuntMenu(event);
    }

    private void checkForInventoryMove(InventoryClickEvent event){
        if (Main.getInstance().getBoard().contains((Player) event.getWhoClicked()) && Main.getInstance().getGame().getStatus() == Status.STANDBY) {
            event.setCancelled(true);
        }
    }

    private void checkForSpectatorTeleportMenu(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        if (Main.getInstance().getBoard().isSpectator(player) && event.getCurrentItem().getType() == XMaterial.PLAYER_HEAD.parseMaterial()) {
            event.setCancelled(true);
            player.closeInventory();
            String name = event.getCurrentItem().getItemMeta().getDisplayName();
            Player clicked = Main.getInstance().getServer().getPlayer(name);
            if(clicked == null) return;
            player.teleport(clicked);
        }
    }

    private void checkForDebugMenu(InventoryClickEvent event){
        Player player = (Player) event.getWhoClicked();
        boolean debug;
        if(Main.getInstance().supports(14)){
            debug = event.getView().getTitle().equals("Debug Menu") && player.hasPermission("hideandseek.debug");
        } else {
            debug = event.getInventory().getName().equals("Debug Menu") && player.hasPermission("hideandseek.debug");
        }
        if (debug){
            event.setCancelled(true);
            player.closeInventory();
            Debug.handleOption(player, event.getRawSlot());
        }
    }

    private void checkForBlockHuntMenu(InventoryClickEvent event){
        boolean test;
        if(Main.getInstance().supports(14)){
            test = event.getView().getTitle().equals("Select a Block");
        } else {
            test = event.getInventory().getName().equals("Select a Block");
        }
        if(!test) return;
        event.setCancelled(true);
        Material mat = blockhuntBlocks.get(event.getRawSlot());
        if(mat == null) return;
        Player player = (Player) event.getWhoClicked();
        Main.getInstance().getDisguiser().disguise(player, mat);
        player.closeInventory();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event){
        if (!(event.getPlayer() instanceof Player)) return;
        boolean test;
        if(Main.getInstance().supports(14)){
            test = event.getView().getTitle().equals("Select a Block");
        } else {
            test = event.getInventory().getName().equals("Select a Block");
        }
        if(!test) return;
        Material mat = blockhuntBlocks.get(0);
        if(mat == null) return;
        Player player = (Player) event.getPlayer();
        if(Main.getInstance().getDisguiser().disguised(player)) return;
        Main.getInstance().getDisguiser().disguise(player, mat);
        player.closeInventory();
    }

}
