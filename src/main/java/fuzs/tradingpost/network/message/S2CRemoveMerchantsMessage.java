package fuzs.tradingpost.network.message;

import fuzs.puzzleslib.network.v2.message.Message;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

public class S2CRemoveMerchantsMessage implements Message {

    private int containerId;
    private IntSet merchantIds;

    public S2CRemoveMerchantsMessage() {

    }

    public S2CRemoveMerchantsMessage(int containerId, IntSet merchantIds) {

        this.containerId = containerId;
        this.merchantIds = merchantIds;
    }

    @Override
    public void write(PacketBuffer buf) {

        buf.writeVarInt(this.containerId);
        buf.writeVarInt(this.merchantIds.size());
        for (int merchantId : this.merchantIds) {

            buf.writeInt(merchantId);
        }
    }

    @Override
    public void read(PacketBuffer buf) {

        this.containerId = buf.readVarInt();
        int length = buf.readVarInt();
        IntSet merchantIds = new IntOpenHashSet();
        for (int i = 0; i < length; i++) {

            merchantIds.add(buf.readInt());
        }

        this.merchantIds = merchantIds;
    }

    @Override
    public RemoveMerchantHandler makeHandler() {
        return new RemoveMerchantHandler();
    }

    private static class RemoveMerchantHandler extends PacketHandler<S2CRemoveMerchantsMessage> {

        @Override
        public void handle(S2CRemoveMerchantsMessage packet, PlayerEntity player, Object gameInstance) {
            Container container = player.containerMenu;
            if (packet.containerId == container.containerId && container instanceof TradingPostContainer) {

                for (int merchantId : packet.merchantIds) {

                    ((TradingPostContainer) container).getTraders().removeMerchant(merchantId);
                }
            }
        }
    }

}
