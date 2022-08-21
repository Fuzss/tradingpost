package fuzs.tradingpost;

import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.core.CoreServices;
import fuzs.puzzleslib.core.ModConstructor;
import fuzs.puzzleslib.network.MessageDirection;
import fuzs.puzzleslib.network.NetworkHandler;
import fuzs.tradingpost.config.ServerConfig;
import fuzs.tradingpost.init.ModRegistry;
import fuzs.tradingpost.network.client.C2SClearSlotsMessage;
import fuzs.tradingpost.network.S2CBuildOffersMessage;
import fuzs.tradingpost.network.S2CMerchantDataMessage;
import fuzs.tradingpost.network.S2CRemoveMerchantsMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradingPost implements ModConstructor {
    public static final String MOD_ID = "tradingpost";
    public static final String MOD_NAME = "Trading Post";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandler NETWORK = CoreServices.FACTORIES.network(MOD_ID);
    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder CONFIG = CoreServices.FACTORIES.serverConfig(ServerConfig.class, () -> new ServerConfig());

    @Override
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
