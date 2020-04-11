# hibernate-view-safe-fix
The example demonstrates an approach to fix LazyInitializationException and other similar cases, which is based on
 subclassing EntityManager and closing after request have passed Controller->View part of processing 
 - The approach is very simple: LazyInitializationException  is caused due to Hibernate session have been closed and proxy object detached after an operation is executed, but the returned proxy objects are used in Controller and View layer.
  Automation of Spring porvides hiden initialization and exposing EntityManagerFactoryBean, which produces EntityManager instances.
  Spring  JpaRepositories uses EntityManager on lower level of its interfaces methods execution and it closes it
   after related method of JpaRepository have been called. Closing the entity manager leads to closing Hibernate session.  The same mess will
    be happened in EJB when trying use the retuende data objects on View level.
   Anyway, after EntityManageris closes underlaying Hibernate session is closed also and all proxy objects are detached and any attempt to of operation, which requires a database hit will cause laxy initialation exception
    Reattachment is a worst of can be done because kills performance of databases.
 Another approach uses tricks of Hibernate JPA definition (see https://thoughts-on-java.org/lazyinitializationexception/?ck_subscriber_id=662673854).
  It is a good approach, but from point of view of system design and problem encapsulation it leads to delegation of a proble, which was birthed oustide Hibernate (Container behavior) to Hibernate. Is  a good working data model with properly working annotations must be extended because of external causes? Where are O-L of  the SOLID? 
 - We propose another approach: problem must be solved by those, who has created it, but using proper moment in environment. Delegate pattern togetherwith chain of responsibilties will work  fine. This way, we will subsclass the EntityManager and prevent call of its close() method where it is not desired. Instead of this, we will delegate the close operation to a component, which is called, for sure after all the possible usage of returned data objects are behind. The best place for this is a simple filter, and, preciselly more- after doFilter of filter chain is called. If we have an internal list to of delegate to run on this stage, then the task is solved safely, without involving Hibernate into external problems.  
 - Fortunately Spring and Java provide the way to build the simple construction. EntityManager is produced by EntityManagerFactory, it is an interface and can be subclassed by Proxy even without CGLib. EntityManagerFactory is a bean and we can intercept its initialization using related Spring API's event BeanPostPorcessor (see CommonJPAConfigSwitch.installHibernateEntityManagerCollisionResolver()). 
 We catch the method call, which produces the EntityManager interface and on this stage, we decorate the EntityManager instance for "close()" interception and delegating the method call to another part  of system, which will be called on another layer,when the mentioned proxy object are really unnecessary. 
 The stage is  after a servlet (the DispatchServlet in Spring case) processed the request, so a filter is a good place to finalize postponed calls of entity manager.
 -At the same time we referred Spring to the configurer com.jpa.template.config.externals.view.postbuild.ConfigEntityTransactionFinal which installs the previously announced filter. The filter just  calls the interface (autowired bean)ITransactionInterceptionManagement, to finalize all works are lelated ton the current brequest. The interface is implemented and exported as a bean in the CommonJPAConfigSwitch. It exposes functionality to signalize about necessity to intercept the close() method of EnitityManager,finalize postponned operations and even provide a consumer which handles the event. The finalization method called (in the filter, particularly), to complete all postponed operations for a thread, which is associated with the current request  

The secondary goals are 
- demonstrate techniques of  routing configuration component scan routing which is managed by properties
- demonstrated techniques of deep subclassing using Spring events of component creation and initialization

