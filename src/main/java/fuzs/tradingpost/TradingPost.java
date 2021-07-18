package fuzs.tradingpost;

import fuzs.puzzleslib.PuzzlesLib;
import fuzs.puzzleslib.element.AbstractElement;
import fuzs.tradingpost.element.TradingPostElement;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@Mod(TradingPost.MODID)
public class TradingPost {

    public static final String MODID = "tradingpost";
    public static final String NAME = "Trading Post";
    public static final Logger LOGGER = LogManager.getLogger(NAME);

    public static final AbstractElement VISUAL_WORKBENCH = PuzzlesLib.register(MODID, "visual_workbench", TradingPostElement::new);

    public TradingPost() {

        PuzzlesLib.setup(true);
    }

}
