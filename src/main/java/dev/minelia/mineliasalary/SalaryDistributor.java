package dev.minelia.mineliasalary;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class SalaryDistributor implements Runnable {

  public static final SalaryDistributor INSTANCE = new SalaryDistributor(MineliaSalary.getInstance());
  private final MineliaSalary plugin;
  private final Map<UUID, Integer> players = new HashMap<>();
  private LocalDate lastReset;
  private YamlConfiguration config;

  public SalaryDistributor(MineliaSalary plugin) {
    this.plugin = plugin;
    initConfig();
    this.config = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
  }

  private void initConfig() {
    final File file = new File(plugin.getDataFolder(), "config.yml");
    if (!file.exists()) {
      plugin.saveResource("config.yml", false);
    }
  }

  private void saveConfig() {
    try {
      config.save(new File(plugin.getDataFolder(), "config.yml"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void addPlayer(Player player) {
    if (config.getString("players." + player.getUniqueId() + ".salary") == null) {
      config.set("players." + player.getUniqueId() + ".salary", 0);
      saveConfig();
    } else {
      players.put(player.getUniqueId(), config.getInt("players." + player.getUniqueId() + ".salary"));
    }
  }

  public void removePlayer(Player player) {
    players.remove(player.getUniqueId());
  }

  public int getSalary(UUID uuid) {
    User user = LuckPermsProvider.get().getUserManager().getUser(uuid);
    if (user == null) {
      Bukkit.getLogger().warning("User " + uuid + " not found.");
      return 0;
    }

    return config.getInt("salaires." + user.getPrimaryGroup().toLowerCase() + ".salaire");
  }

  public int getMaxSalary(UUID uuid) {
    User user = LuckPermsProvider.get().getUserManager().getUser(uuid);
    if (user == null) {
      Bukkit.getLogger().warning("User " + uuid + " not found.");
      return 0;
    }

    return config.getInt("salaires." + user.getPrimaryGroup().toLowerCase() + ".max");
  }

  @Override
  public void run() {
    if (lastReset == null || lastReset.getDayOfMonth() != LocalDate.now().getDayOfMonth()) {
      lastReset = LocalDate.now();
      config.set("last_reset", lastReset.toString());
      saveConfig();
      players.clear();
      for (Player player: Bukkit.getOnlinePlayers()) {
        addPlayer(player);
      }
      config.set("players", null);
      saveConfig();
    }

    for (UUID uuid: players.keySet()) {
      if (Bukkit.getPlayer(uuid) == null || !Bukkit.getPlayer(uuid).isOnline()) {
        return;
      }

      Player player = Bukkit.getPlayer(uuid);

      if (!config.getString("messages.salary_distribution_broadcast").isEmpty()) {
        player.sendMessage("§8Les salaires ont été distribués.");
      }

      final int salary = getSalary(uuid);
      final int maxSalary = getMaxSalary(uuid);

      if (players.get(uuid) >= maxSalary) {
        if (plugin.getConfig().getBoolean("limit_reach_message")) {
          player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.limit_reach")
              .replace("%salary%", String.valueOf(salary))
              .replace("%max%", String.valueOf(maxSalary))));
        }
        continue;
      }

      players.put(uuid, players.get(uuid) + salary);
      if (!config.getString("messages.salary_distribution").isEmpty()) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.salary_distribution")
            .replace("%salary%", String.valueOf(salary))
            .replace("%max%", String.valueOf(maxSalary))));
      }

      config.set("players." + uuid + ".salary", players.get(uuid));
      saveConfig();
    }
  }
}