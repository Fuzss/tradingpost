package fuzs.tradingpost.element;

import fuzs.puzzleslib.PuzzlesLib;
import fuzs.puzzleslib.element.extension.ClientExtensibleElement;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.block.TradingPostBlock;
import fuzs.tradingpost.client.element.TradingPostExtension;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import fuzs.tradingpost.network.message.SMerchantDataMessage;
import fuzs.tradingpost.network.message.SRemoveMerchantMessage;
import fuzs.tradingpost.tileentity.TradingPostTileEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ObjectHolder;

public class TradingPostElement extends ClientExtensibleElement<TradingPostExtension> {

    @ObjectHolder(TradingPost.MODID + ":" + "trading_post")
    public static final Block TRADING_POST_BLOCK = null;
    @ObjectHolder(TradingPost.MODID + ":" + "trading_post")
    public static final TileEntityType<TradingPostTileEntity> TRADING_POST_TILE_ENTITY = null;
    @ObjectHolder(TradingPost.MODID + ":" + "trading_post")
    public static final ContainerType<TradingPostContainer> TRADING_POST_CONTAINER = null;

    public TradingPostElement() {

        super(element -> new TradingPostExtension((TradingPostElement) element));
    }

    @Override
    public String[] getDescription() {

        // TODO proper description
        return new String[]{"Changing the in-game models to allow for better animations and subtle effects."};
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void setupCommon() {

        PuzzlesLib.getNetworkHandler().registerMessage(SMerchantDataMessage::new, LogicalSide.CLIENT);
        PuzzlesLib.getNetworkHandler().registerMessage(SRemoveMerchantMessage::new, LogicalSide.CLIENT);

        TradingPostBlock tradingPostBlock = new TradingPostBlock(AbstractBlock.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD));
        PuzzlesLib.getRegistryManager().register("trading_post", tradingPostBlock);
        PuzzlesLib.getRegistryManager().register("trading_post", registerBlock(tradingPostBlock, ItemGroup.TAB_DECORATIONS));
        PuzzlesLib.getRegistryManager().register("trading_post", TileEntityType.Builder.of(TradingPostTileEntity::new, tradingPostBlock).build(null));
        PuzzlesLib.getRegistryManager().register("trading_post", new ContainerType<>(TradingPostContainer::new));
    }

     // TODO clean this up
    private static Item registerBlock(Block p_221542_0_, ItemGroup p_221542_1_) {
        return registerBlock(new BlockItem(p_221542_0_, (new Item.Properties()).tab(p_221542_1_)));
    }

    private static Item registerBlock(BlockItem p_221543_0_) {
        return registerBlock(p_221543_0_.getBlock(), p_221543_0_);
    }

    protected static Item registerBlock(Block p_221546_0_, Item p_221546_1_) {
        return registerItem(Registry.BLOCK.getKey(p_221546_0_), p_221546_1_);
    }

    private static Item registerItem(String p_221547_0_, Item p_221547_1_) {
        return registerItem(new ResourceLocation(p_221547_0_), p_221547_1_);
    }

    private static Item registerItem(ResourceLocation p_221544_0_, Item p_221544_1_) {
        if (p_221544_1_ instanceof BlockItem) {
            ((BlockItem)p_221544_1_).registerBlocks(Item.BY_BLOCK, p_221544_1_);
        }

        return p_221544_1_;
    }

}
