package fuzs.tradingpost.entity.merchant;

import com.google.common.collect.Sets;
import fuzs.puzzleslib.PuzzlesLib;
import fuzs.tradingpost.network.message.SMerchantDataMessage;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Set;

public class MerchantCollection implements IMerchant {

    private final PlayerEntity player;
    private final Int2ObjectOpenHashMap<IMerchant> idToMerchant = new Int2ObjectOpenHashMap<>();

    private MerchantOffers allOffers;
    private int activeMerchantId = -1;

    public MerchantCollection(PlayerEntity player) {

        this.player = player;
    }
    
    public void addMerchant(int entityId, IMerchant merchant) {
        
        this.idToMerchant.put(entityId, merchant);
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

        // unused by client, jsut a dummy
        return SoundEvents.VILLAGER_YES;
    }

    public void disableMerchant(int merchantId) {

        // TODO
    }

    public void setActiveOffer(int offerId) {

        // TODO
    }

    public void sendMerchantData(final int containerId) {

        for (IMerchant merchant : this.merchants) {

            if (!merchant.getOffers().isEmpty()) {

                new SMerchantDataMessage()
                PuzzlesLib.getNetworkHandler().sendTo(this.player);
            }
        }
    }
    
}
