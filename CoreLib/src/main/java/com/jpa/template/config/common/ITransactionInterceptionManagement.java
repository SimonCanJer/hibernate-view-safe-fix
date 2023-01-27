package com.jpa.template.config.common;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Interface which accepts requests for transaction and entity manager interceptions
 */
public interface ITransactionInterceptionManagement {
    /**
     * just remove any interceptions related to the current thread.
     */
    void byPassInterception();

    /**
     * Keep in mind that the nearest call of EntityManager#close must be intercepted and postponed
     *
     * @param onClosed consumer handling close operation on finalization
     * @see #finalizeInterceptedPostponedOperations()
     */
    void activateSessionCloseInterceptor(Consumer<EntityManager> onClosed);

    void applyEMInterceptionHooks(BiFunction<EntityManager, Throwable, Boolean> onError, Consumer<EntityManager> onClose);

    /**
     * the method called to set handlers of nearest transaction commit's  success and failed cases
     * (for current thread)
     *
     * @param success     - any operation on success of nearest transaction
     * @param compensator - compensator of failed transaction
     *                    Note, that finalizer will remove all the settings
     * @see #finalizeInterceptedPostponedOperations()
     */
    void setTransactionEndHandler(Consumer<EntityTransaction> success, BiConsumer<EntityTransaction, Exception> compensator);

    /**
     * Postpone close() method  for current EntityManager on current thread
     *
     * @param em
     * @param closeHandler
     */
    void addPosponedClose(EntityManager em, Consumer<EntityManager> closeHandler);

    /**
     * Finalize all postponed operations on current thread.
     * Called authomatically by filter, no needs to call this method directly
     */
    void finalizeInterceptedPostponedOperations();

}
