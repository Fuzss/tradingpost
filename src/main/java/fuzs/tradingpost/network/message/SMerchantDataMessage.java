package fuzs.tradingpost.network.message;

import fuzs.puzzleslib.network.message.Message;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.MerchantContainer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.network.PacketBuffer;

public class SMerchantDataMessage extends Message {

    private int containerId;
    private int merchantId;
    private MerchantOffers offers;
    private int villagerLevel;
    private int villagerXp;
    private boolean showProgress;
    private boolean canRestock;

    public SMerchantDataMessage() {

    }

    public SMerchantDataMessage(int containerId, int merchantId, MerchantOffers offers, int villagerLevel, int villagerXp, boolean showProgress, boolean canRestock) {

        this.containerId = containerId;
        this.merchantId = merchantId;
        this.offers = offers;
        this.villagerLevel = villagerLevel;
        this.villagerXp = villagerXp;
        this.showProgress = showProgress;
        this.canRestock = canRestock;
    }

    @Override
    public void write(PacketBuffer buf) {

        buf.writeVarInt(this.containerId);
        buf.writeVarInt(this.merchantId);
        this.offers.writeToStream(buf);
        buf.writeVarInt(this.villagerLevel);
        buf.writeVarInt(this.villagerXp);
        buf.writeBoolean(this.showProgress);
        buf.writeBoolean(this.canRestock);
    }

    @Override
    public void read(PacketBuffer buf) {

        this.containerId = buf.readVarInt();
        this.merchantId = buf.readVarInt();
        this.offers = MerchantOffers.createFromStream(buf);
        this.villagerLevel = buf.readVarInt();
        this.villagerXp = buf.readVarInt();
        this.showProgress = buf.readBoolean();
        this.canRestock = buf.readBoolean();
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new MerchantDataProcessor();
    }

    private class MerchantDataProcessor implements MessageProcessor {

        @Override
        public void accept(PlayerEntity playerEntity) {

            Container container = playerEntity.containerMenu;
            if (SMerchantDataMessage.this.containerId == container.containerId && container instanceof TradingPostContainer) {

                ((MerchantContainer)container).setOffers(new MerchantOffers(SMerchantDataMessage.this.offers.createTag()));
                ((MerchantContainer)container).setXp(SMerchantDataMessage.this.villagerXp);
                ((MerchantContainer)container).setMerchantLevel(SMerchantDataMessage.this.villagerLevel);
                ((MerchantContainer)container).setShowProgressBar(SMerchantDataMessage.this.showProgress);
                ((MerchantContainer)container).setCanRestock(SMerchantDataMessage.this.canRestock);
            }
        }

    }

}
