package fuzs.tradingpost.world.entity.npc;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.network.message.S2CBuildOffersMessage;
import fuzs.tradingpost.network.message.S2CMerchantDataMessage;
import fuzs.tradingpost.network.message.S2CRemoveMerchantsMessage;
import fuzs.tradingpost.world.item.trading.TradingPostOffers;
import fuzs.tradingpost.world.level.block.TradingPostBlock;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.npc.VillagerDataHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

public class MerchantCollection implements Merchant {
    private final Int2ObjectOpenHashMap<Merchant> idToMerchant = new Int2ObjectOpenHashMap<>();
    private final Set<MerchantOffer> disabledOffers = Sets.newHashSet();
    private final ContainerLevelAccess access;
    private final Level level;

    private MerchantOffers allOffers = new MerchantOffers();
    private Object2ObjectOpenHashMap<MerchantOffer, Merchant> offerToMerchant;
    private Merchant currentMerchant;

    public MerchantCollection(ContainerLevelAccess access, Level level) {
        this.access = access;
        this.level = level;
    }

    public void addMerchant(int entityId, Merchant merchant) {
        if (!merchant.getOffers().isEmpty()) {
            this.idToMerchant.put(entityId, merchant);
        }
    }

    @Override
    @Nullable
    public Player getTradingPlayer() {
        if (this.currentMerchant != null) {
            return this.currentMerchant.getTradingPlayer();
        }
        return null;
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        this.idToMerchant.values().forEach(merchant -> merchant.setTradingPlayer(player));
    }

    @Override
    public MerchantOffers getOffers() {
        return this.allOffers;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean checkOffer(MerchantOffer offer) {
        return !this.disabledOffers.contains(offer);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void overrideOffers(@Nullable MerchantOffers p_213703_1_) {
        // this is vanilla, we do this differently
        TradingPost.LOGGER.error("Set offers to stored merchants directly");
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        if (this.currentMerchant != null) {

            this.currentMerchant.notifyTrade(offer);

            if (!TradingPost.CONFIG.server().teleportXp) {

                return;
            }

            // reward xp is spawned at trader location, find it and move to trading post
            this.access.execute((level, pos) -> {

                if (this.currentMerchant instanceof Entity) {

                    Vec3 merchantPos = ((Entity) this.currentMerchant).position().add(0.0, 0.5, 0.0);
                    final double xpWidth = 0.5, xpHeight = 0.5;
                    List<ExperienceOrb> xpRewards = this.getLevel().getEntitiesOfClass(ExperienceOrb.class, new AABB(merchantPos.add(-xpWidth, -xpHeight, -xpWidth), merchantPos.add(xpWidth, xpHeight, xpWidth)), Entity::isAlive);
                    // move xp would be much nicer, unfortunately it'd only be moved on the server, so orbs need to be removed and spawned in again
                    for (ExperienceOrb xpOrb : xpRewards) {
                        this.level.addFreshEntity(new ExperienceOrb(xpOrb.level, pos.getX(), pos.getY() + 1.5, pos.getZ(), xpOrb.getValue()));
                        xpOrb.discard();
                    }
                }
            });
        }
    }

    @Override
    public void notifyTradeUpdated(ItemStack stack) {
        if (this.currentMerchant != null) {
            this.currentMerchant.notifyTradeUpdated(stack);
        }
    }

    @Override
    public Level getLevel() {
        // only here for compatibility with mods and for use in MerchantContainer::remove
        return this.level;
    }

    @Override
    public int getVillagerXp() {
        if (this.currentMerchant != null) {
            return this.currentMerchant.getVillagerXp();
        }
        return 0;
    }

    public Merchant getCurrentMerchant() {
        return this.currentMerchant;
    }

    @Override
    public boolean canRestock() {
        if (this.currentMerchant != null) {
            return this.currentMerchant.canRestock();
        }
        return false;
    }

    @Override
    public void overrideXp(int xpValue) {
        if (this.currentMerchant != null) {
            this.currentMerchant.overrideXp(xpValue);
        }
    }

    @Override
    public boolean showProgressBar() {
        if (this.currentMerchant != null) {
            return this.currentMerchant.showProgressBar();
        }
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        if (this.currentMerchant != null) {
            return this.currentMerchant.getNotifyTradeSound();
        }
        // unused by client, just a dummy
        return SoundEvents.VILLAGER_YES;
    }

    @OnlyIn(Dist.CLIENT)
    public int getTraderLevel() {
        if (this.currentMerchant != null) {
            Merchant merchant = this.currentMerchant;
            if (merchant instanceof LocalMerchant) {
                return ((LocalMerchant) merchant).getMerchantLevel();
            } else if (merchant instanceof VillagerDataHolder) {
                return ((VillagerDataHolder) merchant).getVillagerData().getLevel();
            }
        }
        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    @Nullable
    public Component getDisplayName() {
        if (this.currentMerchant != null) {
            Merchant merchant = this.currentMerchant;
            if (merchant instanceof LocalMerchant) {
                return ((LocalMerchant) merchant).getDisplayName();
            } else if (merchant instanceof Entity) {
                return ((Entity) merchant).getDisplayName();
            }
        }
        return null;
    }

    public boolean updateAvailableMerchants(int containerId, BlockPos pos, Player player, boolean testRange) {
        IntSet toRemove = new IntOpenHashSet();
        for (Map.Entry<Integer, Merchant> entry : this.idToMerchant.int2ObjectEntrySet()) {
            if (entry.getValue() instanceof Entity) {
                if (entry.getValue().getTradingPlayer() != player || testRange && !this.traderInRange((Entity) entry.getValue(), pos)) {
                    toRemove.add(entry.getKey().intValue());
                }
            }
        }
        if (!toRemove.isEmpty()) {
            toRemove.forEach((IntConsumer) this::removeMerchant);
            TradingPost.NETWORK.sendTo(new S2CRemoveMerchantsMessage(containerId, toRemove), (ServerPlayer) player);
        }
        return !this.idToMerchant.isEmpty();
    }

    private boolean traderInRange(Entity entity, BlockPos pos) {
        return this.traderInRange(entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    private boolean traderInRange(Entity entity, double posX, double posY, double posZ) {
        return Math.abs(entity.getX() - posX) <= TradingPost.CONFIG.server().horizontalRange && Math.abs(entity.getY() - posY) <= TradingPost.CONFIG.server().verticalRange && Math.abs(entity.getZ() - posZ) <= TradingPost.CONFIG.server().horizontalRange;
    }

    public void removeMerchant(int merchantId) {
        Merchant merchant = this.idToMerchant.get(merchantId);
        if (merchant != null) {
            this.disabledOffers.addAll(merchant.getOffers());
            this.idToMerchant.remove(merchantId);
            merchant.setTradingPlayer(null);
            if (this.currentMerchant == merchant) {
                this.currentMerchant = null;
            }
        }
    }

    public void setActiveOffer(MerchantOffer offer) {
        if (this.offerToMerchant != null) {
            this.currentMerchant = offer != null ? this.offerToMerchant.get(offer) : null;
        }
    }

    public void sendMerchantData(final int containerId, Player player) {
        for (Map.Entry<Integer, Merchant> entry : this.idToMerchant.int2ObjectEntrySet()) {
            Merchant merchant = entry.getValue();
            final Component merchantTitle = merchant instanceof Entity ? ((Entity) merchant).getDisplayName() : TradingPostBlock.CONTAINER_TITLE;
            final int merchantLevel = merchant instanceof VillagerDataHolder ? ((VillagerDataHolder) merchant).getVillagerData().getLevel() : 0;
            S2CMerchantDataMessage message = new S2CMerchantDataMessage(containerId, entry.getKey(), merchantTitle, merchant.getOffers(), merchantLevel, merchant.getVillagerXp(), merchant.showProgressBar(), merchant.canRestock());
            TradingPost.NETWORK.sendTo(message, (ServerPlayer) player);
        }
        TradingPost.NETWORK.sendTo(new S2CBuildOffersMessage(containerId, this.getIdToOfferCountMap()), (ServerPlayer) player);
    }

    public Int2IntOpenHashMap getIdToOfferCountMap() {
        return this.idToMerchant.int2ObjectEntrySet().stream()
                .collect(Collectors.toMap(Int2ObjectMap.Entry::getIntKey, entry -> entry.getValue().getOffers().size(), (o1, o2) -> o1, Int2IntOpenHashMap::new));
    }

    public void buildOffers(Int2IntOpenHashMap idToOfferCount) {
        List<Int2IntMap.Entry> sortedEntries = Lists.newArrayList(idToOfferCount.int2IntEntrySet());
        sortedEntries.sort(Comparator.comparingInt(Int2IntMap.Entry::getIntKey));
        MerchantOffers allOffers = new TradingPostOffers(this.disabledOffers);
        for (Int2IntMap.Entry entry : sortedEntries) {
            Merchant merchant = this.idToMerchant.get(entry.getIntKey());
            for (int i = 0; i < entry.getIntValue(); i++) {
                MerchantOffer offer;
                if (merchant != null && i < merchant.getOffers().size()) {
                    offer = merchant.getOffers().get(i);
                } else {
                    // this should never actually happen, but you never know with mods
                    offer = fakeOffer();
                    this.disabledOffers.add(offer);
                }
                allOffers.add(offer);
            }
        }
        this.allOffers = allOffers;
        this.buildOfferToMerchantMap();
    }

    private void buildOfferToMerchantMap() {
        Object2ObjectOpenHashMap<MerchantOffer, Merchant> offerToMerchant = new Object2ObjectOpenHashMap<>();
        for (Merchant merchant : this.idToMerchant.values()) {
            merchant.getOffers().forEach(offer -> offerToMerchant.put(offer, merchant));
        }
        this.offerToMerchant = offerToMerchant;
    }

    private static MerchantOffer fakeOffer() {
        return new MerchantOffer(ItemStack.EMPTY, ItemStack.EMPTY, -1, -1, 0.0F);
    }
}
