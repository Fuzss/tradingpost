package fuzs.tradingpost.network;

import fuzs.puzzleslib.api.client.util.v1.SearchRegistryHelper;
import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.WritableMessage;
import fuzs.puzzleslib.api.network.v4.message.play.ClientboundPlayMessage;
import fuzs.tradingpost.client.TradingPostClient;
import fuzs.tradingpost.client.gui.screens.inventory.TradingPostScreen;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class ClientboundBuildOffersMessage implements ClientboundPlayMessage, WritableMessage<RegistryFriendlyByteBuf> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundBuildOffersMessage> STREAM_CODEC = WritableMessage.streamCodec(
            ClientboundBuildOffersMessage::new);
    private final int containerId;
    private final Int2IntOpenHashMap idToOfferCount;

    public ClientboundBuildOffersMessage(int containerId, Int2IntOpenHashMap idToOfferCount) {
        this.containerId = containerId;
        this.idToOfferCount = idToOfferCount;
    }

    private ClientboundBuildOffersMessage(RegistryFriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        final Int2IntOpenHashMap idToOfferCount = new Int2IntOpenHashMap();
        final int length = buf.readVarInt();
        for (int i = 0; i < length; i++) {
            idToOfferCount.put(buf.readInt(), buf.readVarInt());
        }
        this.idToOfferCount = idToOfferCount;
    }

    @Override
    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeVarInt(this.containerId);
        buf.writeVarInt(this.idToOfferCount.size());
        for (Int2IntMap.Entry entry : this.idToOfferCount.int2IntEntrySet()) {
            buf.writeInt(entry.getIntKey());
            buf.writeVarInt(entry.getIntValue());
        }
    }

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<>() {
            @Override
            public void accept(Context context) {
                if (context.player().containerMenu instanceof TradingPostMenu menu
                        && ClientboundBuildOffersMessage.this.containerId == menu.containerId) {
                    if (context.client().screen instanceof TradingPostScreen screen) {
                        menu.getTraders().buildOffers(ClientboundBuildOffersMessage.this.idToOfferCount);
                        SearchRegistryHelper.populateSearchTree(TradingPostClient.MERCHANT_OFFERS_SEARCH_TREE,
                                menu.getOffers());
                        screen.refreshSearchResults();
                    }
                }
            }
        };
    }
}
