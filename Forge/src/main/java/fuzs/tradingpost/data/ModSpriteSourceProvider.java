package fuzs.tradingpost.data;

import fuzs.puzzleslib.api.data.v1.AbstractSpriteSourceProvider;
import fuzs.tradingpost.client.gui.screens.inventory.TradingPostScreen;
import net.minecraft.client.renderer.texture.atlas.sources.SingleFile;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.data.SpriteSourceProvider;

import java.util.Optional;

public class ModSpriteSourceProvider extends AbstractSpriteSourceProvider {

    public ModSpriteSourceProvider(PackOutput packOutput, String modId, ExistingFileHelper fileHelper) {
        super(packOutput, modId, fileHelper);
    }

    @Override
    protected void addSources() {
        this.atlas(SpriteSourceProvider.BLOCKS_ATLAS).addSource(new SingleFile(TradingPostScreen.MAGNIFYING_GLASS_LOCATION, Optional.empty()));
    }
}
