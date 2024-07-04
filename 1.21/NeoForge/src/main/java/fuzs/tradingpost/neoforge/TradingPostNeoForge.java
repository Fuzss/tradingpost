package fuzs.tradingpost.neoforge;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.neoforge.api.data.v2.core.DataProviderHelper;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.data.ModBlockLootProvider;
import fuzs.tradingpost.data.ModBlockTagProvider;
import fuzs.tradingpost.data.ModEntityTypeTagProvider;
import fuzs.tradingpost.data.ModRecipeProvider;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

@Mod(TradingPost.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TradingPostNeoForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(TradingPost.MOD_ID, TradingPost::new);
        DataProviderHelper.registerDataProviders(TradingPost.MOD_ID,
                ModBlockLootProvider::new,
                ModBlockTagProvider::new,
                ModEntityTypeTagProvider::new,
                ModRecipeProvider::new
        );
    }
}
