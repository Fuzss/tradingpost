package fuzs.tradingpost.forge;

import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.tradingpost.TradingPost;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod(TradingPost.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TradingPostForge {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ModConstructor.construct(TradingPost.MOD_ID, TradingPost::new);
    }
}
