package fuzs.tradingpost.fabric.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.client.TradingPostClient;
import net.fabricmc.api.ClientModInitializer;

public class TradingPostFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientModConstructor.construct(TradingPost.MOD_ID, TradingPostClient::new);
    }
}
