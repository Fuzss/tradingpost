package fuzs.tradingpost.entity.merchant;

import fuzs.puzzleslib.PuzzlesLib;
import fuzs.tradingpost.block.TradingPostBlock;
import fuzs.tradingpost.network.message.SMerchantDataMessage;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.villager.IVillagerDataHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Map;

public class MerchantCollection implements IMerchant {

    private final PlayerEntity player;
    private final Int2ObjectOpenHashMap<IMerchant> idToMerchant = new Int2ObjectOpenHashMap<>();

    private MerchantOffers allOffers;
    private int activeMerchantId = -1;

    public MerchantCollection(PlayerEntity player) {

        this.player = player;
    }
    
    public void addMerchant(int entityId, IMerchant merchant) {

        if (!merchant.getOffers().isEmpty()) {

            this.idToMerchant.put(entityId, merchant);
        }
    }

    @Override
    @Nullable
    public PlayerEntity getTradingPlayer() {

        return this.player;
    }

    @Override
    public void setTradingPlayer(@Nullable PlayerEntity player) {

        this.idToMerchant.values().forEach(merchant -> merchant.setTradingPlayer(player));
    }

    @Override
    public MerchantOffers getOffers() {

        if (this.allOffers == null) {

            this.allOffers = new MerchantOffers();
            // TODO create list with known order so we can retrieve merchant from offer later
//            this.merchants.forEach(merchant -> this.allOffers.addAll(merchant.getOffers()));
        }

        return this.allOffers;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void overrideOffers(@Nullable MerchantOffers p_213703_1_) {

        // this is vanilla, we do this differently
        throw new UnsupportedOperationException("Set offers to merchants directly");
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {

        offer.increaseUses();
    }

    @Override
    public void notifyTradeUpdated(ItemStack p_110297_1_) {

        // plays sound, only called on server
    }

    @Override
    public World getLevel() {

        // TODO replace this with worldposcallable in container
        return this.player.level;
    }

    @Override
    public int getVillagerXp() {

        if (this.activeMerchantId != -1) {

            return this.idToMerchant.get(this.activeMerchantId).getVillagerXp();
        }

        return 0;
    }

    @Override
    public void overrideXp(int xpValue) {
        
        if (this.activeMerchantId != -1) {

            this.idToMerchant.get(this.activeMerchantId).overrideXp(xpValue);
        }
    }

    @Override
    public boolean showProgressBar() {

        if (this.activeMerchantId != -1) {

            return this.idToMerchant.get(this.activeMerchantId).showProgressBar();
        }

        return true;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {

        // unused by client, just a dummy
        return SoundEvents.VILLAGER_YES;
    }

    public void disableMerchant(int merchantId) {

        // TODO
    }

    public void setActiveOffer(int offerId) {

        // TODO
    }

    public void sendMerchantData(final int containerId) {

        for (Map.Entry<Integer, IMerchant> entry : this.idToMerchant.int2ObjectEntrySet()) {

            IMerchant merchant = entry.getValue();
            final ITextComponent merchantTitle = merchant instanceof Entity ? ((Entity) merchant).getDisplayName() : TradingPostBlock.CONTAINER_TITLE;
            final int merchantLevel = merchant instanceof IVillagerDataHolder ? ((IVillagerDataHolder) merchant).getVillagerData().getLevel() : 0;

            SMerchantDataMessage message = new SMerchantDataMessage(containerId, entry.getKey(), merchantTitle, this.getOffers(), merchantLevel, this.getVillagerXp(), this.showProgressBar(), this.canRestock());
            PuzzlesLib.getNetworkHandler().sendTo(message, (ServerPlayerEntity) this.player);
        }
    }
    
}
