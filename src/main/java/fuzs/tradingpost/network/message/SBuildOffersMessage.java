package fuzs.tradingpost.network.message;

import fuzs.puzzleslib.network.message.Message;
import fuzs.tradingpost.client.element.TradingPostExtension;
import fuzs.tradingpost.client.gui.screen.inventory.TradingPostScreen;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.IMutableSearchTree;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.network.PacketBuffer;

public class SBuildOffersMessage extends Message {

    private int containerId;
    private Int2IntOpenHashMap idToOfferCount;

    public SBuildOffersMessage() {

    }

    public SBuildOffersMessage(int containerId, Int2IntOpenHashMap idToOfferCount) {

        this.containerId = containerId;
        this.idToOfferCount = idToOfferCount;
    }

    @Override
    public void write(PacketBuffer buf) {

        buf.writeVarInt(this.containerId);
        buf.writeVarInt(this.idToOfferCount.size());
        for (Int2IntMap.Entry entry : this.idToOfferCount.int2IntEntrySet()) {

            buf.writeInt(entry.getIntKey());
            buf.writeVarInt(entry.getIntValue());
        }
    }

    @Override
    public void read(PacketBuffer buf) {

        this.containerId = buf.readVarInt();
        final Int2IntOpenHashMap idToOfferCount = new Int2IntOpenHashMap();
        final int length = buf.readVarInt();
        for (int i = 0; i < length; i++) {

            idToOfferCount.put(buf.readInt(), buf.readVarInt());
        }

        this.idToOfferCount = idToOfferCount;
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new BuildOffersProcessor();
    }

    private class BuildOffersProcessor implements MessageProcessor {

        @Override
        public void accept(PlayerEntity playerEntity) {

            Minecraft mc = Minecraft.getInstance();
            Container container = playerEntity.containerMenu;
            if (SBuildOffersMessage.this.containerId == container.containerId && container instanceof TradingPostContainer && mc.screen instanceof TradingPostScreen) {

                ((TradingPostContainer) container).getTraders().buildOffers(SBuildOffersMessage.this.idToOfferCount);
                this.buildSearchTree(mc, ((TradingPostContainer) container).getOffers());
                ((TradingPostScreen) mc.screen).refreshSearchResults();
            }
        }

        private void buildSearchTree(Minecraft mc, MerchantOffers offers) {

            IMutableSearchTree<MerchantOffer> imutablesearchtree = mc.getSearchTree(TradingPostExtension.OFFER_SEARCH_TREE);
            imutablesearchtree.clear();
            offers.forEach(imutablesearchtree::add);
            imutablesearchtree.refresh();
        }

    }

}
