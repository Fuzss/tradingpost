package fuzs.tradingpost.network;

import fuzs.puzzleslib.api.network.v2.MessageV2;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.trading.MerchantOffers;

public class S2CMerchantDataMessage implements MessageV2<S2CMerchantDataMessage> {
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
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, this.merchantTitle);
        MerchantOffers.STREAM_CODEC.encode((RegistryFriendlyByteBuf) buf, this.offers);
        buf.writeVarInt(this.villagerLevel);
        buf.writeVarInt(this.villagerXp);
        buf.writeBoolean(this.showProgress);
        buf.writeBoolean(this.canRestock);
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        this.merchantId = buf.readInt();
        this.merchantTitle = ComponentSerialization.TRUSTED_STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
        this.offers = MerchantOffers.STREAM_CODEC.decode((RegistryFriendlyByteBuf) buf);
        this.villagerLevel = buf.readVarInt();
        this.villagerXp = buf.readVarInt();
        this.showProgress = buf.readBoolean();
        this.canRestock = buf.readBoolean();
    }

    @Override
    public MessageHandler<S2CMerchantDataMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(S2CMerchantDataMessage packet, Player player, Object gameInstance) {
                AbstractContainerMenu container = player.containerMenu;
                if (packet.containerId == container.containerId && container instanceof TradingPostMenu) {
                    ((TradingPostMenu) container).addMerchant(player, packet.merchantId, packet.merchantTitle, new MerchantOffers(packet.offers.createTag()), packet.villagerLevel, packet.villagerXp, packet.showProgress, packet.canRestock);
                }
            }
        };
    }
}
