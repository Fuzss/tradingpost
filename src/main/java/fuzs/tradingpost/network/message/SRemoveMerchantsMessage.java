package fuzs.tradingpost.network.message;

import fuzs.puzzleslib.network.message.Message;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

public class SRemoveMerchantsMessage extends Message {

    private int containerId;
    private IntSet merchantIds;

    public SRemoveMerchantsMessage() {

    }

    public SRemoveMerchantsMessage(int containerId, IntSet merchantIds) {

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
    protected MessageProcessor createProcessor() {

        return new RemoveMerchantProcessor();
    }

    private class RemoveMerchantProcessor implements MessageProcessor {

        @Override
        public void accept(PlayerEntity playerEntity) {

            Container container = playerEntity.containerMenu;
            if (SRemoveMerchantsMessage.this.containerId == container.containerId && container instanceof TradingPostContainer) {

                for (int merchantId : SRemoveMerchantsMessage.this.merchantIds) {

                    ((TradingPostContainer) container).getTraders().removeMerchant(merchantId);
                }
            }
        }

    }

}
