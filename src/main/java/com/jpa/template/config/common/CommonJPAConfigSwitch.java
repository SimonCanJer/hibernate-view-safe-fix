package com.jpa.template.config.common;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * this class has two roles:
 *  1.is just switch for package scanning by Spring
 *  2. decorates  EntityManagerFactory, EntityManager and EntityTransaction to subclass critical methods, as instance-pospone
 *   EntityManager#close() to handle potential latency initialization exception, define compesators to current transaction
 *  3. Exposed  a bean which enables pospone and finalize operations, add decorators
 * @see ITransactionInterceptionManagement
 * Remark: the root point of the decorations is interception of bean creation and decoration of returned interfaces
 * in deep traversal. Here minimally needed interception is done.
 * In industrial version any another beans and  interfaces was intercepted
 * Any posponed operation registers any runnablke which will be called with finalization method
 * of the
 * @see ITransactionInterceptionManagement
 * @see ITransactionInterceptionManagement#activateCloseInterception(Consumer)
 * @see ITransactionInterceptionManagement#finalize()
 * @see #installHibernateEntityManagerCollisionResolver()
 *
 *
 */
@Configuration
//points on to data repositories
@EnableJpaRepositories("${data.repositories}")
//note: first ComponentScan points to indispensable entity/transaction management
//second component scan points to customized dao services
@ComponentScans({@ComponentScan("${jta.config.path}"), @ComponentScan("${dao.services}"),@ComponentScan("${jta.config.view.safe}")})
public class CommonJPAConfigSwitch {
    static class TransactionNotifier {
        Consumer<EntityTransaction> resume;
        BiConsumer<EntityTransaction, Exception> compensator;

        TransactionNotifier(Consumer<EntityTransaction> ok, BiConsumer<EntityTransaction, Exception> bi) {
            resume = ok;
            compensator = bi;
        }

        void resume(EntityTransaction t) {
            resume.accept(t);
        }

        void failed(EntityTransaction t, Exception e) {
            compensator.accept(t, e);
        }
    }

    ThreadLocal<Boolean> interceptionFlag = new ThreadLocal<>();
    ThreadLocal<Consumer<EntityManager>> closeSink = new ThreadLocal<>();
    ThreadLocal<TransactionNotifier> transactorSink = new ThreadLocal();
    static final Boolean INTERCEPT_FLAG_VALUE = true;
    ThreadLocal<Finalizers> finalizers = new ThreadLocal<>();

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
                interceptionFlag.set(false);

            }

            @Override
            public void activateCloseInterception(Consumer<EntityManager> compensator) {
                interceptionFlag.set(true);
                CommonJPAConfigSwitch.this.closeSink.set(compensator);

            }

            @Override
            public void adviceTransaction(Consumer<EntityTransaction> success, BiConsumer<EntityTransaction, Exception> compensator) {
                TransactionNotifier tn = new TransactionNotifier(success, compensator);
            }

            @Override
            public void addPosponedClose(EntityManager em, Consumer<EntityManager> closeHandler) {
                interceptionFlag.set(true);
                addFinalizingRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if(em.isOpen())
                        {
                            try
                            {
                                closeHandler.accept(em);
                            }
                            catch(Throwable e)
                            {

                            }
                            if(em.isOpen())
                            {
                                em.close();
                            }
                        }
                    }
                });

            }

            @Override
            public void finalizeInterceptedTransaction() {
                try {
                    finalizers.get().run();
                } catch (Exception e) {

                }
                interceptionFlag.set(null);

            }
        };
    }

    Object proxy(Class[] interfaces, InvocationHandler handler) {
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
            listFinalizers.stream().forEach((r) -> {
                try {
                    r.run();
                } catch (Exception e) {

                }
            });
            listFinalizers.clear();
            ;
            interceptionFlag.set(null);
            closeSink.set(null);

        }
    }

    @Autowired
    Environment mEnvironment;

    @Bean
    BeanPostProcessor installHibernateEntityManagerCollisionResolver() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

                return bean;
            }

            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {

                if (bean instanceof EntityManagerFactory) {
                    Object orig = bean;
                    bean = proxyInterceptor((EntityManagerFactory) bean);

                }
                return bean;
            }

            private Object proxyInterceptor(EntityManagerFactory bean) {

                return Proxy.newProxyInstance(CommonJPAConfigSwitch.class.getClassLoader(), new Class[]{EntityManagerFactory.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object res = method.invoke(bean, args);
                        if (method.getName().contains("createEntityM")) {
                            res = decorateEntityManager((EntityManager) res);

                        }

                        return res;
                    }

                    private Object decorateEntityManager(EntityManager em) {

                        return Proxy.newProxyInstance(CommonJPAConfigSwitch.class.getClassLoader(), new Class[]{EntityManager.class}, new InvocationHandler() {
                            @Override
                            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                                if (INTERCEPT_FLAG_VALUE.equals(interceptionFlag.get())) {
                                    if (method.getName().equals("close")) {
                                        Consumer<EntityManager> consume =closeSink.get();
                                        closeSink.remove();
                                        addFinalizingRunnable(new Runnable() {
                                            @Override
                                            public void run() {
                                                try
                                                {
                                                   consume.accept(em);
                                                }
                                                catch(Throwable e)
                                                {

                                                }
                                                if(em.isOpen())
                                                    em.close();
                                            }
                                        });
                                        return null;
                                    }

                                }
                                Object res = method.invoke(em, args);
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


            private Object decorateTransaction(EntityTransaction transactor) {

                return proxy(new Class[]{EntityTransaction.class}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        if (!method.getName().equals("commit") || null == transactorSink.get()) {
                            return method.invoke(transactor, args);
                        }
                        Object res=null;
                        try {
                            res = method.invoke(transactor, args);
                            transactorSink.get().resume(transactor);
                        } catch (Exception e) {
                            transactorSink.get().failed(transactor, e);
                        }
                        transactorSink.remove();

                        return res;
                    }

                    ;
                });
            }

            ;
        };

    }


}
