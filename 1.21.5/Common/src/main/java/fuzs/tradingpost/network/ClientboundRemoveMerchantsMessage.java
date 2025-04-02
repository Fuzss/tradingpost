package fuzs.tradingpost.network;

import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.WritableMessage;
import fuzs.puzzleslib.api.network.v4.message.play.ClientboundPlayMessage;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class ClientboundRemoveMerchantsMessage implements ClientboundPlayMessage, WritableMessage<RegistryFriendlyByteBuf> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRemoveMerchantsMessage> STREAM_CODEC = WritableMessage.streamCodec(
            ClientboundRemoveMerchantsMessage::new);
    private final int containerId;
    private final IntSet merchantIds;

    public ClientboundRemoveMerchantsMessage(int containerId, IntSet merchantIds) {
        this.containerId = containerId;
        this.merchantIds = merchantIds;
    }

    private ClientboundRemoveMerchantsMessage(RegistryFriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        int length = buf.readVarInt();
        IntSet merchantIds = new IntOpenHashSet();
        for (int i = 0; i < length; i++) {
            merchantIds.add(buf.readInt());
        }
        this.merchantIds = merchantIds;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.containerId);
        buf.writeVarInt(this.merchantIds.size());
        for (int merchantId : this.merchantIds) {
            buf.writeInt(merchantId);
        }
    }

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<>() {
            @Override
            public void accept(Context context) {
                if (context.player().containerMenu instanceof TradingPostMenu menu &&
                        ClientboundRemoveMerchantsMessage.this.containerId == menu.containerId) {
                    for (int merchantId : ClientboundRemoveMerchantsMessage.this.merchantIds) {
                        menu.getTraders().removeMerchant(merchantId);
                    }
                }
            }
        };
    }
}
