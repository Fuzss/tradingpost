package fuzs.tradingpost.world.inventory;

import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.world.inventory.TradingPostContainer;
import fuzs.tradingpost.world.level.block.TradingPostBlock;
import fuzs.tradingpost.world.entity.npc.LocalMerchant;
import fuzs.tradingpost.world.entity.npc.MerchantCollection;
import fuzs.tradingpost.mixin.accessor.MerchantMenuAccessor;
import fuzs.tradingpost.registry.ModRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

public class TradingPostMenu extends MerchantMenu {
    private final ContainerLevelAccess access;
    private final MerchantCollection traders;
    private final MerchantContainer tradeContainer;

    private int ticks;
    private boolean lockOffers;

    public TradingPostMenu(int containerId, Inventory playerInventory) {
        super(containerId, playerInventory);
        this.access = ContainerLevelAccess.NULL;
        this.traders = new MerchantCollection(this.access, playerInventory.player.level);
        ((MerchantMenuAccessor) this).setTrader(this.traders);
        this.tradeContainer = new TradingPostContainer(this.traders);
        ((MerchantMenuAccessor) this).setTradeContainer(this.tradeContainer);
        this.replaceSlot(0, new Slot(this.tradeContainer, 0, 136, 37));
        this.replaceSlot(1, new Slot(this.tradeContainer, 1, 162, 37));
        this.replaceSlot(2, new MerchantResultSlot(playerInventory.player, this.traders, this.tradeContainer, 2, 220, 37));
    }

    public TradingPostMenu(int containerId, Inventory playerInventory, MerchantCollection merchantCollection, ContainerLevelAccess worldPosCallable) {
        super(containerId, playerInventory, merchantCollection);
        this.access = worldPosCallable;
        this.traders = merchantCollection;
        ((MerchantMenuAccessor) this).setTrader(this.traders);
        this.tradeContainer = new TradingPostContainer(this.traders);
        ((MerchantMenuAccessor) this).setTradeContainer(this.tradeContainer);
        this.replaceSlot(0, new Slot(this.tradeContainer, 0, 136, 37));
        this.replaceSlot(1, new Slot(this.tradeContainer, 1, 162, 37));
        this.replaceSlot(2, new MerchantResultSlot(playerInventory.player, this.traders, this.tradeContainer, 2, 220, 37));
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
        Optional<Boolean> anyTrader = this.access.evaluate((level, pos) -> this.traders.updateAvailableMerchants(this.containerId, pos, player, TradingPost.CONFIG.server().enforceRange && ++this.ticks >= 20));
        if (this.ticks >= 20) this.ticks = 0;
        if (TradingPost.CONFIG.server().closeScreen && anyTrader.isPresent() && !anyTrader.get()) {
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
        if (!this.traders.getLevel().isClientSide) {
            Merchant merchant = this.traders.getCurrentMerchant();
            if (merchant instanceof Entity entity) {
                this.traders.getLevel().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.traders.getNotifyTradeSound(), SoundSource.NEUTRAL, 1.0F, 1.0F, false);
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

    @OnlyIn(Dist.CLIENT)
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean canRestock() {

        return this.traders.canRestock();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean showProgressBar() {

        return this.traders.showProgressBar();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setShowProgressBar(boolean showProgressBar) {
        // we don't raise an exception anywhere here as you never know what other mods are up to since this extends vanilla's merchant menu
        TradingPost.LOGGER.error("Set showProgressBar to merchants directly");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setXp(int xpValue) {

        TradingPost.LOGGER.error("Set xp to merchants directly");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setMerchantLevel(int merchantLevel) {

        TradingPost.LOGGER.error("Set level to merchants directly");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setCanRestock(boolean canRestock) {

        TradingPost.LOGGER.error("Set canRestock to merchants directly");
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void setOffers(MerchantOffers offers) {

        TradingPost.LOGGER.error("Set offers to merchants directly");
    }

}
