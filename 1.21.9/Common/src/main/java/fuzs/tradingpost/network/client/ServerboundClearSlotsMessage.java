package fuzs.tradingpost.network.client;

import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.WritableMessage;
import fuzs.puzzleslib.api.network.v4.message.play.ServerboundPlayMessage;
import fuzs.tradingpost.world.inventory.TradingPostMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public final class ServerboundClearSlotsMessage implements ServerboundPlayMessage, WritableMessage<RegistryFriendlyByteBuf> {
    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundClearSlotsMessage> STREAM_CODEC = WritableMessage.streamCodec(
            ServerboundClearSlotsMessage::new);

    public ServerboundClearSlotsMessage() {
        // NO-OP
    }

    private ServerboundClearSlotsMessage(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        // NO-OP
    }

    @Override
    public void write(RegistryFriendlyByteBuf registryFriendlyByteBuf) {
        // NO-OP
    }

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<>() {
            @Override
            public void accept(Context context) {
                if (context.player().containerMenu instanceof TradingPostMenu menu) {
                    menu.clearPaymentSlots();
                }
            }
        };
    }
}
