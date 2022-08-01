package fuzs.tradingpost.network.message;

import fuzs.puzzleslib.network.message.Message;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.trading.MerchantOffers;

public class S2CMerchantDataMessage implements Message {
    private int containerId;
    private int merchantId;
    private Component merchantTitle;
    private MerchantOffers offers;
    private int villagerLevel;
    private int villagerXp;
    private boolean showProgress;
    private boolean canRestock;

    public S2CMerchantDataMessage() {

    }

    public S2CMerchantDataMessage(int containerId, int merchantId, Component merchantTitle, MerchantOffers offers, int villagerLevel, int villagerXp, boolean showProgress, boolean canRestock) {
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
    public void write(FriendlyByteBuf buf) {
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
    public void read(FriendlyByteBuf buf) {
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
        public void handle(S2CMerchantDataMessage packet, Player player, Object gameInstance) {
            AbstractContainerMenu container = player.containerMenu;
            if (packet.containerId == container.containerId && container instanceof TradingPostMenu) {
                ((TradingPostMenu) container).addMerchant(player, packet.merchantId, packet.merchantTitle, new MerchantOffers(packet.offers.createTag()), packet.villagerLevel, packet.villagerXp, packet.showProgress, packet.canRestock);
            }
        }
    }
}
