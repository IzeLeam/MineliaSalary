package dev.minelia.mineliasalary;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    SalaryDistributor.INSTANCE.addPlayer(event.getPlayer());
  }

  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    SalaryDistributor.INSTANCE.removePlayer(event.getPlayer());
  }
}