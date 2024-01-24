package fuzs.tradingpost.data;

import fuzs.puzzleslib.api.data.v2.AbstractLootProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.tradingpost.init.ModRegistry;

public class ModBlockLootProvider extends AbstractLootProvider.Blocks {
    public ModBlockLootProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addLootTables() {
        this.add(ModRegistry.TRADING_POST_BLOCK.value(), this::createNameableBlockEntityTable);
    }
}
