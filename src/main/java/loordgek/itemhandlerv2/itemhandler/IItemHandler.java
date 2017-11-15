package loordgek.itemhandlerv2.itemhandler;


import com.google.common.collect.Range;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

public interface IItemHandler{

    int size();

    default boolean isStackValid(@Nonnull ItemStack stack) {
        return true;
    }

    default boolean canExtractStack(@Nonnull ItemStack stack) {
        return true;
    }

    default IItemHandlerIterator itemhandlerIterator(boolean skipEmpty) {
        return new ItemHandlerIterator(this, skipEmpty);
    }

    /**
     * @return A Integer in the range [0,scale] representing how "full" this inventory is.
     */
    //todo give me a better name
    default float calcRedStoneFromInventory(int scale, boolean ignoreStackSize) {
        if (ignoreStackSize){
            float proportion = 0.0F;

            for (int i = 0; i < size(); i++) {
                if (!getStackInSlot(i).isEmpty()){
                    proportion++;
                }
            }

            proportion = proportion / (float) size();

            return (proportion * scale);
        }
        else {
            float proportion = 0.0F;

            for (int j = 0; j < size(); ++j) {
                ItemStack itemstack = getStackInSlot(j);

                if (!itemstack.isEmpty()) {
                    proportion += (float) itemstack.getCount() / (float) getStackLimit(itemstack);
                }
            }

            proportion = proportion / (float) size();

            return (proportion * scale);
        }
    }

    int getSlotLimit();

    default int getStackLimit(ItemStack stack) {
        return Math.min(stack.getMaxStackSize(), getSlotLimit());
    }

    default int getFreeSpaceForSlot(int slot){
        ItemStack existing = getStackInSlot(slot);
        if (!existing.isEmpty()){
            if (!existing.isStackable()){
                return 0;
            }
            else return getSlotLimit() - existing.getCount();
        }
        return getSlotLimit();
    }

    @Nonnull
    ItemStack getStackInSlot(int slot);

    /**
     * @param slotRange
     * @param stack
     * @param simulate
     * @return
     */
    @Nonnull
    InsertTransaction insert(Range<Integer> slotRange, ItemStack stack, boolean simulate);

    /**
     * @param slotRange
     * @param filter
     * @param amount
     * @param simulate
     * @return
     */
    @Nonnull
    ItemStack extract(Range<Integer> slotRange, Predicate<ItemStack> filter, int amount, boolean simulate);
}