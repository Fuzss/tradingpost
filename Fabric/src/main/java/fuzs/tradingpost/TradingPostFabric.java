package fuzs.tradingpost;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import net.fabricmc.api.ModInitializer;

public class TradingPostFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(TradingPost.MOD_ID, TradingPost::new);
    }
}
