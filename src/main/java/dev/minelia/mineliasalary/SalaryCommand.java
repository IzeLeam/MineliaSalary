package dev.minelia.mineliasalary;

import dev.minelia.mineliasalary.SalaryDistributor.RankSalary;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SalaryCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
    if (commandSender instanceof Player) {
      Player player = (Player) commandSender;
      RankSalary rankSalary = SalaryDistributor.INSTANCE.getSalary(player.getUniqueId());

      final FileConfiguration config = MineliaSalary.getInstance().getConfig();
      if (config.getString("messages.salary_info").isEmpty()) {
        return true;
      }
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.salary_info")
          .replace("%salary%", String.valueOf(rankSalary.getSalary()))
          .replace("%max%", String.valueOf(rankSalary.getMaxSalary()))));
    }
    return true;
  }
}
