/**********************************************************************
Copyright (c) 2009 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
**********************************************************************/
package org.datanucleus.store.appengine;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

import static org.datanucleus.store.appengine.TestUtils.assertKeyParentEquals;
import org.datanucleus.test.BidirectionalChildListJDO;
import org.datanucleus.test.BidirectionalChildListLongPkJDO;
import org.datanucleus.test.BidirectionalChildListStringPkJDO;
import org.datanucleus.test.Flight;
import org.datanucleus.test.HasKeyPkJDO;
import org.datanucleus.test.HasOneToManyListJDO;
import org.datanucleus.test.HasOneToManyListLongPkJDO;
import org.datanucleus.test.HasOneToManyListStringPkJDO;
import org.datanucleus.test.HasOneToManyListWithOrderByJDO;

/**
 * @author Max Ross <maxr@google.com>
 */
public class JDOOneToManyListTest extends JDOOneToManyTestCase {

  public void testInsertNewParentAndChild() throws EntityNotFoundException {
    HasOneToManyListJDO parent = new HasOneToManyListJDO();
    BidirectionalChildListJDO bidirChild = new BidirectionalChildListJDO();
    testInsert_NewParentAndChild(parent, bidirChild);
  }

  public void testInsertExistingParentNewChild() throws EntityNotFoundException {
    HasOneToManyListJDO parent = new HasOneToManyListJDO();
    BidirectionalChildListJDO bidirChild = new BidirectionalChildListJDO();
    testInsert_ExistingParentNewChild(parent, bidirChild);
  }

  public void testSwapAtPosition() throws EntityNotFoundException {
    HasOneToManyListJDO parent = new HasOneToManyListJDO();
    BidirectionalChildListJDO bidirChild = new BidirectionalChildListJDO();
    BidirectionalChildListJDO bidirChild2 = new BidirectionalChildListJDO();
    testSwapAtPosition(parent, bidirChild, bidirChild2);
  }

  public void testRemoveAtPosition() throws EntityNotFoundException {
    HasOneToManyListJDO parent = new HasOneToManyListJDO();
    BidirectionalChildListJDO bidirChild = new BidirectionalChildListJDO();
    BidirectionalChildListJDO bidirChild2 = new BidirectionalChildListJDO();
    BidirectionalChildListJDO bidirChild3 = new BidirectionalChildListJDO();
    testRemoveAtPosition(parent, bidirChild, bidirChild2, bidirChild3);
  }

  public void testAddAtPosition() throws EntityNotFoundException {
    HasOneToManyListJDO parent = new HasOneToManyListJDO();
    BidirectionalChildListJDO bidirChild = new BidirectionalChildListJDO();
    BidirectionalChildListJDO bidirChild2 = new BidirectionalChildListJDO();
    testAddAtPosition(parent, bidirChild, bidirChild2);
  }

  public void testUpdateUpdateChildWithMerge() throws EntityNotFoundException {
    HasOneToManyListJDO pojo = new HasOneToManyListJDO();
    BidirectionalChildListJDO bidir = new BidirectionalChildListJDO();
    testUpdate_UpdateChildWithMerge(pojo, bidir);
  }

  public void testUpdateUpdateChild() throws EntityNotFoundException {
    HasOneToManyListJDO pojo = new HasOneToManyListJDO();
    BidirectionalChildListJDO bidir = new BidirectionalChildListJDO();
    testUpdate_UpdateChild(pojo, bidir);
  }

  public void testUpdateNullOutChildren() throws EntityNotFoundException {
    HasOneToManyListJDO pojo = new HasOneToManyListJDO();
    BidirectionalChildListJDO bidir = new BidirectionalChildListJDO();
    testUpdate_NullOutChildren(pojo, bidir);
  }

  public void testUpdateClearOutChildren() throws EntityNotFoundException {
    HasOneToManyListJDO pojo = new HasOneToManyListJDO();
    BidirectionalChildListJDO bidir = new BidirectionalChildListJDO();
    testUpdate_ClearOutChildren(pojo, bidir);
  }

  public void testFindWithOrderBy() throws EntityNotFoundException {
    testFindWithOrderBy(HasOneToManyListWithOrderByJDO.class);
  }

  public void testFind() throws EntityNotFoundException {
    testFind(HasOneToManyListJDO.class, BidirectionalChildListJDO.class);
  }

  public void testQuery() throws EntityNotFoundException {
    testQuery(HasOneToManyListJDO.class, BidirectionalChildListJDO.class);
  }

  public void testChildFetchedLazily() throws Exception {
    testChildFetchedLazily(HasOneToManyListJDO.class, BidirectionalChildListJDO.class);
  }

  public void testDeleteParentDeletesChild() throws Exception {
    testDeleteParentDeletesChild(HasOneToManyListJDO.class, BidirectionalChildListJDO.class);
  }

  public void testIndexOf() throws Exception {
    HasOneToManyListJDO pojo = new HasOneToManyListJDO();
    BidirectionalChildListJDO bidir1 = new BidirectionalChildListJDO();
    BidirectionalChildListJDO bidir2 = new BidirectionalChildListJDO();

    Flight f1 = newFlight();
    Flight f2 = newFlight();

    HasKeyPkJDO hasKeyPk1 = new HasKeyPkJDO();
    HasKeyPkJDO hasKeyPk2 = new HasKeyPkJDO();

    pojo.addBidirChild(bidir1);
    pojo.addBidirChild(bidir2);
    pojo.addFlight(f1);
    pojo.addFlight(f2);
    pojo.addHasKeyPk(hasKeyPk1);
    pojo.addHasKeyPk(hasKeyPk2);

    beginTxn();
    pm.makePersistent(pojo);
    commitTxn();

    beginTxn();
    pojo = pm.getObjectById(HasOneToManyListJDO.class, pojo.getId());
    assertEquals(0, pojo.getBidirChildren().indexOf(bidir1));
    assertEquals(1, pojo.getBidirChildren().indexOf(bidir2));
    assertEquals(0, pojo.getFlights().indexOf(f1));
    assertEquals(1, pojo.getFlights().indexOf(f2));
    assertEquals(0, pojo.getHasKeyPks().indexOf(hasKeyPk1));
    assertEquals(1, pojo.getHasKeyPks().indexOf(hasKeyPk2));
    commitTxn();
  }

  public void testRemoveAll() throws EntityNotFoundException {
    testRemoveAll(new HasOneToManyListJDO(), new BidirectionalChildListJDO(),
                  new BidirectionalChildListJDO(), new BidirectionalChildListJDO());
  }

  public void testChangeParent() {
    testChangeParent(new HasOneToManyListJDO(), new HasOneToManyListJDO());
  }

  public void testNewParentNewChild_NamedKeyOnChild() throws EntityNotFoundException {
    testNewParentNewChild_NamedKeyOnChild(new HasOneToManyListJDO());
  }

  public void testInsert_NewParentAndChild_LongPk() throws EntityNotFoundException {
    BidirectionalChildListLongPkJDO bidirChild = new BidirectionalChildListLongPkJDO();
    bidirChild.setChildVal("yam");

    Flight f = newFlight();

    HasKeyPkJDO hasKeyPk = new HasKeyPkJDO();
    hasKeyPk.setStr("yag");

    HasOneToManyListLongPkJDO parent = new HasOneToManyListLongPkJDO();
    parent.addBidirChild(bidirChild);
    bidirChild.setParent(parent);
    parent.addFlight(f);
    parent.addHasKeyPk(hasKeyPk);
    parent.setVal("yar");

    beginTxn();
    pm.makePersistent(parent);
    commitTxn();

    assertNotNull(bidirChild.getId());
    assertNotNull(f.getId());
    assertNotNull(hasKeyPk.getKey());

    Entity bidirChildEntity = ldth.ds.get(KeyFactory.stringToKey(bidirChild.getId()));
    assertNotNull(bidirChildEntity);
    assertEquals("yam", bidirChildEntity.getProperty("childVal"));
    assertEquals(KeyFactory.stringToKey(bidirChild.getId()), bidirChildEntity.getKey());
    assertKeyParentEquals(parent.getClass(), parent.getId(), bidirChildEntity, bidirChild.getId());
    if (isIndexed()) {
      assertEquals(0L, bidirChildEntity.getProperty("bidirChildren_INTEGER_IDX"));
    }

    Entity flightEntity = ldth.ds.get(KeyFactory.stringToKey(f.getId()));
    assertNotNull(flightEntity);
    assertEquals("bos", flightEntity.getProperty("origin"));
    assertEquals("mia", flightEntity.getProperty("dest"));
    assertEquals("jimmy", flightEntity.getProperty("name"));
    assertEquals(KeyFactory.stringToKey(f.getId()), flightEntity.getKey());
    assertKeyParentEquals(parent.getClass(), parent.getId(), flightEntity, f.getId());
    if (isIndexed()) {
      assertEquals(0L, flightEntity.getProperty("flights_INTEGER_IDX"));
    }

    Entity hasKeyPkEntity = ldth.ds.get(hasKeyPk.getKey());
    assertNotNull(hasKeyPkEntity);
    assertEquals("yag", hasKeyPkEntity.getProperty("str"));
    assertEquals(hasKeyPk.getKey(), hasKeyPkEntity.getKey());
    assertKeyParentEquals(parent.getClass(), parent.getId(), hasKeyPkEntity, hasKeyPk.getKey());
    if (isIndexed()) {
      assertEquals(0L, hasKeyPkEntity.getProperty("hasKeyPks_INTEGER_IDX"));
    }

    Entity parentEntity = ldth.ds.get(TestUtils.createKey(parent, parent.getId()));
    assertNotNull(parentEntity);
    assertEquals(1, parentEntity.getProperties().size());
    assertEquals("yar", parentEntity.getProperty("val"));

    assertEquals(HasOneToManyListLongPkJDO.class.getName(), 1, countForClass(HasOneToManyListLongPkJDO.class));
    assertEquals(BidirectionalChildListLongPkJDO.class.getName(), 1, countForClass(BidirectionalChildListLongPkJDO.class));
    assertEquals(Flight.class.getName(), 1, countForClass(Flight.class));
    assertEquals(HasKeyPkJDO.class.getName(), 1, countForClass(HasKeyPkJDO.class));
  }

  public void testInsert_NewParentAndChild_StringPk() throws EntityNotFoundException {
    BidirectionalChildListStringPkJDO bidirChild = new BidirectionalChildListStringPkJDO();
    bidirChild.setChildVal("yam");

    Flight f = newFlight();

    HasKeyPkJDO hasKeyPk = new HasKeyPkJDO();
    hasKeyPk.setStr("yag");

    HasOneToManyListStringPkJDO parent = new HasOneToManyListStringPkJDO();
    parent.setId("yar");
    parent.addBidirChild(bidirChild);
    bidirChild.setParent(parent);
    parent.addFlight(f);
    parent.addHasKeyPk(hasKeyPk);
    parent.setVal("yar");

    beginTxn();
    pm.makePersistent(parent);
    commitTxn();

    assertNotNull(bidirChild.getId());
    assertNotNull(f.getId());
    assertNotNull(hasKeyPk.getKey());

    Entity bidirChildEntity = ldth.ds.get(KeyFactory.stringToKey(bidirChild.getId()));
    assertNotNull(bidirChildEntity);
    assertEquals("yam", bidirChildEntity.getProperty("childVal"));
    assertEquals(KeyFactory.stringToKey(bidirChild.getId()), bidirChildEntity.getKey());
    assertKeyParentEquals(parent.getClass(), parent.getId(), bidirChildEntity, bidirChild.getId());
    if (isIndexed()) {
      assertEquals(0L, bidirChildEntity.getProperty("bidirChildren_INTEGER_IDX"));
    }

    Entity flightEntity = ldth.ds.get(KeyFactory.stringToKey(f.getId()));
    assertNotNull(flightEntity);
    assertEquals("bos", flightEntity.getProperty("origin"));
    assertEquals("mia", flightEntity.getProperty("dest"));
    assertEquals("jimmy", flightEntity.getProperty("name"));
    assertEquals(KeyFactory.stringToKey(f.getId()), flightEntity.getKey());
    assertKeyParentEquals(parent.getClass(), parent.getId(), flightEntity, f.getId());
    if (isIndexed()) {
      assertEquals(0L, flightEntity.getProperty("flights_INTEGER_IDX"));
    }

    Entity hasKeyPkEntity = ldth.ds.get(hasKeyPk.getKey());
    assertNotNull(hasKeyPkEntity);
    assertEquals("yag", hasKeyPkEntity.getProperty("str"));
    assertEquals(hasKeyPk.getKey(), hasKeyPkEntity.getKey());
    assertKeyParentEquals(parent.getClass(), parent.getId(), hasKeyPkEntity, hasKeyPk.getKey());
    if (isIndexed()) {
      assertEquals(0L, hasKeyPkEntity.getProperty("hasKeyPks_INTEGER_IDX"));
    }

    Entity parentEntity = ldth.ds.get(TestUtils.createKey(parent, parent.getId()));
    assertNotNull(parentEntity);
    assertEquals(1, parentEntity.getProperties().size());
    assertEquals("yar", parentEntity.getProperty("val"));

    assertEquals(HasOneToManyListStringPkJDO.class.getName(), 1, countForClass(HasOneToManyListStringPkJDO.class));
    assertEquals(BidirectionalChildListStringPkJDO.class.getName(), 1, countForClass(BidirectionalChildListStringPkJDO.class));
    assertEquals(Flight.class.getName(), 1, countForClass(Flight.class));
    assertEquals(HasKeyPkJDO.class.getName(), 1, countForClass(HasKeyPkJDO.class));
  }

  @Override
  boolean isIndexed() {
    return true;
  }
}