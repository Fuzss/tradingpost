package fuzs.tradingpost;

import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.context.FuelBurnTimesContext;
import fuzs.puzzleslib.api.network.v2.MessageDirection;
import fuzs.puzzleslib.api.network.v2.NetworkHandlerV2;
import fuzs.tradingpost.config.ServerConfig;
import fuzs.tradingpost.init.ModRegistry;
import fuzs.tradingpost.network.S2CBuildOffersMessage;
import fuzs.tradingpost.network.S2CMerchantDataMessage;
import fuzs.tradingpost.network.S2CRemoveMerchantsMessage;
import fuzs.tradingpost.network.client.C2SClearSlotsMessage;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradingPost implements ModConstructor {
    public static final String MOD_ID = "tradingpost";
    public static final String MOD_NAME = "Trading Post";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandlerV2 NETWORK = NetworkHandlerV2.build(MOD_ID);
    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID).server(ServerConfig.class);

    @Override
    public void onConstructMod() {
        ModRegistry.touch();
        registerMessages();
    }

    private static void registerMessages() {
        NETWORK.register(S2CMerchantDataMessage.class, S2CMerchantDataMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(S2CRemoveMerchantsMessage.class, S2CRemoveMerchantsMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(S2CBuildOffersMessage.class, S2CBuildOffersMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(C2SClearSlotsMessage.class, C2SClearSlotsMessage::new, MessageDirection.TO_SERVER);
    }

    @Override
    public void onRegisterFuelBurnTimes(FuelBurnTimesContext context) {
        context.registerFuel(300, ModRegistry.TRADING_POST_BLOCK.get());
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
