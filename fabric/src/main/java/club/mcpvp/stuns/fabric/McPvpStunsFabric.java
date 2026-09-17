package club.mcpvp.stuns.fabric;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class McPvpStunsFabric implements ModInitializer {

    public static final String MOD_ID = "mcpvpstuns";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[McPvpStuns] McPvpStuns Fabric mod initialized successfully! (1:1 mcpvp shield stun active)");
    }
}
