package fuzs.tradingpost.config;

import com.google.common.collect.Lists;
import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;
import fuzs.puzzleslib.api.config.v3.serialization.ConfigDataSet;
import fuzs.tradingpost.TradingPost;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class ServerConfig implements ConfigCore {
    @Config(description = "Range on xz plane trading post should search for merchants.")
    @Config.IntRange(min = 1, max = 96)
    public int horizontalRange = 24;
    @Config(description = "Range on y axis trading post should search for merchants.")
    @Config.IntRange(min = 1, max = 96)
    public int verticalRange = 16;
    @Config(description = "Disable traders on the trading screen when they wander out of range.")
    public boolean enforceRange = false;
    @Config(description = "Teleport xp from trading from villagers on top of the trading post.")
    public boolean teleportXp = true;
    @Config(name = "close_empty_screen", description = "Close trading post interface when all traders have become unavailable.")
    public boolean closeScreen = true;
    @Config(name = "trader_blacklist", description = {"Trader entities disabled from being found by the trading post.", "Modders may add their own incompatible trader entities via the \"" + TradingPost.MOD_ID + ":blacklisted_traders\" entity tag.", ConfigDataSet.CONFIG_DESCRIPTION})
    List<String> traderBlacklistRaw = Lists.newArrayList();

    public ConfigDataSet<EntityType<?>> traderBlacklist;

    @Override
    public void afterConfigReload() {
        this.traderBlacklist = ConfigDataSet.from(Registries.ENTITY_TYPE, this.traderBlacklistRaw);
    }
}
