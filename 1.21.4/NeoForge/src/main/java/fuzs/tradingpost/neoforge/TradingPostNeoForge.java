package fuzs.tradingpost.neoforge;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.data.ModBlockLootProvider;
import fuzs.tradingpost.data.ModBlockTagProvider;
import fuzs.tradingpost.data.ModEntityTypeTagProvider;
import fuzs.tradingpost.data.ModRecipeProvider;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(TradingPost.MOD_ID)
public class TradingPostNeoForge {

    public TradingPostNeoForge(ModContainer modContainer) {
        ModConstructor.construct(TradingPost.MOD_ID, TradingPost::new);
        DataProviderHelper.registerDataProviders(TradingPost.MOD_ID,
                ModBlockLootProvider::new,
                ModBlockTagProvider::new,
                ModEntityTypeTagProvider::new,
                ModRecipeProvider::new
        );
    }
}
