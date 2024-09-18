package fuzs.tradingpost.world.level.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class TradingPostAnimationController {
    private final Vec3 position;

    public int time;
    public float open;
    public float oOpen;
    public float rot;
    public float oRot;
    public float tRot;

    public TradingPostAnimationController(BlockPos blockPos) {
        this.position = blockPos.getCenter();
    }

    public void tick(Level level) {
        ++this.time;
        this.oOpen = this.open;
        this.oRot = this.rot;
        this.setOpenness(level);
        this.tickAnimation();
    }

    private void setOpenness(Level level) {
        Player player = level.getNearestPlayer(this.position.x(), this.position.y(), this.position.z(), 3.0, false);
        if (player != null) {
            double d0 = player.getX() - this.position.x();
            double d1 = player.getZ() - this.position.z();
            this.tRot = (float) Mth.atan2(d1, d0);
            this.open += 0.1F;
        } else {
            this.tRot += 0.02F;
            this.open -= 0.1F;
        }
    }

    private void tickAnimation() {
        while (this.rot >= (float) Math.PI) {
            this.rot -= ((float) Math.PI * 2.0F);
        }
        while (this.rot < -(float) Math.PI) {
            this.rot += ((float) Math.PI * 2.0F);
        }
        while (this.tRot >= (float) Math.PI) {
            this.tRot -= ((float) Math.PI * 2.0F);
        }
        while (this.tRot < -(float) Math.PI) {
            this.tRot += ((float) Math.PI * 2.0F);
        }
        float f2;
        f2 = this.tRot - this.rot;
        while (f2 >= (float) Math.PI) {
            f2 -= ((float) Math.PI * 2.0F);
        }
        while (f2 < -(float) Math.PI) {
            f2 += ((float) Math.PI * 2.0F);
        }
        this.rot += f2 * 0.4F;
        this.open = Mth.clamp(this.open, 0.0F, 1.0F);
    }
}
