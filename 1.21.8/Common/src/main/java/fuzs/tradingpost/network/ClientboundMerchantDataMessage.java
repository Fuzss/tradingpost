package fuzs.tradingpost.network;

import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.WritableMessage;
import fuzs.puzzleslib.api.network.v4.message.play.ClientboundPlayMessage;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.trading.MerchantOffers;

public final class ClientboundMerchantDataMessage implements ClientboundPlayMessage, WritableMessage<RegistryFriendlyByteBuf> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundMerchantDataMessage> STREAM_CODEC = WritableMessage.streamCodec(
            ClientboundMerchantDataMessage::new);
    private final int containerId;
    private final int merchantId;
    private final Component merchantTitle;
    private final MerchantOffers offers;
    private final int villagerLevel;
    private final int villagerXp;
    private final boolean showProgress;
    private final boolean canRestock;

    public ClientboundMerchantDataMessage(int containerId, int merchantId, Component merchantTitle, MerchantOffers offers, int villagerLevel, int villagerXp, boolean showProgress, boolean canRestock) {
        this.containerId = containerId;
        this.merchantId = merchantId;
        this.merchantTitle = merchantTitle;
        this.offers = offers;
        this.villagerLevel = villagerLevel;
        this.villagerXp = villagerXp;
        this.showProgress = showProgress;
        this.canRestock = canRestock;
    }

    private ClientboundMerchantDataMessage(RegistryFriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        this.merchantId = buf.readInt();
        this.merchantTitle = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buf);
        this.offers = MerchantOffers.STREAM_CODEC.decode(buf);
        this.villagerLevel = buf.readVarInt();
        this.villagerXp = buf.readVarInt();
        this.showProgress = buf.readBoolean();
        this.canRestock = buf.readBoolean();
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.containerId);
        buf.writeInt(this.merchantId);
        ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buf, this.merchantTitle);
        MerchantOffers.STREAM_CODEC.encode(buf, this.offers);
        buf.writeVarInt(this.villagerLevel);
        buf.writeVarInt(this.villagerXp);
        buf.writeBoolean(this.showProgress);
        buf.writeBoolean(this.canRestock);
    }

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<>() {
            @Override
            public void accept(Context context) {
                if (context.player().containerMenu instanceof TradingPostMenu menu &&
                        ClientboundMerchantDataMessage.this.containerId == menu.containerId) {
                    menu.addMerchant(context.player(),
                            ClientboundMerchantDataMessage.this.merchantId,
                            ClientboundMerchantDataMessage.this.merchantTitle,
                            ClientboundMerchantDataMessage.this.offers,
                            ClientboundMerchantDataMessage.this.villagerLevel,
                            ClientboundMerchantDataMessage.this.villagerXp,
                            ClientboundMerchantDataMessage.this.showProgress,
                            ClientboundMerchantDataMessage.this.canRestock);
                }
            }
        };
    }
}
