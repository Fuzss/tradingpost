package fuzs.tradingpost.inventory.container;

import fuzs.tradingpost.element.TradingPostElement;
import fuzs.tradingpost.entity.merchant.LocalMerchant;
import fuzs.tradingpost.entity.merchant.MerchantCollection;
import fuzs.tradingpost.mixin.accessor.MerchantContainerAccessor;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.MerchantInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.inventory.container.MerchantResultSlot;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TradingPostContainer extends MerchantContainer {

    private final MerchantCollection traders;

    public TradingPostContainer(int containerId, PlayerInventory playerInventory) {

        super(containerId, playerInventory);
        this.traders = new MerchantCollection(playerInventory.player);
        ((MerchantContainerAccessor) this).setTrader(this.traders);
        this.updateTradingSlots(playerInventory, this.traders);
    }

    public TradingPostContainer(int containerId, PlayerInventory playerInventory, MerchantCollection merchantCollection) {

        super(containerId, playerInventory, merchantCollection);
        this.traders = merchantCollection;
        ((MerchantContainerAccessor) this).setTrader(this.traders);
        this.updateTradingSlots(playerInventory, this.traders);
    }

    private void updateTradingSlots(PlayerInventory playerInventory, MerchantCollection merchantCollection) {

        MerchantInventory tradeContainer = new MerchantInventory(merchantCollection);
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
        this.traders.setActiveOffer(offerId);
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

    public ITextComponent getContainerTitle() {

        return this.traders.getDisplayName();
    }

    @Override
    public int getTraderLevel() {

        return this.traders.getTraderLevel();
    }

    @OnlyIn(Dist.CLIENT)
    public void setShowProgressBar(boolean showProgressBar) {

        throw new UnsupportedOperationException("Set showProgressBar to merchants directly");
    }

    @OnlyIn(Dist.CLIENT)
    public void setXp(int xpValue) {

        throw new UnsupportedOperationException("Set xp to merchants directly");
    }

    @OnlyIn(Dist.CLIENT)
    public void setMerchantLevel(int merchantLevel) {

        throw new UnsupportedOperationException("Set level to merchants directly");
    }

    @OnlyIn(Dist.CLIENT)
    public void setCanRestock(boolean canRestock) {

        throw new UnsupportedOperationException("Set canRestock to merchants directly");
    }

    @OnlyIn(Dist.CLIENT)
    public void setOffers(MerchantOffers offers) {

        throw new UnsupportedOperationException("Set offers to merchants directly");
    }

    public void addMerchant(PlayerEntity playerEntity, int merchantId, ITextComponent merchantTitle, MerchantOffers offers, int villagerLevel, int villagerXp, boolean showProgress, boolean canRestock) {

        LocalMerchant merchant = new LocalMerchant(playerEntity, merchantTitle, offers, villagerLevel, villagerXp, showProgress, canRestock);
        this.traders.addMerchant(merchantId, merchant);
    }

    public void removeMerchant(int merchantId) {

        this.traders.disableMerchant(merchantId);
    }

    public void setOffers(Int2IntOpenHashMap idToOfferCount) {

        this.traders.buildOffers(idToOfferCount);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean canRestock() {

        return this.traders.canRestock();
    }

//    @Override
//    public MerchantOffers getOffers() {
//
//        return this.traders.getOffers();
//    }
//
//    @Override
//    public int getTraderXp() {
//
//        return this.traders.getVillagerXp();
//    }
//
//    @Override
//    public boolean stillValid(PlayerEntity player) {
//
//        // TODO player is fixed in merchant collection, do something else
//        return this.traders.getTradingPlayer() == player;
//    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean showProgressBar() {

        return this.traders.showProgressBar();
    }

}
