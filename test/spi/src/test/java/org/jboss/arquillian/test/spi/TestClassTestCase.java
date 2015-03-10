package org.jboss.arquillian.test.spi;


import org.junit.Assert;
import org.junit.Test;

public class TestClassTestCase {

   @Test
   public void shouldNotBeSuiteIfNoChildren() {
      TestClass root = new TestClass(Root.class);

      Assert.assertFalse(root.isSuite());
   }

   @Test
   public void shouldBeSuiteIfChildren() {
      TestClass root = new TestClass(Root.class);
      TestClass subRoot = new TestClass(root, SubRoot.class);

      Assert.assertTrue(root.isSuite());
      Assert.assertFalse(subRoot.isSuite());
   }

   @Test
   public void shouldBeAbleToGetChildrenChain() {
      TestClass root = new TestClass(Root.class);
      TestClass subRoot = new TestClass(root, SubRoot.class);
      new TestClass(subRoot, Child1.class);
      TestClass subSubRoot = new TestClass(subRoot, SubSubRoot.class);
      new TestClass(subSubRoot, Child2.class);

      Assert.assertEquals(5, root.getChildrenChain().size());
      Assert.assertEquals(4, subRoot.getChildrenChain().size());
      Assert.assertEquals(2, subSubRoot.getChildrenChain().size());
   }

   @Test
   public void shouldBeAbleToGetParent() {
      TestClass root = new TestClass(Root.class);
      TestClass subRoot = new TestClass(root, SubRoot.class);
      TestClass subSubRoot = new TestClass(subRoot, SubSubRoot.class);

      Assert.assertEquals(subSubRoot.getJavaClass(), SubSubRoot.class);
      Assert.assertEquals(subSubRoot.getParent().getJavaClass(), SubRoot.class);
      Assert.assertEquals(subSubRoot.getParent().getParent().getJavaClass(), Root.class);
   }
   
   @Test
   public void shouldBeAbleToGetParents() {
      TestClass root = new TestClass(Root.class);
      TestClass subRoot = new TestClass(root, SubRoot.class);
      new TestClass(subRoot, Child1.class);
      TestClass subSubRoot = new TestClass(subRoot, SubSubRoot.class);
      new TestClass(subSubRoot, Child2.class);

      Assert.assertEquals(3, subSubRoot.getParentChain().size());
      Assert.assertEquals(SubSubRoot.class, subSubRoot.getParentChain().get(0));
   }

   @Test
   public void shouldBeAbleToGetParentsChildFirst() {
      TestClass root = new TestClass(Root.class);
      TestClass subRoot = new TestClass(root, SubRoot.class);
      TestClass subSubRoot = new TestClass(subRoot, SubSubRoot.class);
      new TestClass(subSubRoot, Child1.class);

      Assert.assertEquals(SubSubRoot.class, subSubRoot.getParentChain().get(0));
      Assert.assertEquals(SubRoot.class, subSubRoot.getParentChain().get(1));
      Assert.assertEquals(Root.class, subSubRoot.getParentChain().get(2));
   }

   public static class Root {}
   public static class SubRoot {}
   public static class SubSubRoot {}
   public static class Child1 {}
   public static class Child2 {}
   public static class Child3 {}
}
