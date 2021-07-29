package fuzs.tradingpost.entity.merchant;

import com.google.common.collect.Sets;
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

    private final PlayerEntity source;
    private final Set<IMerchant> merchants = Sets.newHashSet();
    private MerchantOffers offers;
    private int xp;

    public MerchantCollection(PlayerEntity player) {

        this.source = player;
    }
    
    public void addMerchant(IMerchant merchant) {
        
        this.merchants.add(merchant);
    }

    @Override
    @Nullable
    public PlayerEntity getTradingPlayer() {

        return this.source;
    }

    @Override
    public void setTradingPlayer(@Nullable PlayerEntity player) {

        this.merchants.forEach(merchant -> merchant.setTradingPlayer(player));
    }

    @Override
    public MerchantOffers getOffers() {

        if (this.offers == null) {

            this.offers = new MerchantOffers();
            this.merchants.forEach(merchant -> this.offers.addAll(merchant.getOffers()));
        }

        return this.offers;
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
        
    }

    @Override
    public World getLevel() {
        
        return this.source.level;
    }

    @Override
    public int getVillagerXp() {
        
        return this.xp;
    }

    @Override
    public void overrideXp(int newXpValue) {
        
        this.xp = newXpValue;
    }

    @Override
    public boolean showProgressBar() {
        
        return true;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        
        return SoundEvents.VILLAGER_YES;
    }
    
}
