package fuzs.tradingpost.network.client;

import fuzs.puzzleslib.api.network.v2.MessageV2;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class C2SClearSlotsMessage implements MessageV2<C2SClearSlotsMessage> {

    @Override
    public void write(FriendlyByteBuf buf) {

    }

    @Override
    public void read(FriendlyByteBuf buf) {

    }

    @Override
    public MessageHandler<C2SClearSlotsMessage> makeHandler() {
        return new MessageHandler<>() {

            @Override
            public void handle(C2SClearSlotsMessage packet, Player player, Object gameInstance) {
                AbstractContainerMenu container = player.containerMenu;
                if (container instanceof TradingPostMenu) {
                    ((TradingPostMenu) container).clearPaymentSlots();
                }
            }
        };
    }
}
