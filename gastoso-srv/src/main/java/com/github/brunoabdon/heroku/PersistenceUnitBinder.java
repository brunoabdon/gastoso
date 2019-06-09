package com.github.brunoabdon.heroku;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUnit;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;


/**
 *
 * @author bruno
 */
class PersistenceUnitBinder extends AbstractBinder {

    @Singleton
    private static class PersistenceUnitInjectionResolver implements InjectionResolver<PersistenceUnit> {

        private final Map<String,EntityManagerFactory> cache = new HashMap<>();
        
        @Override
        public Object resolve(final Injectee injectee, final ServiceHandle<?> root) {
            if (!injectee.getRequiredType().equals(EntityManagerFactory.class)) {
                return null;
            }
            
            final PersistenceUnit annotation = 
                injectee.getParent().getAnnotation(PersistenceUnit.class);
            
            final String unitName = annotation.unitName();
            
            EntityManagerFactory emf = cache.get(unitName);
            if(emf == null){
    
                final Map<String, String> properties = HerokuUtils.getEMFEnvProperties();

                emf = Persistence.createEntityManagerFactory(unitName, properties);        

    //            final ThreadLocalInvoker<EntityManagerFactory> tliemf = 
    //                new ThreadLocalInvoker();
    //            
    //            tliemf.set(emf);
    //            final EntityManagerFactory proxy = (EntityManagerFactory)Proxy.newProxyInstance(
    //                    this.getClass().getClassLoader(),
    //                    new Class[] {EntityManagerFactory.class},
    //                    tliemf);
    //            
                cache.put(unitName, emf);
            }
            return emf;
        }

        @Override
        public boolean isConstructorParameterIndicator() {
            return false;
        }

        @Override
        public boolean isMethodParameterIndicator() {
            return false;
        }
    }


    @Override
    protected void configure() {
        bind(PersistenceUnitInjectionResolver.class)
            .to(new TypeLiteral<InjectionResolver<PersistenceUnit>>() {})
            .in(Singleton.class);
    }
}