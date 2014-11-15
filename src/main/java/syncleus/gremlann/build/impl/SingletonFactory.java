package syncleus.gremlann.build.impl;

import syncleus.gremlann.build.Factory;

public class SingletonFactory implements Factory {
	
	private final Object instance;
	
	private final Class<?> type;
	
	
	public SingletonFactory(Object instance) {

		this.instance = instance;
		
		this.type = instance.getClass();
	}
	
	@Override
	public <T> T getInstance()  {
		
		return (T) instance;
	}
	
	@Override
	public Class<?> getType() {
		
		return type;
	}
}