package fuzs.tradingpost.client;

import fuzs.puzzleslib.client.core.ClientCoreServices;
import fuzs.tradingpost.TradingPost;
import net.fabricmc.api.ClientModInitializer;

public class TradingPostFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientCoreServices.FACTORIES.clientModConstructor(TradingPost.MOD_ID).accept(new TradingPostClient());
    }
}
