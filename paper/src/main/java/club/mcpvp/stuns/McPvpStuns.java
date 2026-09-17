package club.mcpvp.stuns;

import club.mcpvp.stuns.command.StunCommand;
import club.mcpvp.stuns.config.PaperConfigPatcher;
import club.mcpvp.stuns.listener.StunCombatListener;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class McPvpStuns extends JavaPlugin {

    private PaperConfigPatcher patcher;

    @Override
    public void onLoad() {
        // Run config patcher as early as STARTUP phase
        patcher = new PaperConfigPatcher(getLogger());
        saveDefaultConfig();
        if (getConfig().getBoolean("auto-configure-paper-yml", true)) {
            getLogger().info("[McPvpStuns] Inspecting paper-global.yml configuration on startup...");
            patcher.applyPatch();
        }
    }

    @Override
    public void onEnable() {
        getLogger().info("[McPvpStuns] McPvpStuns plugin is enabling...");

        // Ensure config is loaded
        saveDefaultConfig();

        // Register combat event listener
        getServer().getPluginManager().registerEvents(new StunCombatListener(this), this);

        // Register command
        StunCommand stunCommand = new StunCommand(this);
        PluginCommand cmd = getCommand("mcpvpstuns");
        if (cmd != null) {
            cmd.setExecutor(stunCommand);
            cmd.setTabCompleter(stunCommand);
        }

        getLogger().info("[McPvpStuns] McPvpStuns enabled successfully! 1:1 mcpvp stun mechanics active.");
    }

    @Override
    public void onDisable() {
        getLogger().info("[McPvpStuns] McPvpStuns disabled.");
    }

    public PaperConfigPatcher getPatcher() {
        return patcher;
    }
}
