package fuzs.tradingpost.item;

import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;

import javax.annotation.Nullable;
import java.util.Set;

public class TradingPostOffers extends MerchantOffers {

    private final Set<MerchantOffer> disabledOffers;

    public TradingPostOffers(Set<MerchantOffer> disabledOffers) {

        this.disabledOffers = disabledOffers;
    }

    @Nullable
    @Override
    public MerchantOffer getRecipeFor(ItemStack input1, ItemStack input2, int offerId) {

        if (offerId > 0 && offerId < this.size()) {

            MerchantOffer offer = this.get(offerId);
            return offer.satisfiedBy(input1, input2) && !this.disabledOffers.contains(offer) ? offer : null;
        } else {

            for (MerchantOffer offer : this) {

                if (offer.satisfiedBy(input1, input2) && !this.disabledOffers.contains(offer)) {

                    return offer;
                }
            }
        }

        return null;
    }

}
