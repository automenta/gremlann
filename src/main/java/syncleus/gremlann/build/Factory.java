package syncleus.gremlann.build;

/**
 * An IoC factory that knows how to create instances.
 * 
 * @author sergio.oliveira.jr@gmail.com
 */
public interface Factory {
	
	/**
	 * Returns an instance. Creates one if necessary.
	 * 
	 * @return an instance
	 */
	public <T> T getInstance();
	
	
	/**
	 * Return the type of objects that this factory disposes.
	 * 
	 * @return the type of objects returned by this factory.
	 */
	public Class<?> getType();
}