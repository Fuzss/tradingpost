package fuzs.tradingpost.world.item.trading;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public class TradingPostOffers extends MerchantOffers {
    private final Set<MerchantOffer> disabledOffers;
    private int[] indexToShopItem;

    public TradingPostOffers(Set<MerchantOffer> disabledOffers) {
        this.disabledOffers = disabledOffers;
    }

    @Nullable
    @Override
    public MerchantOffer getRecipeFor(ItemStack input1, ItemStack input2, int offerId) {
        if (offerId >= 0 && offerId < this.size()) {
            MerchantOffer offer = this.get(offerId);
            return offer.satisfiedBy(input1, input2) && !this.disabledOffers.contains(offer) ? offer : null;
        }
        return null;
    }

    @Override
    public int size() {
        return this.indexToShopItem != null ? this.indexToShopItem.length : super.size();
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public MerchantOffer get(int index) {
        return super.get(this.indexToShopItem != null ? this.indexToShopItem[index] : index);
    }

    public void setFilter(Collection<MerchantOffer> activeOffers) {
        this.indexToShopItem = activeOffers.stream()
                .filter(this::contains)
                .mapToInt(this::indexOf)
                .toArray();
    }

    public void clearFilter() {
        this.indexToShopItem = null;
    }

    public int getOrigShopItem(int filteredShopItem) {
        return this.indexToShopItem != null && filteredShopItem < this.indexToShopItem.length ? this.indexToShopItem[filteredShopItem] : filteredShopItem;
    }
}
