package fuzs.tradingpost.network.message;

import fuzs.puzzleslib.network.message.Message;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

public class SRemoveMerchantMessage extends Message {

    private int containerId;
    private int merchantId;

    public SRemoveMerchantMessage() {

    }

    public SRemoveMerchantMessage(int containerId, int merchantId) {

        this.containerId = containerId;
        this.merchantId = merchantId;
    }

    @Override
    public void write(PacketBuffer buf) {

        buf.writeInt(this.containerId);
        buf.writeInt(this.merchantId);
    }

    @Override
    public void read(PacketBuffer buf) {

        this.containerId = buf.readInt();
        this.merchantId = buf.readInt();
    }

    @Override
    protected MessageProcessor createProcessor() {

        return new RemoveMerchantProcessor();
    }

    private class RemoveMerchantProcessor implements MessageProcessor {

        @Override
        public void accept(PlayerEntity playerEntity) {

            Container container = playerEntity.containerMenu;
            if (SRemoveMerchantMessage.this.containerId == container.containerId && container instanceof TradingPostContainer) {

                ((TradingPostContainer) container).removeMerchant(SRemoveMerchantMessage.this.merchantId);
            }
        }

    }

}
