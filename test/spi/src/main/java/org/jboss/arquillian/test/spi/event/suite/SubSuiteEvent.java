package org.jboss.arquillian.test.spi.event.suite;

import java.util.ArrayList;
import java.util.List;


public class SubSuiteEvent extends SuiteEvent {

    private SubSuiteClass subSuiteClass;

    public SubSuiteEvent(SubSuiteClass subSuiteClass) {
        this.subSuiteClass = subSuiteClass;
    }

    public SubSuiteClass getSubSuiteClass() {
        return subSuiteClass;
    }

    public boolean hasSubSuite() {
        return subSuiteClass != null;
    }

    public static class SubSuiteClass {
        private SubSuiteClass parent;
        private Class<?> suiteClass;
        private List<SubSuiteClass> children;

        private SubSuiteClass(Class<?> suiteClass) {
            this(null, suiteClass);
        }

        private SubSuiteClass(SubSuiteClass parent, Class<?> suiteClass) {
            this.children = new ArrayList<SubSuiteClass>();
            this.parent = parent;
            this.suiteClass = suiteClass;
        }

        public boolean hasParent() {
            return parent != null;
        }

        public Class<?> getSuiteClass() {
            return suiteClass;
        }

        public SubSuiteClass getParent() {
            return parent;
        }

        public List<SubSuiteClass> getChildren() {
            return new ArrayList<SubSuiteClass>(children);
        }

        private void addChild(SubSuiteClass childSuite) {
            this.children.add(childSuite);
        }

        public List<Class<?>> getSubSuiteChain() {
            List<Class<?>> all = new ArrayList<Class<?>>();
            all.add(suiteClass);
            SubSuiteClass parent = this;
            while((parent = parent.getParent()) != null) {
                all.add(parent.getSuiteClass());
            }
            return all;
        }

        SubSuiteClass createChild(Class<?> suiteClass) {
            SubSuiteClass suite = new SubSuiteClass(this, suiteClass);
            this.addChild(suite);
            return suite;
        }

        public static SubSuiteClass of(SubSuiteClass parent, Class<?> suiteClass) {
            if(parent == null) {
                return new SubSuiteClass(suiteClass);
            }
            return parent.createChild(suiteClass);
        }
    }
}
