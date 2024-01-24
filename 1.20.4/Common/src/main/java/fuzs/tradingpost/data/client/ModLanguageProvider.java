package fuzs.tradingpost.data.client;

import fuzs.puzzleslib.api.client.data.v2.AbstractLanguageProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.tradingpost.client.gui.screens.inventory.TradingPostScreen;
import fuzs.tradingpost.init.ModRegistry;
import fuzs.tradingpost.world.level.block.TradingPostBlock;
import fuzs.tradingpost.world.level.block.entity.TradingPostBlockEntity;

public class ModLanguageProvider extends AbstractLanguageProvider {

    public ModLanguageProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addTranslations(TranslationBuilder builder) {
        builder.add(ModRegistry.TRADING_POST_BLOCK.value(), "Trading Post");
        builder.add(TradingPostBlockEntity.CONTAINER_COMPONENT, "Trading Post");
        builder.add(TradingPostScreen.MERCHANT_UNAVAILABLE_COMPONENT, "The trader is no longer available.");
        builder.add(TradingPostBlock.MISSING_MERCHANT_COMPONENT, "Couldn't find any available trader nearby");
    }
}
