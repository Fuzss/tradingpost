package fuzs.tradingpost.network.client.message;

import fuzs.puzzleslib.network.message.Message;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class C2SClearSlotsMessage implements Message<C2SClearSlotsMessage> {

    @Override
    public void write(FriendlyByteBuf buf) {

    }

    @Override
    public void read(FriendlyByteBuf buf) {

    }

    @Override
    public PacketHandler<C2SClearSlotsMessage> makeHandler() {
        return new ClearSlotsHandler();
    }

    private static class ClearSlotsHandler extends PacketHandler<C2SClearSlotsMessage> {

        @Override
        public void handle(C2SClearSlotsMessage packet, Player player, Object gameInstance) {
            AbstractContainerMenu container = player.containerMenu;
            if (container instanceof TradingPostMenu) {
                ((TradingPostMenu) container).clearPaymentSlots();
            }
        }
    }
}
