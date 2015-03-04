package org.jboss.arquillian.junit.suite.resolve;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PackageResolverStrategy implements ResolveStrategy {

    private final Pattern DEFAULT_PATTERN = Pattern.compile(".*");
    
    @Override
    public List<Class<?>> resolve(String[] packages) {
        final ClassLoader cl = PackageResolverStrategy.class.getClassLoader();
        
        final List<Class<?>> result = new ArrayList<Class<?>>();
        for(final String pack : packages) {
            boolean recursive = false;
            String[] packExp = pack.split("\\|");

            String packageName = packExp[0];
            if(packageName.endsWith("*")) {
                recursive = true;
                packageName = packageName.substring(0, packageName.length()-1);
            }
            Pattern pattern = DEFAULT_PATTERN;
            if(packExp.length == 2) {
                pattern = Pattern.compile(packExp[1]);
            }
            final Pattern classPattern = pattern;
            
            URLPackageScanner.newInstance(recursive, cl, new URLPackageScanner.Callback() {
                @Override
                public void classFound(String className) {
                    try {
                        if(classPattern.matcher(className).find()) {
                            result.add(cl.loadClass(className));
                        }
                    } catch(Exception e) {
                        throw new RuntimeException("Could not resolve class: " + className, e);
                    }
                }
            }, packageName).scanPackage();
        }
        return result;
    }

}
