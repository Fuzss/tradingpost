package fuzs.tradingpost.network.message;

import fuzs.puzzleslib.network.message.Message;
import fuzs.tradingpost.client.TradingPostClient;
import fuzs.tradingpost.client.gui.screens.inventory.TradingPostScreen;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.searchtree.MutableSearchTree;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

public class S2CBuildOffersMessage implements Message {
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
    public BuildOffersHandler makeHandler() {
        return new BuildOffersHandler();
    }

    private static class BuildOffersHandler extends PacketHandler<S2CBuildOffersMessage> {

        @Override
        public void handle(S2CBuildOffersMessage packet, Player player, Object gameInstance) {
            Minecraft mc = Minecraft.getInstance();
            AbstractContainerMenu container = player.containerMenu;
            if (packet.containerId == container.containerId && container instanceof TradingPostMenu && mc.screen instanceof TradingPostScreen) {
                ((TradingPostMenu) container).getTraders().buildOffers(packet.idToOfferCount);
                this.buildSearchTree(mc, ((TradingPostMenu) container).getOffers());
                ((TradingPostScreen) mc.screen).refreshSearchResults();
            }
        }

        private void buildSearchTree(Minecraft mc, MerchantOffers offers) {
            MutableSearchTree<MerchantOffer> imutablesearchtree = mc.getSearchTree(TradingPostClient.OFFER_SEARCH_TREE);
            imutablesearchtree.clear();
            offers.forEach(imutablesearchtree::add);
            imutablesearchtree.refresh();
        }
    }
}
