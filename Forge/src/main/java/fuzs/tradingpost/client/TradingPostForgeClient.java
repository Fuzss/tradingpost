package fuzs.tradingpost.client;

import fuzs.puzzleslib.api.client.core.v1.ClientModConstructor;
import fuzs.tradingpost.TradingPost;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;

@Mod.EventBusSubscriber(modid = TradingPost.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class TradingPostForgeClient {

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ClientModConstructor.construct(TradingPost.MOD_ID, TradingPostClient::new);
    }
}
