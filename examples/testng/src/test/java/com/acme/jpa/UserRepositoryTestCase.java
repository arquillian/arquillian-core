/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acme.jpa;

import java.util.List;

import javax.ejb.EJB;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.descriptor.api.Descriptors;
import org.jboss.shrinkwrap.descriptor.api.persistence20.PersistenceDescriptor;
import org.jboss.shrinkwrap.descriptor.api.persistence20.PersistenceUnitTransactionType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * UserRepositoryTest
 *
 * @author <a href="mailto:aslak@conduct.no">Aslak Knutsen</a>
 * @version $Revision: $
 */
@Test(groups = "integration")
public class UserRepositoryTestCase extends Arquillian
{
   @Deployment
   public static JavaArchive createDeployment() {
      return ShrinkWrap.create(JavaArchive.class, "test.jar")
               .addPackage(
                     User.class.getPackage())
               .addAsManifestResource(new StringAsset(
                     Descriptors.create(PersistenceDescriptor.class)
                        .createPersistenceUnit()
                            .name("Domain")
                            .provider("org.hibernate.ejb.HibernatePersistence")
                            .transactionType(PersistenceUnitTransactionType._JTA)
                            .clazz(User.class.toString())
                            .excludeUnlistedClasses(true)
                            .jtaDataSource("java:/DefaultDS")
                            .getOrCreateProperties()
                                .createProperty()
                                    .name("hibernate.hbm2ddl.auto")
                                    .value("create-drop")
                                    .up().up().up()
                        .exportAsString()), "persistence.xml");
   }
   
   private static final String FIRST_NAME = "first-name";
   private static final String LAST_NAME = "last-name";
   
   @EJB
   private UserRepository userRepository;
   
   @Test
   public void shouldBeAbleToStoreUser() throws Exception 
   {
      userRepository.store(new User(FIRST_NAME, LAST_NAME));
   }
   
   @Test(dependsOnMethods = "shouldBeAbleToStoreUser")
   public void shouldBeAbleToFindUser() throws Exception 
   {
      List<User> users  = userRepository.getByFirstName(FIRST_NAME);
      
      Assert.assertNotNull(users);
      Assert.assertTrue(users.size() == 1);
      
      Assert.assertEquals(users.get(0).getLastName(), LAST_NAME);
      Assert.assertEquals(users.get(0).getFirstName(), FIRST_NAME);
   }
}
