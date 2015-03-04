package org.jboss.arquillian.junit.suite.resolve;

import java.util.ArrayList;
import java.util.List;

public class ClassResolverStrategy implements ResolveStrategy {

    @Override
    public List<Class<?>> resolve(String[] values) {
        ClassLoader cl = ClassResolverStrategy.class.getClassLoader();
        List<Class<?>> result = new ArrayList<Class<?>>();
        try {
            for(String value : values) {
                result.add(cl.loadClass(value));
            }
            
        } catch(Exception e) {
            throw new RuntimeException("Could not resolve classes: " + values, e);
        }
        return result;
    }

}
