package fuzs.tradingpost.network.message;

import fuzs.puzzleslib.network.message.Message;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

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
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.containerId);
        buf.writeVarInt(this.merchantIds.size());
        for (int merchantId : this.merchantIds) {
            buf.writeInt(merchantId);
        }
    }

    @Override
    public void read(FriendlyByteBuf buf) {
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
        public void handle(S2CRemoveMerchantsMessage packet, Player player, Object gameInstance) {
            AbstractContainerMenu container = player.containerMenu;
            if (packet.containerId == container.containerId && container instanceof TradingPostMenu) {
                for (int merchantId : packet.merchantIds) {
                    ((TradingPostMenu) container).getTraders().removeMerchant(merchantId);
                }
            }
        }
    }
}
