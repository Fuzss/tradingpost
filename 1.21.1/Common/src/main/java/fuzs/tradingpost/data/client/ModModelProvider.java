package fuzs.tradingpost.data.client;

import fuzs.puzzleslib.api.client.data.v2.AbstractModelProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.tradingpost.init.ModRegistry;
import net.minecraft.data.models.BlockModelGenerators;

public class ModModelProvider extends AbstractModelProvider {

    public ModModelProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addBlockModels(BlockModelGenerators builder) {
        builder.createNonTemplateModelBlock(ModRegistry.TRADING_POST_BLOCK.value());
    }
}
