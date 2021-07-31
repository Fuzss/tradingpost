package fuzs.tradingpost.inventory.container;

import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.block.TradingPostBlock;
import fuzs.tradingpost.element.TradingPostElement;
import fuzs.tradingpost.entity.merchant.LocalMerchant;
import fuzs.tradingpost.entity.merchant.MerchantCollection;
import fuzs.tradingpost.inventory.TradingPostInventory;
import fuzs.tradingpost.mixin.accessor.MerchantContainerAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.MerchantResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Optional;

public class TradingPostContainer extends MerchantContainer {

    private final IWorldPosCallable access;
    private final MerchantCollection traders;

    private int ticks;
    private boolean lockOffers;

    public TradingPostContainer(int containerId, PlayerInventory playerInventory) {

        super(containerId, playerInventory);

        this.access = IWorldPosCallable.NULL;
        this.traders = new MerchantCollection(this.access, playerInventory.player.level);
        ((MerchantContainerAccessor) this).setTrader(this.traders);
        this.updateTradingSlots(playerInventory, this.traders);
    }

    public TradingPostContainer(int containerId, PlayerInventory playerInventory, MerchantCollection merchantCollection, IWorldPosCallable worldPosCallable) {

        super(containerId, playerInventory, merchantCollection);

        this.access = worldPosCallable;
        this.traders = merchantCollection;
        ((MerchantContainerAccessor) this).setTrader(this.traders);
        this.updateTradingSlots(playerInventory, this.traders);
    }

    private void updateTradingSlots(PlayerInventory playerInventory, MerchantCollection merchantCollection) {

        // TODO not like this, redo all slots
        TradingPostInventory tradeContainer = new TradingPostInventory(merchantCollection);
        ((MerchantContainerAccessor) this).setTradeContainer(tradeContainer);
        Slot input1 = new Slot(tradeContainer, 0, 136, 37);
        input1.index = 0;
        this.slots.set(0, input1);
        Slot input2 = new Slot(tradeContainer, 1, 162, 37);
        input2.index = 1;
        this.slots.set(1, input2);
        MerchantResultSlot output = new MerchantResultSlot(playerInventory.player, merchantCollection, tradeContainer, 2, 220, 37);
        output.index = 2;
        this.slots.set(2, output);
    }

    @Override
    public ContainerType<?> getType() {

        return TradingPostElement.TRADING_POST_CONTAINER;
    }

    @Override
    public void setSelectionHint(int offerId) {

        super.setSelectionHint(offerId);
        this.traders.setActiveOffer(this.getOffers().get(offerId));
    }

    @Override
    public boolean stillValid(PlayerEntity player) {

        // don't want this to go off on every tick
        if (++this.ticks == 20) {

            this.ticks = 0;
            Optional<Boolean> anyTrader = this.access.evaluate((level, pos) -> this.traders.checkAvailableMerchants(this.containerId, pos, player));
            if (anyTrader.isPresent() && !anyTrader.get()) {

                player.displayClientMessage(TradingPostBlock.NO_MERCHANT_FOUND, false);
                return false;
            }
        }

        return stillValid(this.access, player, TradingPostElement.TRADING_POST_BLOCK);
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int slotIndex) {

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

            IMerchant merchant = this.traders.getCurrentMerchant();
            if (merchant instanceof Entity) {

                Entity entity = (Entity) merchant;
                this.traders.getLevel().playLocalSound(entity.getX(), entity.getY(), entity.getZ(), this.traders.getNotifyTradeSound(), SoundCategory.NEUTRAL, 1.0F, 1.0F, false);
            }
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

    public void addMerchant(PlayerEntity playerEntity, int merchantId, ITextComponent merchantTitle, MerchantOffers offers, int villagerLevel, int villagerXp, boolean showProgress, boolean canRestock) {

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
