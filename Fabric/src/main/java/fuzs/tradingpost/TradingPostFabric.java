package fuzs.tradingpost;

import fuzs.puzzleslib.core.CoreServices;
import net.fabricmc.api.ModInitializer;

public class TradingPostFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CoreServices.FACTORIES.modConstructor(TradingPost.MOD_ID).accept(new TradingPost());
    }
}
