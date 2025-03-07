package net.tylermurphy.hideAndSeek.game.listener;

import com.cryptomorin.xseries.XSound;
import net.tylermurphy.hideAndSeek.Main;
import net.tylermurphy.hideAndSeek.game.Board;
import net.tylermurphy.hideAndSeek.game.Game;
import net.tylermurphy.hideAndSeek.game.PlayerLoader;
import net.tylermurphy.hideAndSeek.game.util.Status;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import static net.tylermurphy.hideAndSeek.configuration.Config.*;
import static net.tylermurphy.hideAndSeek.configuration.Config.spawnPosition;
import static net.tylermurphy.hideAndSeek.configuration.Localization.message;

public class DamageHandler implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent event) {
        Board board = Main.getInstance().getBoard();
        Game game = Main.getInstance().getGame();
        // If you are not a player, get out of here
        if (!(event.getEntity() instanceof Player)) return;
        // Define variables
        Player player = (Player) event.getEntity();
        Player attacker = null;
        // If no spawn position, we won't be able to manage their death :o
        if (spawnPosition == null) { return; }
        // If there is an attacker, find them
        if (event instanceof EntityDamageByEntityEvent) {
            if (((EntityDamageByEntityEvent) event).getDamager() instanceof Player)
                attacker = (Player) ((EntityDamageByEntityEvent) event).getDamager();
            else if (((EntityDamageByEntityEvent) event).getDamager() instanceof Projectile)
                if (((Projectile) ((EntityDamageByEntityEvent) event).getDamager()).getShooter() instanceof Player)
                    attacker = (Player) ((Projectile) ((EntityDamageByEntityEvent) event).getDamager()).getShooter();
        }
        // Makes sure that if there was an attacking player, that the event is allowed for the game
        if (attacker != null) {
            System.out.println(event.getFinalDamage() + " " + player.getDisplayName() + " " + attacker.getDisplayName());
            // Cancel if one player is in the game but other isn't
            if ((board.contains(player) && !board.contains(attacker)) || (!board.contains(player) && board.contains(attacker))) {
                event.setCancelled(true);
                return;
                // Ignore event if neither player are in the game
            } else if (!board.contains(player) && !board.contains(attacker)) {
                return;
                // Ignore event if players are on the same team, or one of them is a spectator
            } else if (board.onSameTeam(player, attacker) || board.isSpectator(player) || board.isSpectator(attacker)) {
                event.setCancelled(true);
                return;
                // Ignore the event if pvp is disabled, and a hider is trying to attack a seeker
            } else if (!pvpEnabled && board.isHider(attacker) && board.isSeeker(player)) {
                event.setCancelled(true);
                return;
            }
        // If there was no attacker, if the damaged is not a player, ignore them.
        } else if (!board.contains(player)) {
            return;
        // If there is no attacker, it most of been by natural causes. If pvp is disabled, and config doesn't allow natural causes, cancel event.
        } else if (!pvpEnabled && !allowNaturalCauses && board.contains(player)) {
            event.setCancelled(true);
            return;
        }
        // Spectators and cannot take damage
        if (board.isSpectator(player)) {
            event.setCancelled(true);
            if (Main.getInstance().supports(18) && player.getLocation().getBlockY() < -64) {
                player.teleport(new Location(Bukkit.getWorld(game.getGameWorld()), spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ()));
            } else if (!Main.getInstance().supports(18) && player.getLocation().getY() < 0) {
                player.teleport(new Location(Bukkit.getWorld(game.getGameWorld()), spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ()));
            }
            return;
        }
        // Players cannot take damage while game is not in session
        if (board.contains(player) && game.getStatus() != Status.PLAYING){
            event.setCancelled(true);
            return;
        }
        // Check if player dies (pvp mode)
        if(pvpEnabled && player.getHealth() - event.getFinalDamage() >= 0.5) return;
        // Handle death event
        event.setCancelled(true);
        // Play death effect
        if (Main.getInstance().supports(9)) {
            XSound.ENTITY_PLAYER_DEATH.play(player, 1, 1);
        } else {
            XSound.ENTITY_PLAYER_HURT.play(player, 1, 1);
        }
        // Reveal player if they are disguised
        Main.getInstance().getDisguiser().reveal(player);
        // Teleport player to seeker spawn
        if(delayedRespawn){
            player.teleport(new Location(Bukkit.getWorld(game.getGameWorld()), seekerLobbyPosition.getX(), seekerLobbyPosition.getY(), seekerLobbyPosition.getZ()));
            player.sendMessage(messagePrefix + message("RESPAWN_NOTICE").addAmount(delayedRespawnDelay));
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> {
                if(game.getStatus() == Status.PLAYING){
                    player.teleport(new Location(Bukkit.getWorld(game.getGameWorld()), spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ()));
                }
            }, delayedRespawnDelay * 20L);
        } else {
            player.teleport(new Location(Bukkit.getWorld(game.getGameWorld()), spawnPosition.getX(), spawnPosition.getY(), spawnPosition.getZ()));
        }
        // Add leaderboard stats
        board.addDeath(player.getUniqueId());
        if (attacker != null) board.addKill(attacker.getUniqueId());
        // Broadcast player death message
        if (board.isSeeker(player)) {
            game.broadcastMessage(message("GAME_PLAYER_DEATH").addPlayer(player).toString());
        } else if (board.isHider(player)) {
            if (attacker == null) {
                game.broadcastMessage(message("GAME_PLAYER_FOUND").addPlayer(player).toString());
            } else {
                game.broadcastMessage(message("GAME_PLAYER_FOUND_BY").addPlayer(player).addPlayer(attacker).toString());
            }
            board.addSeeker(player);
        }
        //Reload player
        PlayerLoader.resetPlayer(player, board);
        board.reloadBoardTeams();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event){
        Main.getInstance().getDisguiser().reveal(event.getEntity());
    }

}
