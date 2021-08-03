package fuzs.tradingpost.mixin.client.accessor;

import net.minecraft.client.gui.widget.button.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Button.class)
public interface ButtonAccessor {

    @Accessor
    void setOnPress(Button.IPressable onPress);

}
