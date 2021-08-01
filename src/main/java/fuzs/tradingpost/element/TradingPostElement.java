package fuzs.tradingpost.element;

import com.google.common.collect.Lists;
import fuzs.puzzleslib.PuzzlesLib;
import fuzs.puzzleslib.config.ConfigManager;
import fuzs.puzzleslib.config.option.OptionsBuilder;
import fuzs.puzzleslib.config.serialization.EntryCollectionBuilder;
import fuzs.puzzleslib.element.extension.ClientExtensibleElement;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.block.TradingPostBlock;
import fuzs.tradingpost.client.element.TradingPostExtension;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import fuzs.tradingpost.network.message.SBuildOffersMessage;
import fuzs.tradingpost.network.message.SMerchantDataMessage;
import fuzs.tradingpost.network.message.SRemoveMerchantsMessage;
import fuzs.tradingpost.tileentity.TradingPostTileEntity;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

import java.util.Set;

public class TradingPostElement extends ClientExtensibleElement<TradingPostExtension> {

    @ObjectHolder(TradingPost.MODID + ":" + "trading_post")
    public static final Block TRADING_POST_BLOCK = null;
    @ObjectHolder(TradingPost.MODID + ":" + "trading_post")
    public static final Item TRADING_POST_ITEM = null;
    @ObjectHolder(TradingPost.MODID + ":" + "trading_post")
    public static final TileEntityType<TradingPostTileEntity> TRADING_POST_TILE_ENTITY = null;
    @ObjectHolder(TradingPost.MODID + ":" + "trading_post")
    public static final ContainerType<TradingPostContainer> TRADING_POST_CONTAINER = null;

    public static final Tags.IOptionalNamedTag<EntityType<?>> BLACKLISTED_TRADERS_TAG = EntityTypeTags.createOptional(new ResourceLocation(TradingPost.MODID, "blacklisted_traders"));

    public int horizontalRange;
    public int verticalRange;
    public boolean teleportXp;
    public boolean closeScreen;
    public Set<EntityType<?>> traderBlacklist;

    public TradingPostElement() {

        super(element -> new TradingPostExtension((TradingPostElement) element));
    }

    @Override
    public String[] getDescription() {

        return new String[]{"A unique utility for the true bargain hunter looking for the best possible deal."};
    }

    @Override
    protected boolean isPersistent() {

        return true;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void constructCommon() {

        PuzzlesLib.getNetworkHandler().registerMessage(SMerchantDataMessage::new, LogicalSide.CLIENT);
        PuzzlesLib.getNetworkHandler().registerMessage(SRemoveMerchantsMessage::new, LogicalSide.CLIENT);
        PuzzlesLib.getNetworkHandler().registerMessage(SBuildOffersMessage::new, LogicalSide.CLIENT);

        PuzzlesLib.getRegistryManager().registerBlockWithItem("trading_post", () -> new TradingPostBlock(AbstractBlock.Properties.of(Material.WOOD).strength(2.5F).sound(SoundType.WOOD)), ItemGroup.TAB_DECORATIONS);
        PuzzlesLib.getRegistryManager().registerTileEntityType("trading_post", () -> TileEntityType.Builder.of(TradingPostTileEntity::new, TRADING_POST_BLOCK).build(null));
        PuzzlesLib.getRegistryManager().registerContainerType("trading_post", () -> new ContainerType<>(TradingPostContainer::new));
    }

    @Override
    public void setupCommon2() {

        PuzzlesLib.getFuelManager().addWoodenBlock(TRADING_POST_BLOCK);
    }

    @Override
    public void setupCommonConfig(OptionsBuilder builder) {

        builder.define("Horizontal Range", 8).range(1, 64).comment("Range on xz plane trading post should search for merchants.").sync(v -> this.horizontalRange = v);
        builder.define("Vertical Range", 5).range(1, 64).comment("Range on y axis trading post should search for merchants.").sync(v -> this.verticalRange = v);
        builder.define("Teleport Xp", true).comment("Teleport xp from trading from villagers on top of the trading post.").sync(v -> this.teleportXp = v);
        builder.define("Close Empty Screen", true).comment("Close trading post interface when all traders have become unavailable.").sync(v -> this.closeScreen = v);
        builder.define("Trader Blacklist", Lists.<String>newArrayList()).comment("Trader entities disabled from being found by the trading post.", "Modders may add their own incompatible trader entities via the \"" + TradingPost.MODID + ":blacklisted_traders\" entity tag.", EntryCollectionBuilder.CONFIG_STRING).sync(v -> this.traderBlacklist = ConfigManager.deserializeToSet(v, ForgeRegistries.ENTITIES));
    }

}
