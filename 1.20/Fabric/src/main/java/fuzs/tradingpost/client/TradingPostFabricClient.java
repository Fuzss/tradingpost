package fuzs.tradingpost.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.tradingpost.TradingPost;
import net.fabricmc.api.ClientModInitializer;

public class TradingPostFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(TradingPost.MOD_ID, TradingPostClient::new);
    }
}
