package fuzs.tradingpost.entity.merchant;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import fuzs.puzzleslib.PuzzlesLib;
import fuzs.tradingpost.TradingPost;
import fuzs.tradingpost.block.TradingPostBlock;
import fuzs.tradingpost.element.TradingPostElement;
import fuzs.tradingpost.item.TradingPostOffers;
import fuzs.tradingpost.network.message.SBuildOffersMessage;
import fuzs.tradingpost.network.message.SMerchantDataMessage;
import fuzs.tradingpost.network.message.SRemoveMerchantsMessage;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.villager.IVillagerDataHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

public class MerchantCollection implements IMerchant {

    private final Int2ObjectOpenHashMap<IMerchant> idToMerchant = new Int2ObjectOpenHashMap<>();
    private final Set<MerchantOffer> disabledOffers = Sets.newHashSet();
    private final IWorldPosCallable access;
    private final World level;

    private MerchantOffers allOffers = new MerchantOffers();
    private Object2ObjectOpenHashMap<MerchantOffer, IMerchant> offerToMerchant;
    private IMerchant currentMerchant;

    public MerchantCollection(IWorldPosCallable access, World level) {

        this.access = access;
        this.level = level;
    }

    public void addMerchant(int entityId, IMerchant merchant) {

        if (!merchant.getOffers().isEmpty()) {

            this.idToMerchant.put(entityId, merchant);
        }
    }

    @Override
    @Nullable
    public PlayerEntity getTradingPlayer() {

        if (this.currentMerchant != null) {

            return this.currentMerchant.getTradingPlayer();
        }

        return null;
    }

    @Override
    public void setTradingPlayer(@Nullable PlayerEntity player) {

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

            TradingPostElement element = (TradingPostElement) TradingPost.TRADING_POST;
            if (!element.teleportXp) {

                return;
            }

            // reward xp is spawned at trader location, find it and move to trading post
            this.access.execute((level, pos) -> {

                if (this.currentMerchant instanceof Entity) {

                    Vector3d merchantPos = ((Entity) this.currentMerchant).position().add(0.0, 0.5, 0.0);
                    final double xpWidth = 0.5, xpHeight = 0.5;
                    List<ExperienceOrbEntity> xpRewards = this.getLevel().getEntitiesOfClass(ExperienceOrbEntity.class, new AxisAlignedBB(merchantPos.add(-xpWidth, -xpHeight, -xpWidth), merchantPos.add(xpWidth, xpHeight, xpWidth)), Entity::isAlive);
                    // move xp would be much nicer, unfortunately it'd only be moved on the server, so orbs need to be removed and spawned in again
                    for (ExperienceOrbEntity xpOrb : xpRewards) {

                        this.level.addFreshEntity(new ExperienceOrbEntity(xpOrb.level, pos.getX(), pos.getY() + 1.5, pos.getZ(), xpOrb.getValue()));
                        xpOrb.remove();
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
    public World getLevel() {

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

    public IMerchant getCurrentMerchant() {

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

            IMerchant merchant = this.currentMerchant;
            if (merchant instanceof LocalMerchant) {

                return ((LocalMerchant) merchant).getMerchantLevel();
            } else if (merchant instanceof IVillagerDataHolder) {

                return ((IVillagerDataHolder) merchant).getVillagerData().getLevel();
            }
        }

        return 0;
    }

    @OnlyIn(Dist.CLIENT)
    public ITextComponent getDisplayName() {

        if (this.currentMerchant != null) {

            IMerchant merchant = this.currentMerchant;
            if (merchant instanceof LocalMerchant) {

                return ((LocalMerchant) merchant).getDisplayName();
            } else if (merchant instanceof Entity) {

                return ((Entity) merchant).getDisplayName();
            }
        }

        return TradingPostBlock.CONTAINER_TITLE;
    }

    public boolean checkAvailableMerchants(int containerId, BlockPos pos, PlayerEntity player) {

        IntSet toRemove = new IntOpenHashSet();
        for (Map.Entry<Integer, IMerchant> entry : this.idToMerchant.int2ObjectEntrySet()) {

            if (entry.getValue() instanceof Entity) {

                if (!this.traderInRange((Entity) entry.getValue(), pos)) {

                    toRemove.add(entry.getKey().intValue());
                }
            }
        }

        if (!toRemove.isEmpty()) {

            toRemove.forEach((IntConsumer) this::removeMerchant);
            PuzzlesLib.getNetworkHandler().sendTo(new SRemoveMerchantsMessage(containerId, toRemove), (ServerPlayerEntity) player);
        }

        return !this.idToMerchant.isEmpty();
    }

    private boolean traderInRange(Entity entity, BlockPos pos) {

        return this.traderInRange(entity, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    private boolean traderInRange(Entity entity, double posX, double posY, double posZ) {

        TradingPostElement element = (TradingPostElement) TradingPost.TRADING_POST;
        return Math.abs(entity.getX() - posX) <= element.horizontalRange && Math.abs(entity.getY() - posY) <= element.verticalRange && Math.abs(entity.getZ() - posZ) <= element.horizontalRange;
    }

    public void removeMerchant(int merchantId) {

        IMerchant merchant = this.idToMerchant.get(merchantId);
        this.disabledOffers.addAll(merchant.getOffers());
        this.idToMerchant.remove(merchantId);
        merchant.setTradingPlayer(null);
        if (this.currentMerchant == merchant) {

            this.currentMerchant = null;
        }
    }

    public void setActiveOffer(MerchantOffer offer) {

        if (this.offerToMerchant != null) {

            this.currentMerchant = offer != null ? this.offerToMerchant.get(offer) : null;
        }
    }

    public void sendMerchantData(final int containerId, PlayerEntity player) {

        for (Map.Entry<Integer, IMerchant> entry : this.idToMerchant.int2ObjectEntrySet()) {

            IMerchant merchant = entry.getValue();
            final ITextComponent merchantTitle = merchant instanceof Entity ? ((Entity) merchant).getDisplayName() : TradingPostBlock.CONTAINER_TITLE;
            final int merchantLevel = merchant instanceof IVillagerDataHolder ? ((IVillagerDataHolder) merchant).getVillagerData().getLevel() : 0;

            SMerchantDataMessage message = new SMerchantDataMessage(containerId, entry.getKey(), merchantTitle, merchant.getOffers(), merchantLevel, merchant.getVillagerXp(), merchant.showProgressBar(), merchant.canRestock());
            PuzzlesLib.getNetworkHandler().sendTo(message, (ServerPlayerEntity) player);
        }

        PuzzlesLib.getNetworkHandler().sendTo(new SBuildOffersMessage(containerId, this.getIdToOfferCountMap()), (ServerPlayerEntity) player);
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

            IMerchant merchant = this.idToMerchant.get(entry.getIntKey());
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

        Object2ObjectOpenHashMap<MerchantOffer, IMerchant> offerToMerchant = new Object2ObjectOpenHashMap<>();
        for (IMerchant merchant : this.idToMerchant.values()) {

            merchant.getOffers().forEach(offer -> offerToMerchant.put(offer, merchant));
        }

        this.offerToMerchant = offerToMerchant;
    }

    private static MerchantOffer fakeOffer() {

        return new MerchantOffer(ItemStack.EMPTY, ItemStack.EMPTY, -1, -1, 0.0F);
    }
    
}
