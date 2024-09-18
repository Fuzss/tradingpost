package fuzs.tradingpost.config;

import fuzs.puzzleslib.api.config.v3.Config;
import fuzs.puzzleslib.api.config.v3.ConfigCore;

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
}
