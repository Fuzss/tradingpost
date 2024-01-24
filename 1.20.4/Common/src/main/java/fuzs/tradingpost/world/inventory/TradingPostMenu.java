package fuzs.tradingpost.world.inventory;

import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.config.ServerConfig;
import fuzs.tradingpost.init.ModRegistry;
import fuzs.tradingpost.mixin.accessor.MerchantMenuAccessor;
import fuzs.tradingpost.world.entity.npc.LocalMerchant;
import fuzs.tradingpost.world.entity.npc.MerchantCollection;
import fuzs.tradingpost.world.level.block.TradingPostBlock;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.Optional;

public class TradingPostMenu extends MerchantMenu {
    private final ContainerLevelAccess access;
    private final MerchantCollection traders;
    private final MerchantContainer tradeContainer;

    private int ticks;
    private boolean lockOffers;

    public TradingPostMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new MerchantCollection(), ContainerLevelAccess.NULL);
    }

    public TradingPostMenu(int containerId, Inventory inventory, MerchantCollection merchantCollection, ContainerLevelAccess worldPosCallable) {
        super(containerId, inventory, merchantCollection);
        this.access = worldPosCallable;
        this.traders = merchantCollection;
        ((MerchantMenuAccessor) this).setTrader(this.traders);
        this.tradeContainer = new TradingPostContainer(this.traders);
        ((MerchantMenuAccessor) this).setTradeContainer(this.tradeContainer);
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
        return ModRegistry.TRADING_POST_MENU_TYPE.get();
    }

    @Override
    public boolean stillValid(Player player) {
        // this also updates merchants, so run independent of config option
        // don't want this to go off on every tick
        Optional<Boolean> anyTrader = this.access.evaluate((level, pos) -> this.traders.updateAvailableMerchants(this.containerId, pos, player, TradingPost.CONFIG.get(ServerConfig.class).enforceRange && ++this.ticks >= 20));
        if (this.ticks >= 20) this.ticks = 0;
        if (TradingPost.CONFIG.get(ServerConfig.class).closeScreen && anyTrader.isPresent() && !anyTrader.get()) {
            player.displayClientMessage(TradingPostBlock.NO_MERCHANT_FOUND, false);
            return false;
        }
        return stillValid(this.access, player, ModRegistry.TRADING_POST_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);
        if (slot != null && slot.hasItem()) {

            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (slotIndex == 2) {

                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {

                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
                // only replace this, vanilla will throw ClassCastException due to MerchantCollection not being an entity
                this.playTradeSound();
            } else {

                return super.quickMoveStack(player, slotIndex);
            }

            if (itemstack1.isEmpty()) {

                slot.set(ItemStack.EMPTY);
            } else {

                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {

                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    private void playTradeSound() {
        if (!this.traders.isClientSide()) {
            Merchant merchant = this.traders.getCurrentMerchant();
            if (merchant instanceof Entity entity) {
                entity.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.traders.getNotifyTradeSound(), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
            }
        }
    }

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
        return this.lockOffers ? new MerchantOffers() : super.getOffers();
    }

    public MerchantCollection getTraders() {
        return this.traders;
    }

    public void lockOffers(boolean lock) {
        this.lockOffers = lock;
    }

    public void addMerchant(Player playerEntity, int merchantId, Component merchantTitle, MerchantOffers offers, int villagerLevel, int villagerXp, boolean showProgress, boolean canRestock) {

        LocalMerchant merchant = new LocalMerchant(playerEntity, merchantTitle, offers, villagerLevel, villagerXp, showProgress, canRestock);
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
        // we don't raise an exception anywhere here as you never know what other mods are up to since this extends vanilla's merchant menu
        TradingPost.LOGGER.error("Operation setShowProgressBar no supported on trading post, set showProgressBar to merchants directly");
    }

    @Override
    public void setXp(int xpValue) {
        TradingPost.LOGGER.error("Operation setXp no supported on trading post, set xp to merchants directly");
    }

    @Override
    public void setMerchantLevel(int merchantLevel) {
        TradingPost.LOGGER.error("Operation setMerchantLevel no supported on trading post, set level to merchants directly");
    }

    @Override
    public void setCanRestock(boolean canRestock) {
        TradingPost.LOGGER.error("Operation setCanRestock no supported on trading post, set canRestock to merchants directly");
    }

    @Override
    public void setOffers(MerchantOffers offers) {
        TradingPost.LOGGER.error("Operation setOffers no supported on trading post, set offers to merchants directly");
    }
}
