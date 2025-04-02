package fuzs.tradingpost;

import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.context.GameplayContentContext;
import fuzs.puzzleslib.api.core.v1.context.PayloadTypesContext;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.api.event.v1.BuildCreativeModeTabContentsCallback;
import fuzs.tradingpost.config.ServerConfig;
import fuzs.tradingpost.init.ModRegistry;
import fuzs.tradingpost.network.ClientboundBuildOffersMessage;
import fuzs.tradingpost.network.ClientboundMerchantDataMessage;
import fuzs.tradingpost.network.ClientboundRemoveMerchantsMessage;
import fuzs.tradingpost.network.client.ServerboundClearSlotsMessage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.apache.commons.lang3.math.Fraction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TradingPost implements ModConstructor {
    public static final String MOD_ID = "tradingpost";
    public static final String MOD_NAME = "Trading Post";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);

    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID).server(ServerConfig.class);

    @Override
    public void onConstructMod() {
        ModRegistry.bootstrap();
        registerLoadingHandlers();
    }

    private static void registerLoadingHandlers() {
        BuildCreativeModeTabContentsCallback.buildCreativeModeTabContents(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register((CreativeModeTab creativeModeTab, CreativeModeTab.ItemDisplayParameters itemDisplayParameters, CreativeModeTab.Output output) -> {
                    output.accept(ModRegistry.TRADING_POST_ITEM.value());
                });
    }

    @Override
    public void onRegisterPayloadTypes(PayloadTypesContext context) {
        context.playToClient(ClientboundMerchantDataMessage.class, ClientboundMerchantDataMessage.STREAM_CODEC);
        context.playToClient(ClientboundRemoveMerchantsMessage.class, ClientboundRemoveMerchantsMessage.STREAM_CODEC);
        context.playToClient(ClientboundBuildOffersMessage.class, ClientboundBuildOffersMessage.STREAM_CODEC);
        context.playToServer(ServerboundClearSlotsMessage.class, ServerboundClearSlotsMessage.STREAM_CODEC);
    }

    @Override
    public void onRegisterGameplayContent(GameplayContentContext context) {
        context.registerFuel(ModRegistry.TRADING_POST_BLOCK, Fraction.getFraction(3, 2));
    }

    public static ResourceLocation id(String path) {
        return ResourceLocationHelper.fromNamespaceAndPath(MOD_ID, path);
    }
}
