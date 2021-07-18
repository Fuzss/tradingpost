package fuzs.tradingpost.element;

import fuzs.puzzleslib.PuzzlesLib;
import fuzs.puzzleslib.element.extension.ClientExtensibleElement;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.block.TradingPostBlock;
import fuzs.tradingpost.client.element.TradingPostExtension;
import fuzs.tradingpost.tileentity.TradingPostTileEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.registries.ObjectHolder;

public class TradingPostElement extends ClientExtensibleElement<TradingPostExtension> {

    @ObjectHolder(TradingPost.MODID + ":" + "trading_post")
    public static final Block TRADING_POST_BLOCK = null;
    @ObjectHolder(TradingPost.MODID + ":" + "trading_post")
    public static final TileEntityType<TradingPostTileEntity> TRADING_POST_TILE_ENTITY = null;
//    @ObjectHolder(TradingPost.MODID + ":" + "crafting")
//    public static final ContainerType<VisualWorkbenchContainer> CRAFTING_CONTAINER = null;

    public TradingPostElement() {

        super(element -> new TradingPostExtension((TradingPostElement) element));
    }

    @Override
    public String[] getDescription() {

        return new String[]{"Changing the in-game models to allow for better animations and subtle effects."};
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setupCommon() {

        PuzzlesLib.getRegistryManager().register("trading_post", new TradingPostBlock(AbstractBlock.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)));
        PuzzlesLib.getRegistryManager().register("trading_post", TileEntityType.Builder.of(TradingPostTileEntity::new, TRADING_POST_BLOCK).build(null));
//        PuzzlesLib.getRegistryManager().register("crafting", new ContainerType<>(VisualWorkbenchContainer::new));
    }

}
