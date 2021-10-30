package fuzs.tradingpost.network.message;

import fuzs.puzzleslib.network.v2.message.Message;
import fuzs.tradingpost.inventory.container.TradingPostContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.network.PacketBuffer;

public class C2SClearSlotsMessage implements Message {
    @Override
    public void write(PacketBuffer buf) {

    }

    @Override
    public void read(PacketBuffer buf) {

    }

    @Override
    public ClearSlotsHandler makeHandler() {
        return new ClearSlotsHandler();
    }

    private static class ClearSlotsHandler extends PacketHandler<C2SClearSlotsMessage> {

        @Override
        public void handle(C2SClearSlotsMessage packet, PlayerEntity player, Object gameInstance) {
            Container container = player.containerMenu;
            if (container instanceof TradingPostContainer) {
                ((TradingPostContainer) container).clearPaymentSlots();
            }
        }
    }
}
