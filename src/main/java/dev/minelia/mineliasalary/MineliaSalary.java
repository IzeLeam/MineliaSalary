package dev.minelia.mineliasalary;

import dev.minelia.mineliasalary.SalaryDistributor.RankSalary;
import java.io.File;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineliaSalary extends JavaPlugin {

  private static MineliaSalary INSTANCE;
  private YamlConfiguration salaries;

  public YamlConfiguration getSalariesFile() {
    return salaries;
  }

  public void saveSalariesFile() {
    try {
      salaries.save(new File(getDataFolder() + "/salaires.yml"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static MineliaSalary getInstance() {
    return INSTANCE;
  }

  @Override
  public void onEnable() {
    INSTANCE = this;

    if (!this.getDataFolder().exists()) {
      if (!this.getDataFolder().mkdir()) {
        this.getLogger().severe("Impossible de créer le dossier de configuration");
        this.getServer().getPluginManager().disablePlugin(this);
      }
      YamlConfiguration config = new YamlConfiguration();
      try {
        config.save(this.getDataFolder() + "/salaires.yml");
      } catch (Exception e) {
        e.printStackTrace();
      }
      this.salaries = config;
    } else {
      this.salaries = YamlConfiguration.loadConfiguration(new File(this.getDataFolder() + "/salaires.yml"));
    }
    saveDefaultConfig();

    this.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);
    this.getCommand("salary").setExecutor(new SalaryCommand());

    getServer().getScheduler().runTaskTimerAsynchronously(this, SalaryDistributor.INSTANCE, 0, 20 * 60 * 20);
    RankSalary.loadFromConfig();
  }

  @Override
  public void onDisable() {
    saveSalariesFile();
    saveConfig();
  }
}