package org.jboss.arquillian.junit;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

class ArquillianTestRunner extends BlockJUnit4ClassRunner {

    public ArquillianTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }
    
}
