package loordgek.itemhandlerv2.itemhandler;


import loordgek.itemhandlerv2.filter.IStackFilter;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.Iterator;

public interface IItemHandler extends Iterable<ItemStack>{

    int size();

    @Nonnull
    ItemStack getStackInSlot(int slot);

    int getSlotLimit(int slot);

    @Override
    default Iterator<ItemStack> iterator() {
        return new Iterator<ItemStack>() {
            int index;
            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public ItemStack next() {
                ItemStack stack = getStackInSlot(index);
                index++;
                return stack;
            }
        };
    }

    ITransaction insert(int slot, ItemStack stack, boolean force);

    ITransaction insert(ItemStack stack);

    ITransaction extract(int slot, int amount);

    ITransaction extract(IStackFilter filter, int amount);}
