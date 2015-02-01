/**
 * ScriptEngineManager.java
 * (c) Radim Loskot and Radek Burget, 2013-2014
 *
 * ScriptBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * ScriptBox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *  
 * You should have received a copy of the GNU Lesser General Public License
 * along with ScriptBox. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.jsen.core;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import com.jsen.core.annotation.ScriptEngineFactory;
import com.jsen.core.misc.MimeContentRegistryBase;

public class ScriptEngineManager extends MimeContentRegistryBase<AbstractScriptEngineFactory, AbstractScriptEngine> {
	static private ScriptEngineManager instance = null;
	
	public static synchronized ScriptEngineManager getInstance() {
		if (instance == null) {
			instance = new ScriptEngineManager();
			
			instance.registerScriptEngineFactories();
		}
		
		return instance;
	}
	
	/**
	 * Constructs new script engine for given MIME type with given script settings.
	 * 
	 * @param mimeType MIME type of which script engine should be returned.
	 * @param settings Settings that should be passed into constructed script engine.
	 * @return New script engine factory if there is any for given MIME type, otherwse null.
	 */
	public synchronized AbstractScriptEngine getBrowserScriptEngine(String mimeType, ScriptSettings settings) {
		return getContent(mimeType, settings);
	}
	
	@Override
	public synchronized AbstractScriptEngine getContent(String mimeType, Object... args) {
		/*
		 * We are accessing the first script engine, so this manager should be already fully instatized.
		 * We can lazily register injectors now.
		 */
		if (instance != null) {
			registerScriptContextInjectors();
		}
		
		return super.getContent(mimeType, args);
	}
	
	@SuppressWarnings("unchecked")
	private void registerScriptEngineFactories() {
	    List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
	    classLoadersList.add(ClasspathHelper.contextClassLoader());
	    classLoadersList.add(ClasspathHelper.staticClassLoader());
	    Reflections reflections = new Reflections(new ConfigurationBuilder()
	            .setScanners(new SubTypesScanner(), new TypeAnnotationsScanner(), new ResourcesScanner())
	            .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
	            .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix("com.jsen"))));
		
	    Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(ScriptEngineFactory.class);
		
		for (Class<?> clazz : annotated) {
			if (AbstractScriptEngineFactory.class.isAssignableFrom(clazz)) {
				registerMimeContentFactory((Class<AbstractScriptEngineFactory>)clazz);	
			}
		}

		
		
	}
	
	
	private void registerScriptContextInjectors() {
		/*registerScriptContextInjector(new URLInjector());
		registerScriptContextInjector(new XMLHttpRequestInjector());*/
	}
	
	/*private void registerScriptContextInjector(ScriptContextInjector injector) {
		injector.registerScriptContextInject();
	}*/
}
