package fuzs.tradingpost.init;

import fuzs.puzzleslib.api.init.v3.registry.RegistryManager;
import fuzs.puzzleslib.api.init.v3.tags.TagFactory;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import fuzs.tradingpost.world.level.block.TradingPostBlock;
import fuzs.tradingpost.world.level.block.entity.TradingPostBlockEntity;
import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class ModRegistry {
    static final RegistryManager REGISTRIES = RegistryManager.from(TradingPost.MOD_ID);
    public static final Holder.Reference<Block> TRADING_POST_BLOCK = REGISTRIES.registerBlock("trading_post",
            TradingPostBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD)
                    .instrument(NoteBlockInstrument.BASS)
                    .strength(2.5F)
                    .sound(SoundType.WOOD)
                    .ignitedByLava());
    public static final Holder.Reference<Item> TRADING_POST_ITEM = REGISTRIES.registerBlockItem(TRADING_POST_BLOCK);
    public static final Holder.Reference<BlockEntityType<TradingPostBlockEntity>> TRADING_POST_BLOCK_ENTITY_TYPE = REGISTRIES.registerBlockEntityType(
            "trading_post",
            TradingPostBlockEntity::new,
            TRADING_POST_BLOCK);
    public static final Holder.Reference<MenuType<TradingPostMenu>> TRADING_POST_MENU_TYPE = REGISTRIES.registerMenuType(
            "trading_post",
            TradingPostMenu::new);

    static final TagFactory TAGS = TagFactory.make(TradingPost.MOD_ID);
    public static final TagKey<EntityType<?>> EXCLUDE_FROM_TRADING_POST_ENTITY_TYPE_TAG = TAGS.registerEntityTypeTag(
            "exclude_from_trading_post");

    public static void bootstrap() {
        // NO-OP
    }
}
