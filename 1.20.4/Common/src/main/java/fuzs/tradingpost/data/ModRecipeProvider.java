package fuzs.tradingpost.data;

import fuzs.puzzleslib.api.data.v2.AbstractRecipeProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import fuzs.tradingpost.init.ModRegistry;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

public class ModRecipeProvider extends AbstractRecipeProvider {

    public ModRecipeProvider(DataProviderContext context) {
        super(context);
    }

    @Override
    public void addRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, ModRegistry.TRADING_POST_BLOCK.value())
                .define('#', ItemTags.PLANKS)
                .define('X', Items.EMERALD)
                .define('S', Items.STICK)
                .pattern(" X ")
                .pattern("###")
                .pattern("S S")
                .unlockedBy(getHasName(Items.EMERALD), has(Items.EMERALD))
                .save(recipeOutput);
    }
}
