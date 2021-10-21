package fuzs.tradingpost.network.message;

import fuzs.puzzleslib.network.v2.message.Message;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.MerchantOffers;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public class S2CMerchantDataMessage implements Message {

    private int containerId;
    private int merchantId;
    private ITextComponent merchantTitle;
    private MerchantOffers offers;
    private int villagerLevel;
    private int villagerXp;
    private boolean showProgress;
    private boolean canRestock;

    public S2CMerchantDataMessage() {

    }

    public S2CMerchantDataMessage(int containerId, int merchantId, ITextComponent merchantTitle, MerchantOffers offers, int villagerLevel, int villagerXp, boolean showProgress, boolean canRestock) {

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
        buf.writeInt(this.merchantId);
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
        this.merchantId = buf.readInt();
        this.merchantTitle = buf.readComponent();
        this.offers = MerchantOffers.createFromStream(buf);
        this.villagerLevel = buf.readVarInt();
        this.villagerXp = buf.readVarInt();
        this.showProgress = buf.readBoolean();
        this.canRestock = buf.readBoolean();
    }

    @Override
    public MerchantDataHandler makeHandler() {
        return new MerchantDataHandler();
    }

    private static class MerchantDataHandler extends PacketHandler<S2CMerchantDataMessage> {

        @Override
        public void handle(S2CMerchantDataMessage packet, PlayerEntity player, Object gameInstance) {
            Container container = player.containerMenu;
            if (packet.containerId == container.containerId && container instanceof TradingPostContainer) {

                ((TradingPostContainer) container).addMerchant(player, packet.merchantId, packet.merchantTitle, new MerchantOffers(packet.offers.createTag()), packet.villagerLevel, packet.villagerXp, packet.showProgress, packet.canRestock);
            }
        }
    }

}
