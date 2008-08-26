// Copyright 2008 Google Inc. All Rights Reserved.
package org.datanucleus.store.appengine;

import com.google.apphosting.api.datastore.Entity;
import com.google.apphosting.api.datastore.EntityNotFoundException;
import com.google.apphosting.api.datastore.KeyFactory;
import org.datanucleus.test.Book;

import java.lang.reflect.Field;

import javax.persistence.EntityTransaction;

/**
 * @author Max Ross <maxr@google.com>
 */
public class JPAInsertionTest extends JPATestCase {
  public void testSimpleInsert() throws EntityNotFoundException {
    Book b1 = new Book();
    b1.setAuthor("jimmy");
    b1.setIsbn("isbn");
    b1.setTitle("the title");
    assertNull(b1.getId());
    EntityTransaction txn = em.getTransaction();
    txn.begin();
    em.persist(b1);
    txn.commit();
    assertNotNull(b1.getId());
    Entity entity = ldth.ds.get(KeyFactory.decodeKey(b1.getId()));
    assertNotNull(entity);
    assertEquals("jimmy", entity.getProperty("author"));
    assertEquals("isbn", entity.getProperty("isbn"));
    assertEquals("the title", entity.getProperty("title"));
    assertEquals(Book.class.getName(), entity.getKind());
  }

  public void testInsertObjectWithPKAlreadySet() throws NoSuchFieldException,
      IllegalAccessException {
    Book b = new Book();
    Field idField = Book.class.getDeclaredField("id");
    idField.setAccessible(true);
    idField.set(b, "99");
    assertNotNull(b.getId());
    EntityTransaction txn = em.getTransaction();
    txn.begin();
    em.persist(b);
    try {
      txn.commit();
      fail("expected uoe");
    } catch (UnsupportedOperationException uoe) {
      // good
    }
  }
}