package net.minecraftforge.container;

import net.minecraft.util.Tuple;
import net.minecraftforge.container.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CombiningReadWriteContainer<T> extends CombiningReadOnlyContainer<T> implements IReadWriteContainer<T> {

    private List<IReadWriteContainer<T>> readWriteContainers;
    private IContainerTransaction<T> activeTransaction;

    public CombiningReadWriteContainer(List<IReadWriteContainer<T>> iReadWriteContainers) {
        super(new ArrayList<>(iReadWriteContainers));
        this.readWriteContainers = iReadWriteContainers;
    }

    /**
     * Begins a new transaction for this container.
     * <p>
     * Either call {@link IContainerTransaction#commit()}
     * to commit the transaction and make it live, or call {@link IContainerTransaction#cancel()}
     * to cancel the transaction.
     *
     * @return The transaction object to handle the transaction.
     */
    @Override
    public IContainerTransaction<T> beginTransaction() {
        return null;
    }

    /**
     * Checks if the given transaction is the active one.
     *
     * @param transactionToCheck The transaction to check.
     * @return True when the given transaction is active and can be commited, false when not.
     */
    @Override
    public boolean isActiveTransaction(IContainerTransaction<T> transactionToCheck) {
        return false;
    }

    public class CombiningContainerTransaction<T> extends CombiningReadOnlyContainer<T> implements IContainerTransaction<T>
    {

        private final CombiningReadWriteContainer<T> readWriteContainer;
        private final List<IContainerTransaction<T>> internalTransactionHandlers;

        public CombiningContainerTransaction(final CombiningReadWriteContainer<T> readWriteContainer) {
            super(readWriteContainer.readWriteContainers.stream().map(IReadWriteContainer::beginTransaction).collect(Collectors.toList()));
            this.readWriteContainer = readWriteContainer;
            this.internalTransactionHandlers = super.readOnlyContainers.stream().map(container -> (IContainerTransaction<T>) container).collect(Collectors.toList());
        }

        /**
         * Cancels the current transaction.
         */
        @Override
        public void cancel() {
            if (readWriteContainer.isActiveTransaction(this))
                readWriteContainer.activeTransaction = null;

            internalTransactionHandlers.forEach(IContainerTransaction::cancel);
        }

        /**
         * Attempts to commit the transaction.
         * If this method is called on a not active transaction, indicated by {@link IReadWriteContainer#isActiveTransaction(IContainerTransaction)}
         * being false, then an exception is thrown.
         *
         * @throws TransactionNotValidException When this transaction is not the active transaction.
         */
        @Override
        public void commit() throws TransactionNotValidException {
            if (!readWriteContainer.isActiveTransaction(this))
                throw new TransactionNotValidException(readWriteContainer, this);

            for (IContainerTransaction<T> internalTransactionHandler : internalTransactionHandlers) {
                internalTransactionHandler.commit();
            }
        }

        /**
         * Attempts to insert the given instance into the slot of this container.
         *
         * @param slot     The slot to insert into.
         * @param toInsert The object to insert.
         * @return An instance of {@link IContainerTransactionOperationResult} that indicates success or failure, and provides results.
         */
        @Override
        public IContainerTransactionOperationResult<T> insert(int slot, T toInsert) {
            final Tuple<Integer, Integer> targets = calculateInternalSlotInformationFromSlotIndex(slot);
            return this.internalTransactionHandlers.get(targets.getFirst()).insert(targets.getSecond(), toInsert);
        }

        /**
         * Attempts to extract a given amount from the slot of this container.
         *
         * @param slot   The slot to extract from.
         * @param amount The amount to extract.
         * @return An instance of {@link IContainerTransactionOperationResult} that indicates success or failure, and provides results.
         */
        @Override
        public IContainerTransactionOperationResult<T> extract(int slot, int amount) {
            final Tuple<Integer, Integer> targets = calculateInternalSlotInformationFromSlotIndex(slot);
            return this.internalTransactionHandlers.get(targets.getFirst()).extract(targets.getSecond(), amount);
        }

        /**
         * The container which is being manipulated, once {@link #commit()} is called.
         *
         * @return The container.
         */
        @Override
        public IReadWriteContainer<T> getContainer() {
            return readWriteContainer;
        }
    }
}
