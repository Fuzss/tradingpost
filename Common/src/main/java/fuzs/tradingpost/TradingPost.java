package fuzs.tradingpost;

import fuzs.puzzleslib.config.ConfigHolderV2;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.core.ModConstructor;
import fuzs.puzzleslib.network.MessageDirection;
import fuzs.puzzleslib.network.NetworkHandler;
import fuzs.tradingpost.config.ServerConfig;
import fuzs.tradingpost.network.client.message.C2SClearSlotsMessage;
import fuzs.tradingpost.network.message.S2CBuildOffersMessage;
import fuzs.tradingpost.network.message.S2CMerchantDataMessage;
import fuzs.tradingpost.network.message.S2CRemoveMerchantsMessage;
import fuzs.tradingpost.init.ModRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradingPost implements ModConstructor {
    public static final String MOD_ID = "tradingpost";
    public static final String MOD_NAME = "Trading Post";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandler NETWORK = CoreServices.FACTORIES.network(MOD_ID);
    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolderV2 CONFIG = CoreServices.FACTORIES.server(ServerConfig.class, () -> new ServerConfig());

    public void onConstructMod() {
        CONFIG.bakeConfigs(MOD_ID);
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
        context.registerWoodenBlock(ModRegistry.TRADING_POST_BLOCK.get());
    }
}
