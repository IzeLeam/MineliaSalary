package dev.minelia.mineliasalary;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SalaryDistributor implements Runnable {

  public static final SalaryDistributor INSTANCE = new SalaryDistributor(MineliaSalary.getInstance());
  private final MineliaSalary plugin;
  private final Map<UUID, Integer> players = new HashMap<>();
  private LocalDate lastReset;

  public SalaryDistributor(MineliaSalary plugin) {
    this.plugin = plugin;
    for (String uuid : plugin.getSalariesFile().getKeys(false)) {
      players.put(UUID.fromString(uuid), plugin.getSalariesFile().getInt(uuid));
    }
    if (plugin.getConfig().getString("last_reset") == null) {
      plugin.getConfig().set("last_reset", LocalDate.now().toString());
      plugin.saveConfig();
    }
    final String lastResetFromConfig = plugin.getConfig().getString("last_reset");
    if (lastResetFromConfig.equals("0")) {
      lastReset = LocalDate.MIN;
    } else {
      lastReset = LocalDate.parse(plugin.getConfig().getString("last_reset"));
    }
  }

  public void addPlayer(Player player) {
    players.computeIfAbsent(player.getUniqueId(), p -> 0);
  }

  public RankSalary getSalary(UUID uuid) {
    User user = LuckPermsProvider.get().getUserManager().getUser(uuid);
    if (user == null) {
      return RankSalary.DEFAULT;
    }

    try {
      return RankSalary.valueOf(user.getPrimaryGroup().toUpperCase());
    } catch (IllegalArgumentException e) {
      return RankSalary.DEFAULT;
    }
  }

  @Override
  public void run() {
    final FileConfiguration config = plugin.getConfig();
    if (LocalDate.now().isAfter(lastReset)) {
      lastReset = LocalDate.now();
      plugin.getConfig().set("last_reset", lastReset.toString());
      plugin.saveConfig();
      for (UUID uuid : players.keySet()) {
        players.put(uuid, 0);
        plugin.getSalariesFile().set(uuid.toString(), 0);
        Player p = Bukkit.getPlayer(uuid);
        if (p != null && p.isOnline() && config.getString("messages.salary_info").isEmpty()) {
          p.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.salary_info")));
        }
      }
      plugin.saveSalariesFile();
    }

    for (UUID uuid: players.keySet()) {
      if (Bukkit.getPlayer(uuid) == null || !Bukkit.getPlayer(uuid).isOnline()) {
        return;
      }

      Player player = Bukkit.getPlayer(uuid);

      if (!config.getString("messages.salary_distribution_broadcast").isEmpty()) {
        player.sendMessage("§8Les salaires ont été distribués.");
      }

      RankSalary rankSalary = this.getSalary(uuid);
      if (players.get(uuid) >= rankSalary.getMaxSalary()) {
        if (plugin.getConfig().getBoolean("limit_reach_message")) {
          player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.limit_reach")
              .replace("%salary%", String.valueOf(rankSalary.getSalary()))
              .replace("%max%", String.valueOf(rankSalary.getMaxSalary()))));
        }
        continue;
      }

      players.put(uuid, players.get(uuid) + rankSalary.getSalary());
      if (!config.getString("messages.salary_distribution").isEmpty()) {
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.salary_distribution")
            .replace("%salary%", String.valueOf(rankSalary.getSalary()))
            .replace("%max%", String.valueOf(rankSalary.getMaxSalary()))));
      }

      plugin.getSalariesFile().set(player.getUniqueId().toString(), players.get(uuid));
    }
    plugin.saveSalariesFile();
  }

  public enum RankSalary {
    DEFAULT(5000, 50000),
    ELU(10000, 100000),
    CHAMPION(15000, 200000),
    MASTER(20000, 400000),
    HERO(20000, 800000),
    LEGEND(40000, 1000000),
    SUPREME(50000, 2000000),
    GOD(70000, 3000000);

    private int salary;
    private int maxSalary;

    RankSalary(int salary, int maxSalary) {
      this.salary = salary;
      this.maxSalary = maxSalary;
    }

    public int getSalary() {
      return salary;
    }

    public int getMaxSalary() {
      return maxSalary;
    }

    public static void loadFromConfig() {
      for (String key : MineliaSalary.getInstance().getConfig().getConfigurationSection("salaires").getKeys(false)) {
        RankSalary rank = RankSalary.valueOf(key.toUpperCase());
        rank.salary = MineliaSalary.getInstance().getConfig().getInt("salaires." + key + ".salaire");
        rank.maxSalary = MineliaSalary.getInstance().getConfig().getInt("salaires." + key + ".max");
      }
    }
  }
}