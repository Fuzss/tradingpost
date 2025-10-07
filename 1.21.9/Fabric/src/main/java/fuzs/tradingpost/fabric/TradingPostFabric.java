package fuzs.tradingpost.fabric;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.tradingpost.TradingPost;
import net.fabricmc.api.ModInitializer;

public class TradingPostFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        ModConstructor.construct(TradingPost.MOD_ID, TradingPost::new);
    }
}
