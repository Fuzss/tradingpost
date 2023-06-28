package fuzs.tradingpost.world.entity.npc;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.trading.MerchantOffers;

public class LocalMerchant extends ClientSideMerchant {
    private final Component merchantTitle;
    private final int merchantLevel;
    private final boolean showProgressBar;
    private final boolean canRestock;
    
    public LocalMerchant(Player playerEntity, Component merchantTitle, MerchantOffers offers, int villagerLevel, int villagerXp, boolean showProgress, boolean canRestock) {
        super(playerEntity);
        this.merchantTitle = merchantTitle;
        this.overrideOffers(offers);
        this.merchantLevel = villagerLevel;
        this.overrideXp(villagerXp);
        this.showProgressBar = showProgress;
        this.canRestock = canRestock;
    }

    public Component getDisplayName() {
        return this.merchantTitle;
    }

    public int getMerchantLevel() {
        return this.merchantLevel;
    }

    @Override
    public boolean canRestock() {
        return this.canRestock;
    }

    @Override
    public boolean showProgressBar() {
        return this.showProgressBar;
    }

}
