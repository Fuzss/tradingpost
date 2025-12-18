package fuzs.tradingpost.world.inventory;

import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.config.ServerConfig;
import fuzs.tradingpost.init.ModRegistry;
import fuzs.tradingpost.world.entity.npc.LocalMerchant;
import fuzs.tradingpost.world.item.trading.MerchantCollection;
import fuzs.tradingpost.world.level.block.TradingPostBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class TradingPostMenu extends MerchantMenu {
    private final ContainerLevelAccess access;
    private final MerchantCollection traders;

    private int ticks;
    private boolean offersLocked;

    public TradingPostMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new MerchantCollection(), ContainerLevelAccess.NULL);
    }

    public TradingPostMenu(int containerId, Inventory inventory, MerchantCollection merchantCollection, ContainerLevelAccess containerLevelAccess) {
        super(containerId, inventory, merchantCollection);
        this.access = containerLevelAccess;
        this.trader = this.traders = merchantCollection;
        this.tradeContainer = new TradingPostContainer(this.traders);
        this.replaceSlot(0, new Slot(this.tradeContainer, 0, 136, 37));
        this.replaceSlot(1, new Slot(this.tradeContainer, 1, 162, 37));
        this.replaceSlot(2, new MerchantResultSlot(inventory.player, this.traders, this.tradeContainer, 2, 220, 37));
    }

    private void replaceSlot(int index, Slot slot) {
        slot.index = index;
        this.slots.set(index, slot);
    }

    @Override
    public MenuType<?> getType() {
        return ModRegistry.TRADING_POST_MENU_TYPE.value();
    }

    @Override
    public boolean stillValid(Player player) {
        // TODO this should be handled by the block entity, and synced to the menu via a data slot
        // this also updates merchants, so run independent of config option
        // don't want this to go off on every tick
        Optional<Boolean> anyTrader = this.access.evaluate((Level level, BlockPos pos) -> {
            boolean testRange = TradingPost.CONFIG.get(ServerConfig.class).enforceRange && ++this.ticks >= 20;
            return this.traders.updateAvailableMerchants((ServerPlayer) player, this.containerId, pos, testRange);
        });
        if (this.ticks >= 20) this.ticks = 0;
        if (TradingPost.CONFIG.get(ServerConfig.class).closeEmptyScreen && anyTrader.isPresent() && !anyTrader.get()) {
            player.displayClientMessage(TradingPostBlock.MISSING_MERCHANT_COMPONENT, false);
            return false;
        }
        return stillValid(this.access, player, ModRegistry.TRADING_POST_BLOCK.value());
    }

    @Override
    protected void playTradeSound() {
        if (!this.traders.isClientSide()) {
            Merchant merchant = this.traders.getCurrentMerchant();
            if (merchant instanceof Entity entity) {
                entity.level()
                        .playLocalSound(entity.getX(),
                                entity.getY(),
                                entity.getZ(),
                                this.traders.getNotifyTradeSound(),
                                SoundSource.NEUTRAL,
                                1.0F,
                                1.0F,
                                false);
            }
        }
    }

    /**
     * @see MerchantMenu#tryMoveItems(int)
     */
    public void clearPaymentSlots() {
        ItemStack itemstack = this.tradeContainer.getItem(0);
        if (!itemstack.isEmpty()) {
            if (!this.moveItemStackTo(itemstack, 3, 39, true)) {
                return;
            }

            this.tradeContainer.setItem(0, itemstack);
        }

        ItemStack itemstack1 = this.tradeContainer.getItem(1);
        if (!itemstack1.isEmpty()) {
            if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                return;
            }

            this.tradeContainer.setItem(1, itemstack1);
        }
    }

    @Override
    public MerchantOffers getOffers() {
        return this.offersLocked ? new MerchantOffers() : super.getOffers();
    }

    public MerchantCollection getTraders() {
        return this.traders;
    }

    public void setOffersLocked(boolean offersLocked) {
        this.offersLocked = offersLocked;
    }

    public void addMerchant(Player player, int merchantId, Component merchantTitle, MerchantOffers offers, int villagerLevel, int villagerXp, boolean showProgress, boolean canRestock) {
        LocalMerchant merchant = new LocalMerchant(player,
                merchantTitle,
                offers,
                villagerLevel,
                villagerXp,
                showProgress,
                canRestock);
        this.traders.addMerchant(merchantId, merchant);
    }

    @Override
    public int getTraderLevel() {
        return this.traders.getTraderLevel();
    }

    @Override
    public boolean canRestock() {
        return this.traders.canRestock();
    }

    @Override
    public boolean showProgressBar() {
        return this.traders.showProgressBar();
    }

    @Override
    public void setShowProgressBar(boolean showProgressBar) {
        // NO-OP
    }

    @Override
    public void setXp(int xpValue) {
        // NO-OP
    }

    @Override
    public void setMerchantLevel(int merchantLevel) {
        // NO-OP
    }

    @Override
    public void setCanRestock(boolean canRestock) {
        // NO-OP
    }

    @Override
    public void setOffers(MerchantOffers offers) {
        // NO-OP
    }
}
