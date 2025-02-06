package fuzs.tradingpost;

import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.api.event.v1.BuildCreativeModeTabContentsCallback;
import fuzs.puzzleslib.api.event.v1.server.RegisterFuelValuesCallback;
import fuzs.puzzleslib.api.network.v3.NetworkHandler;
import fuzs.tradingpost.config.ServerConfig;
import fuzs.tradingpost.init.ModRegistry;
import fuzs.tradingpost.network.S2CBuildOffersMessage;
import fuzs.tradingpost.network.S2CMerchantDataMessage;
import fuzs.tradingpost.network.S2CRemoveMerchantsMessage;
import fuzs.tradingpost.network.client.C2SClearSlotsMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradingPost implements ModConstructor {
    public static final String MOD_ID = "tradingpost";
    public static final String MOD_NAME = "Trading Post";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final NetworkHandler NETWORK = NetworkHandler.builder(MOD_ID)
            .registerLegacyClientbound(S2CMerchantDataMessage.class, S2CMerchantDataMessage::new)
            .registerLegacyClientbound(S2CRemoveMerchantsMessage.class, S2CRemoveMerchantsMessage::new)
            .registerLegacyClientbound(S2CBuildOffersMessage.class, S2CBuildOffersMessage::new)
            .registerLegacyServerbound(C2SClearSlotsMessage.class, C2SClearSlotsMessage::new);
    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID).server(ServerConfig.class);

    @Override
    public void onConstructMod() {
        ModRegistry.bootstrap();
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        RegisterFuelValuesCallback.EVENT.register((builder, fuelBaseValue) -> {
            builder.add(ModRegistry.TRADING_POST_BLOCK.value(), fuelBaseValue * 3 / 2);
        });
        BuildCreativeModeTabContentsCallback.buildCreativeModeTabContents(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register((CreativeModeTab creativeModeTab, CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) -> {
                    output.accept(ModRegistry.TRADING_POST_ITEM.value());
                });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocationHelper.fromNamespaceAndPath(MOD_ID, path);
    }
}
