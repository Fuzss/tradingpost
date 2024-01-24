package fuzs.tradingpost.network;

import fuzs.puzzleslib.api.network.v2.MessageV2;
import fuzs.tradingpost.client.gui.screens.inventory.TradingPostScreen;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public class S2CBuildOffersMessage implements MessageV2<S2CBuildOffersMessage> {
    private int containerId;
    private Int2IntOpenHashMap idToOfferCount;

    public S2CBuildOffersMessage() {

    }

    public S2CBuildOffersMessage(int containerId, Int2IntOpenHashMap idToOfferCount) {
        this.containerId = containerId;
        this.idToOfferCount = idToOfferCount;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.containerId);
        buf.writeVarInt(this.idToOfferCount.size());
        for (Int2IntMap.Entry entry : this.idToOfferCount.int2IntEntrySet()) {
            buf.writeInt(entry.getIntKey());
            buf.writeVarInt(entry.getIntValue());
        }
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.containerId = buf.readVarInt();
        final Int2IntOpenHashMap idToOfferCount = new Int2IntOpenHashMap();
        final int length = buf.readVarInt();
        for (int i = 0; i < length; i++) {
            idToOfferCount.put(buf.readInt(), buf.readVarInt());
        }
        this.idToOfferCount = idToOfferCount;
    }

    @Override
    public MessageHandler<S2CBuildOffersMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(S2CBuildOffersMessage message, Player player, Object gameInstance) {
                Minecraft minecraft = (Minecraft) gameInstance;
                if (message.containerId == player.containerMenu.containerId && player.containerMenu instanceof TradingPostMenu playerMenu && minecraft.screen instanceof TradingPostScreen screen) {
                    playerMenu.getTraders().buildOffers(message.idToOfferCount);
                    minecraft.populateSearchTree(TradingPostScreen.OFFER_SEARCH_TREE, playerMenu.getOffers());
                    screen.refreshSearchResults();
                }
            }
        };
    }
}
