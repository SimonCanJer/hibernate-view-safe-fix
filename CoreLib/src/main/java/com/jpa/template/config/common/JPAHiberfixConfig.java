package com.jpa.template.config.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * this class has two roles:
 * 1.it directs to packages to be also scanned by Spring
 * 2. decorates  EntityManagerFactory, EntityManager and EntityTransaction to subclass critical methods, as instance-pospone
 * EntityManager#close() to handle potential latency initialization exception, define compesators to current transaction
 * 3. Exposes  a bean which enables pospone and finalize operations by means of decorations
 * <p>
 * Remark: the root point of the decorations is interception of bean creation and decoration of returned interfaces
 * in deep traversal. Here minimally needed interception is done.
 * Any posponed operation registers a runnable which will be called by finalization method
 * of the {@link ITransactionInterceptionManagement}, {@link ITransactionInterceptionManagement#activateSessionCloseInterceptor(Consumer)}
 */
@Configuration
//points on to data repositories

//note: first ComponentScan points to  entity/transaction management, it is not a compulsory part,so it can be a dummy
//second component scan points to customized dao services
@ComponentScans({@ComponentScan("com.jpa.template.config.finalization")})
@EnableAutoConfiguration
@SuppressWarnings({"rawTypes", "unused"})
@Slf4j
public class JPAHiberfixConfig {

    static class TransactionNotifier {
        /**
         * Handle transaction end
         */
        Consumer<EntityTransaction> resume;

        /**
         * compenensation action when transaction failed
         */
        BiConsumer<EntityTransaction, Exception> compensator;

        TransactionNotifier(Consumer<EntityTransaction> ok, BiConsumer<EntityTransaction, Exception> bi) {
            resume = ok;
            compensator = bi;
        }

        /**
         * resumes transaction on its end
         *
         * @param t - transaction object
         */
        void resume(EntityTransaction t) {
            resume.accept(t);
        }

        /**
         * called on failure
         *
         * @param t - transaction object.
         * @param e - exception, which occured
         */

        void failed(EntityTransaction t, Exception e) {
            compensator.accept(t, e);
        }
    }

    /**
     * This class is a container for operation, which are to  be called on the boundaries
     * of {@link EntityManager} lifetime
     */
    static class EntityManagerInterception {
        /**
         * to be called when closing  entity manager is closed by operation of the end of hook
         **/
        private Consumer<EntityManager> onClose;
        /**
         * to be called when an error ocuured
         **/
        private BiFunction<EntityManager, Throwable, Boolean> onError;
        private boolean postpone = false;

    }

    /**
     * keeps interception operations for EntityManager, which must be called
     * when this entity manager is really closed (not its proxy)
     */
    private final ThreadLocal<EntityManagerInterception> currentEMInterception = new ThreadLocal<>();

    /**
     * Transaction sink for the current thread
     **/
    private final ThreadLocal<TransactionNotifier> transactorSink = new ThreadLocal<>();

    private final ThreadLocal<Finalizers> finalizers = new ThreadLocal<>();

    void addFinalizingRunnable(Runnable r) {
        Finalizers reg = finalizers.get();
        if (reg == null) {
            finalizers.set(reg = new Finalizers());
            reg.add(r);
        }

    }

    @Bean
    ITransactionInterceptionManagement transactionInterceptionManagement() {
        return new ITransactionInterceptionManagement() {

            @Override
            public void byPassInterception() {

                currentEMInterception.remove();

            }

            @Override
            public void activateSessionCloseInterceptor(Consumer<EntityManager> finalizer) {

                handleFinalizerAdd(finalizer);

            }


            @Override
            public void applyEMInterceptionHooks(BiFunction<EntityManager, Throwable, Boolean> onError, Consumer<EntityManager> onClose) {
                EntityManagerInterception emi = handleFinalizerAdd(onClose);
                emi.onError = onError;
            }

            /**
             * implements related interfaced method by means of creating container object, which keeps
             * handlers to apply on transaction demarkation methods
             * @param success     - any operation on success of nearest transaction
             * @param compensator - compensator of failed transaction
             *                    Note, that finalizer will remove all the settings
             */
            @Override
            public void setTransactionEndHandler(Consumer<EntityTransaction> success, BiConsumer<EntityTransaction, Exception> compensator) {
                transactorSink.set(new TransactionNotifier(success, compensator));

            }

            @Override
            public void addPosponedClose(EntityManager em, Consumer<EntityManager> closeHandler) {

                addFinalizingRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (em.isOpen()) {
                            try {
                                closeHandler.accept(em);
                            } catch (Throwable e) {
                                log.warn("an error occured in a handler when closing EM {}", e.getMessage());
                            }
                            if (em.isOpen()) {
                                em.close();
                            }
                        }
                    }
                });
            }

            @Override
            public void finalizeInterceptedPostponedOperations() {
                try {
                    finalizers.get().run();
                } catch (Exception e) {
                    log.error("An error occured when running finalizers of Entity Manager {}", e.getMessage());
                }
                //remove all finalizers for session, which is associated with this thread
                finalizers.remove();
                currentEMInterception.remove();
            }

            private EntityManagerInterception handleFinalizerAdd(Consumer<EntityManager> finalizer) {
                EntityManagerInterception emi = currentEMInterception.get();
                if (emi == null) {
                    currentEMInterception.set(emi = new EntityManagerInterception());
                }
                emi.postpone = true;
                emi.onClose = finalizer;
                return emi;
            }

        };

    }

    Object proxy(Class<?>[] interfaces, InvocationHandler handler) {
        return Proxy.newProxyInstance(getClass().getClassLoader(), interfaces, handler);

    }

    class Finalizers implements Runnable {
        List<Runnable> listFinalizers = new ArrayList<>();

        Finalizers add(Runnable r) {
            listFinalizers.add(r);
            return this;
        }

        @Override
        public void run() {
            listFinalizers.forEach((r) -> {
                try {
                    r.run();
                } catch (Exception e) {

                    log.error("Error in finalizers {} ", e.getMessage());
                }
            });
            listFinalizers.clear();
            currentEMInterception.remove();
        }
    }


    /**
     * Installing {@link EntityManager} creation hook.
     *
     * @return instance of the {@link BeanPostProcessor} , which is instructed to intercept
     * entity manager methods to postpone the "close()" method
     */
    @Bean
    BeanPostProcessor entityManagerProblemResolver() {

        return new BeanPostProcessor() {
            @Value("${em.hiberfix:true}")
            Boolean isHiberfixOn;

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }

            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

                if (!isHiberfixOn)
                    return bean;
                if (bean instanceof EntityManagerFactory) {
                    Object orig = bean;
                    bean = generateEmProxyInterceptor((EntityManagerFactory) bean);
                }
                return bean;
            }

            /**
             * Install interceptor on entity manager factory (proxy wrapper), and then
             * install interceptors on EntityManager, upon it is created by the class.
             * @param bean original bean
             * @return decorator
             */
            private Object generateEmProxyInterceptor(EntityManagerFactory bean) {


                return Proxy.newProxyInstance(JPAHiberfixConfig.class.getClassLoader(), new Class[]{EntityManagerFactory.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object res = method.invoke(bean, args);
                        if (method.getName().contains("createEntityM")) {
                            res = decorateEntityManager((EntityManager) res);
                        }

                        return res;
                    }

                    /**
                     *
                     * @param em original engity manager
                     * @return decorator.
                     */
                    private Object decorateEntityManager(EntityManager em) {

                        return Proxy.newProxyInstance(JPAHiberfixConfig.class.getClassLoader(), new Class[]{EntityManager.class}, new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                                final EntityManagerInterception interceptorDef = currentEMInterception.get();
                             /* if interceptor of entity manager for close is defined, then bypass now
                                and postpone the method call */
                                if (interceptorDef != null) {
                                    //if EntityManager is being closed,
                                    // then prevent it is called immediatelly.
                                    if (method.getName().equals("close")) {
                                        currentEMInterception.remove();//and add a runnable to be ran
                                                                       //by finalizers.
                                        addFinalizingRunnable(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    if (interceptorDef.onClose != null)
                                                        interceptorDef.onClose.accept(em);
                                                } catch (Throwable e) {
                                                    log.warn("The  error occured while calling handler of em.close() ");
                                                    if (interceptorDef.onError != null) {
                                                        interceptorDef.onError.apply(em, e);
                                                    }
                                                }
                                                if (em.isOpen())
                                                    em.close();
                                            }
                                        });
                                        return null;
                                    }
                                }
                                Object res = null;
                                try {
                                    res = method.invoke(em, args);
                                } catch (Throwable t) {
                                    boolean doNotPropagate = false;
                                    if (interceptorDef.onError != null) {
                                        try {
                                            doNotPropagate = interceptorDef.onError.apply(em, t);

                                        } catch (Throwable ti) {
                                        }
                                        if (!doNotPropagate)
                                            throw t;
                                    }
                                }
                                if (method.getName().equals("getTransaction")) {
                                    if (null != transactorSink.get()) {
                                        res = decorateTransaction((EntityTransaction) res);
                                    }
                                }
                                return res;
                            }
                        });
                    }
                });
            }

            /**
             * Decorates transaction by adding handlers
             * @param transactor  - {@link EntityManager to be decorated}
             * @return the proxy, which is built
             */
            private Object decorateTransaction(EntityTransaction transactor) {

                return proxy(new Class[]{EntityTransaction.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        if (!method.getName().equals("commit") || null == transactorSink.get()) {
                            return method.invoke(transactor, args);
                        }
                        Object res = null;
                        try {
                            res = method.invoke(transactor, args);
                            transactorSink.get().resume(transactor);
                        } catch (Exception e) {
                            transactorSink.get().failed(transactor, e);
                        }
                        transactorSink.remove();
                        return res;
                    }
                });
            }
        };
    }
}
