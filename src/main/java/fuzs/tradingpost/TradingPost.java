package fuzs.tradingpost;

import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.config.ConfigHolderImpl;
import fuzs.puzzleslib.network.MessageDirection;
import fuzs.puzzleslib.network.NetworkHandler;
import fuzs.puzzleslib.registry.FuelManager;
import fuzs.tradingpost.config.ServerConfig;
import fuzs.tradingpost.network.client.message.C2SClearSlotsMessage;
import fuzs.tradingpost.network.message.S2CBuildOffersMessage;
import fuzs.tradingpost.network.message.S2CMerchantDataMessage;
import fuzs.tradingpost.network.message.S2CRemoveMerchantsMessage;
import fuzs.tradingpost.registry.ModRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(TradingPost.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TradingPost {
    public static final String MOD_ID = "tradingpost";
    public static final String MOD_NAME = "Trading Post";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandler NETWORK = NetworkHandler.of(MOD_ID);
    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder<AbstractConfig, ServerConfig> CONFIG = ConfigHolder.server(() -> new ServerConfig());

    @SubscribeEvent
    public static void onConstructMod(final FMLConstructModEvent evt) {
        ((ConfigHolderImpl<?, ?>) CONFIG).addConfigs(MOD_ID);
        registerMessages();
        ModRegistry.touch();
    }

    @SubscribeEvent
    public static void onCommonSetup(final FMLCommonSetupEvent evt) {
        FuelManager.INSTANCE.addWoodenBlock(ModRegistry.TRADING_POST_BLOCK.get());
    }
    
    private static void registerMessages() {
        NETWORK.register(S2CMerchantDataMessage.class, S2CMerchantDataMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(S2CRemoveMerchantsMessage.class, S2CRemoveMerchantsMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(S2CBuildOffersMessage.class, S2CBuildOffersMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(C2SClearSlotsMessage.class, C2SClearSlotsMessage::new, MessageDirection.TO_SERVER);
    }
}
