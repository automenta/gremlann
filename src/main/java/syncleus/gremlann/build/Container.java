package syncleus.gremlann.build;


/**
 * A IoC container that provides:
 * <ul>
 * <li>Programmatic Configuration</li>
 * <li>Bean Instantiation through constructors</li>
 * <li>Bean Initialization through setters</li>
 * <li>Dependency Injection through constructors</li>
 * <li>Dependency Injection through setters</li>
 * <li>Auto-wiring through constructors and setters (very simple!)</li>
 * <li>Injection through setters so you can populate any external object with objects from the container</li>
 * <li>Instantiation through constructors so you can instantiate any external class with objects from the container</li>
 * <li>Support for SINGLETON and THREAD scopes, plus you can easily get REQUEST and SESSION scopes for web projects</li>
 * <li>Generic Factories so you can easily turn anything into a object factory</li>
 * <li>Interceptors for factories: onCreated, onCleared, useful for object pooling</li> 
 * </ul>
 *
 * @author sergio.oliveira.jr@gmail.com
 *
 */
public interface Container {
	
	/**
	 * Get an instance from the container by using the associated factory.
	 * 
	 * The instance will be fully initialized (through constructor and/or setters) and fully wired (all dependencies will be resolved).
	 * 
	 * @param key The key representing the factory to use. The name of the bean in the container.
	 * @return The fully initialized and wired bean.
	 */
	public <T> T get(Object key);
	
	/**
	 * Get the type of the instances returned by the associated factory.
	 * 
	 * @param key The factory
	 * @return The type returned by this factory
	 */
	public Class<?> getType(Object key);
	
	/**
	 * Configure a bean to be returned with the given implementation when {@link #get(String)} is called.
	 * An internal factory will be used.
	 * 
	 * @param key The key representing the bean to return. The name of the bean in the container.
	 * @param klass The class used to instantiate the bean, in other words, its implementation.
	 * @param scope The scope of the factory.
	 * @return The factory created as a ConfigurableFactory. (Fluent API)
	 * @see Scope
	 */
	public ConfigurableFactory use(Object key, Class<?> klass, Scope scope);
	
	/**
	 * Same as {@link #ioc(String, Class, Scope)} except that it assumes
	 * there is no scope (Scope.NONE).
	 * 
	 * @param key
	 * @param klass
	 * @return The factory created as a ConfigurableFactory. (Fluent API)
	 * @see Scope
	 */
	public ConfigurableFactory use(Object key, Class<?extends Object> klass);
	
	/**
	 * Set up a factory for the given key. The scope assumed is NONE.
	 * 
	 * @param key The key representing the bean to return. The name of the bean in the container.
	 * @param factory The factory for the IoC.
	 * @return The factory passed as a parameter. (Fluent API)
	 * @see Factory
	 */
	public Factory use(Object key, Factory factory);
	
	/**
	 * Set up a factory for the given key in the given scope.
	 * 
	 * @param key The key representing the bean to return. The name of the bean in the container.
	 * @param factory The factory for the IoC.
	 * @param scope The scope used by the factory.
	 * @return The factory passed as a parameter (Fluent API).
	 * @see Factory
	 * @see Scope
	 */
	public Factory use(Object key, Factory factory, Scope scope);
	
	/**
	 * Configure a bean dependency to be auto-wired by the container.
	 * It wires by constructor and by setter. By constructor is uses the type of sourceFromContainer. By setter it assumes the property is also named sourceFromContainer.
	 * 
	 * @param sourceFromContainer The bean inside the container that will be wired automatically inside any other bean the depends on it.
	 */
	public void useAuto(Object sourceFromContainer);
	
	/**
	 * Configure a bean dependency to be auto-wired by the container.
	 * It wires by constructor and by setter. By constructor is uses the type of sourceFromContainer. By setter it looks for a property with the given name and try to inject.
	 * 
	 * @param sourceFromContainer The bean inside the container that will be wired automatically inside any other bean the depends on it.
	 * @param property The name of the property to inject, whey trying auto-wiring by setter.
	 */
	public void useAuto(Object sourceFromContainer, String property);
	
	/**
	 * Take a given bean and populate its properties with other beans coming from this container. 
	 * You basically checking properties of the given bean and looking for values inside the container.
	 * And injecting in the given bean, in other words, populating it.
	 * 
	 * @param bean The bean to be populated with other beans from the container.
	 */
	public void inject(Object bean);
	
	/**
	 * Construct an instance using beans from the container. A constructor will be chosen that has arguments that can be found 
	 * inside the container.
	 * 
	 * @param klass The class that should be instantiated.
	 * @return An instantiated bean.
	 */
	public <T> T get(Class<?> klass);
	
	/**
	 * Check whether the container currently has a value for this key. For example,
 if it is a singleton AND someone has requested it, the container will have it cached.
 The method is useful to contains for an instance without forcing her creation.
	 * 
	 * @param key The key representing the bean inside the container.
	 * @return true if the container has an instance cached in the scope for this key
	 */
	public boolean contains(Object key);
	
	/**
	 * Clear all cached instances for that scope. If you have a thread-pool for example you will
 want to remove the THREAD scope when your thread is returned to the pool. Because you have a thread
 pool you will have the SAME thread handling different requests and each request will need its own instances
 from the container. Therefore, each time you are done with a thread and it is returned to your thread-pool
 you can call remove to release the instances allocated and cached by the container. A web container and/or framework
 can use this feature to implement a REQUEST scope which is nothing more than the THREAD scope with remove. If the web
 container was not using a thread-pool, the THREAD scope would be equal to the REQUEST scope as each request would
 always be handled by a different thread.
  
 It does not make sense to remove a NONE scope (the method returns doing nothing). You can remove a SINGLETON scope if necessary.
	 * 
	 * @param scope The scope to be cleared.
	 */
	public void remove(Scope scope);
	
	/**
	 * Clear a single key from cache and return the instance that was cached.
	 * 
	 * @param key The key representing the bean inside the container.
	 * @return The value that was cached and it is not anymore (was cleared) or null if nothing was cleared
	 */
	public <T> T remove(Object key);
}
