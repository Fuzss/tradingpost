package fuzs.tradingpost.neoforge.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.client.TradingPostClient;
import fuzs.tradingpost.data.client.ModLanguageProvider;
import fuzs.tradingpost.data.client.ModModelProvider;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = TradingPost.MOD_ID, dist = Dist.CLIENT)
public class TradingPostNeoForgeClient {

    public TradingPostNeoForgeClient(ModContainer modContainer) {
        ClientModConstructor.construct(TradingPost.MOD_ID, TradingPostClient::new);
        DataProviderHelper.registerDataProviders(TradingPost.MOD_ID,
                ModLanguageProvider::new,
                ModModelProvider::new
        );
    }
}
