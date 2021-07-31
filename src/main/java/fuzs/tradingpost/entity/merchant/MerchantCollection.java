package fuzs.tradingpost.entity.merchant;

import com.google.common.collect.Lists;
import fuzs.puzzleslib.PuzzlesLib;
import fuzs.tradingpost.block.TradingPostBlock;
import fuzs.tradingpost.network.message.SBuildOffersMessage;
import fuzs.tradingpost.network.message.SMerchantDataMessage;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.IMerchant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.villager.IVillagerDataHolder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MerchantOffer;
import net.minecraft.item.MerchantOffers;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MerchantCollection implements IMerchant {

    private final Int2ObjectOpenHashMap<IMerchant> idToMerchant = new Int2ObjectOpenHashMap<>();
    private final World level;

    private MerchantOffers allOffers = new MerchantOffers();
    private Object2ObjectOpenHashMap<MerchantOffer, IMerchant> offerToMerchant;
    private IMerchant currentMerchant;

    public MerchantCollection(World level) {

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

    @Override
    @OnlyIn(Dist.CLIENT)
    public void overrideOffers(@Nullable MerchantOffers p_213703_1_) {

        // this is vanilla, we do this differently
        throw new UnsupportedOperationException("Set offers to merchants directly");
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {

        if (this.currentMerchant != null) {

            this.currentMerchant.notifyTrade(offer);
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

    public void disableMerchant(int merchantId) {

        // TODO
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
        MerchantOffers allOffers = new MerchantOffers();
        for (Int2IntMap.Entry entry : sortedEntries) {

            IMerchant merchant = this.idToMerchant.get(entry.getIntKey());
            for (int i = 0; i < entry.getIntValue(); i++) {

                MerchantOffer offer = merchant != null && i < merchant.getOffers().size() ? merchant.getOffers().get(i) : fakeOffer();
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
