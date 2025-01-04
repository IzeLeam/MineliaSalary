package dev.minelia.mineliasalary;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerConnectionListener implements Listener {

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    SalaryDistributor.INSTANCE.addPlayer(event.getPlayer());
  }
}