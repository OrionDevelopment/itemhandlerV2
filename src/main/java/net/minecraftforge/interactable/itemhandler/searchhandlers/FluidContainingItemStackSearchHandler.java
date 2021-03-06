package net.minecraftforge.interactable.itemhandler.searchhandlers;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.interactable.api.IInteractableSearchHandler;

import java.util.Optional;

public class FluidContainingItemStackSearchHandler implements IInteractableSearchHandler<ItemStack> {
    private final FluidStack fluidStackToTest;
    private final MatchingStrategy matchingStrategy;

    public FluidContainingItemStackSearchHandler(FluidStack fluidStackToTest, MatchingStrategy matchingStrategy) {
        this.fluidStackToTest = fluidStackToTest;
        this.matchingStrategy = matchingStrategy;
    }

    @Override
    public boolean test(ItemStack stack) {
        Optional<FluidStack> fluidStack = FluidUtil.getFluidContained(stack);
        if (fluidStack.isPresent() && !fluidStack.get().isEmpty()){
            switch (this.matchingStrategy)
            {
                case EXCEEDED:
                    return fluidStack.get().isFluidEqual(fluidStackToTest) && fluidStack.get().getAmount() <= fluidStackToTest.getAmount();

                case EXACT:
                    return fluidStack.get().isFluidStackIdentical(fluidStackToTest);
            }
        }
        return false;
    }

    public enum MatchingStrategy {
        /**
         * EXACT matching indicates that it will try matching exact amount of fluid.
         * Example: if the FluidIngredient asks for 500 mB water, a vanilla water bucket
         * will not be matched, as it holds 1000 mB water, 1000 != 500.
         */
        EXACT,

        /**
         * EXCEEDED matching is the default matching strategy, when not specified.
         * It will try matching those containers that can drain the specified amount of
         * fluid out. Example: if the FluidIngredient asks for 500 mB water, a vanilla bucket
         * will be matched.
         *
         */
        EXCEEDED,
    }
}
