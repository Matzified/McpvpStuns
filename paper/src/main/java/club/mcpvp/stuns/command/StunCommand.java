package club.mcpvp.stuns.command;

import club.mcpvp.stuns.McPvpStuns;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class StunCommand implements CommandExecutor, TabCompleter {

    private final McPvpStuns plugin;

    public StunCommand(McPvpStuns plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("mcpvpstuns.admin")) {
            sender.sendMessage(Component.text("You do not have permission to execute this command.", NamedTextColor.RED));
            return true;
        }

        if (args.length == 0 || "status".equalsIgnoreCase(args[0])) {
            sender.sendMessage(Component.text("=== [McPvpStuns Status] ===", NamedTextColor.GOLD));
            sender.sendMessage(Component.text("Paper Config Auto-Patch: ", NamedTextColor.YELLOW)
                    .append(Component.text(plugin.getConfig().getBoolean("auto-configure-paper-yml", true), NamedTextColor.GREEN)));
            sender.sendMessage(Component.text("Runtime Stun Override: ", NamedTextColor.YELLOW)
                    .append(Component.text(plugin.getConfig().getBoolean("runtime-stun-override", true), NamedTextColor.GREEN)));
            sender.sendMessage(Component.text("Shield Cooldown: ", NamedTextColor.YELLOW)
                    .append(Component.text((plugin.getConfig().getInt("shield-cooldown-ticks", 100) / 20.0) + "s", NamedTextColor.GREEN)));
            sender.sendMessage(Component.text("Knockback On Followup Only: ", NamedTextColor.YELLOW)
                    .append(Component.text(plugin.getConfig().getBoolean("knockback-on-followup-only", true), NamedTextColor.GREEN)));
            return true;
        }

        if ("reload".equalsIgnoreCase(args[0])) {
            plugin.reloadConfig();
            sender.sendMessage(Component.text("[McPvpStuns] Configuration reloaded successfully.", NamedTextColor.GREEN));
            if (plugin.getConfig().getBoolean("auto-configure-paper-yml", true)) {
                plugin.getPatcher().applyPatch();
            }
            return true;
        }

        sender.sendMessage(Component.text("Usage: /" + label + " [status|reload]", NamedTextColor.RED));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return Stream.of("status", "reload")
                    .filter(sub -> sub.startsWith(prefix))
                    .toList();
        }
        return Collections.emptyList();
    }
}
