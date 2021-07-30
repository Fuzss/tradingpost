package fuzs.tradingpost.network.message;

import fuzs.puzzleslib.network.message.Message;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.MerchantOffers;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class SMerchantDataMessage extends Message {

    private int containerId;
    private int merchantId;
    private ITextComponent merchantTitle;
    private MerchantOffers offers;
    private int villagerLevel;
    private int villagerXp;
    private boolean showProgress;
    private boolean canRestock;

    public SMerchantDataMessage() {

    }

    public SMerchantDataMessage(int containerId, int merchantId, ITextComponent merchantTitle, MerchantOffers offers, int villagerLevel, int villagerXp, boolean showProgress, boolean canRestock) {

        this.containerId = containerId;
        this.merchantId = merchantId;
        this.merchantTitle = merchantTitle;
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
        buf.writeComponent(this.merchantTitle);
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
        this.merchantTitle = buf.readComponent();
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

                ((TradingPostContainer) container).addMerchant(playerEntity, SMerchantDataMessage.this.merchantId, SMerchantDataMessage.this.merchantTitle, new MerchantOffers(SMerchantDataMessage.this.offers.createTag()), SMerchantDataMessage.this.villagerLevel, SMerchantDataMessage.this.villagerXp, SMerchantDataMessage.this.showProgress, SMerchantDataMessage.this.canRestock);
            }
        }

    }

}
