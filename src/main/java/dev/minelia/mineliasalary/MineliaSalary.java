package dev.minelia.mineliasalary;

import org.bukkit.plugin.java.JavaPlugin;

public final class MineliaSalary extends JavaPlugin {

  private static MineliaSalary INSTANCE;

  public static MineliaSalary getInstance() {
    return INSTANCE;
  }

  @Override
  public void onEnable() {
    INSTANCE = this;

    this.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);
    this.getCommand("salary").setExecutor(new SalaryCommand());

    getServer().getScheduler().runTaskTimerAsynchronously(this, SalaryDistributor.INSTANCE, 0, 20 * 60);
  }
}