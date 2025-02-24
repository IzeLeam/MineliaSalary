package dev.minelia.mineliasalary;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
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
      final int salary = SalaryDistributor.INSTANCE.getSalary(player.getUniqueId());
      final int maxSalary = SalaryDistributor.INSTANCE.getMaxSalary(player.getUniqueId());

      final FileConfiguration config = MineliaSalary.getInstance().getConfig();
      if (config.getString("messages.salary_info").isEmpty()) {
        return true;
      }

      User user = LuckPermsProvider.get().getUserManager().getUser(player.getUniqueId());
      if (user == null) {
        player.sendMessage(ChatColor.RED + "An error occurred while fetching your rank.");
        return true;
      }
      player.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.salary_info")
          .replace("%salary%", String.valueOf(salary))
          .replace("%max%", String.valueOf(maxSalary))
          .replace("%rank%", user.getPrimaryGroup())));
    }
    return true;
  }
}
