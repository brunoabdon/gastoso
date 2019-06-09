package com.github.brunoabdon.heroku;

import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author bruno
 */
public class AbdApplication extends ResourceConfig{

    public AbdApplication() {
        register(new PersistenceUnitBinder());
    }
}
