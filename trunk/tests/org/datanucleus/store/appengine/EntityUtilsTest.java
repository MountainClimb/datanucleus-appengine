// Copyright 2008 Google Inc. All Rights Reserved.
package org.datanucleus.store.appengine;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.apphosting.api.ApiProxy;

import junit.framework.TestCase;

import org.datanucleus.exceptions.NucleusUserException;
import org.datanucleus.metadata.AbstractMemberMetaData;

/**
 * @author Max Ross <maxr@google.com>
 */
public class EntityUtilsTest extends TestCase {

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ApiProxy.setEnvironmentForCurrentThread(DatastoreTestHelper.ENV);
  }

  public void testUnencodedStringToEncodedString() {
    String keyStr = (String) EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(true, String.class), Object.class, "yar");
    assertEquals(KeyFactory.createKey("Object", "yar"), KeyFactory.stringToKey(keyStr));
  }

  public void testUnencodedStringToUnencodedString() {
    String keyStr = (String) EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(false, String.class), Object.class, "yar");
    assertEquals("yar", keyStr);

  }

  public void testUnencodedStringToLong() {
    try {
    EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(false, Long.class), Object.class, "yar");
      fail("expected exception");
    } catch (NucleusUserException e) {
      // good
    }
  }

  public void testUnencodedStringToKey() {
    Key key = (Key) EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(false, Key.class), Object.class, "yar");
    assertEquals(KeyFactory.createKey("Object", "yar"), key);
  }

  public void testEncodedStringToEncodedString() {
    Key key = KeyFactory.createKey("Object", "yar");
    String keyStr = (String) EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(true, String.class), Object.class,
        KeyFactory.keyToString(key));
    assertEquals(key, KeyFactory.stringToKey(keyStr));
  }

  public void testEncodedStringToUnencodedString() {
    Key key = KeyFactory.createKey("Object", "yar");
    String name = (String) EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(false, String.class), Object.class,
        KeyFactory.keyToString(key));
    assertEquals("yar", name);
  }

  public void testEncodedStringToUnencodedString_IdSet() {
    Key key = KeyFactory.createKey("Object", 44);
    try {
      EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(false, String.class), Object.class,
        KeyFactory.keyToString(key));
      fail("expected exception");
    } catch (NucleusUserException e) {
      // good
    }
  }

  public void testEncodedStringToLong() {
    Key key = KeyFactory.createKey("Object", 44);
    long id = (Long) EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(false, Long.class), Object.class,
        KeyFactory.keyToString(key));
    assertEquals(44, id);
  }

  public void testEncodedStringToLong_NameSet() {
    Key key = KeyFactory.createKey("Object", "yar");
    try {
      EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(false, Long.class), Object.class,
        KeyFactory.keyToString(key));
      fail("expected exception");
    } catch (NucleusUserException e) {
      // good
    }
  }

  public void testEncodedStringToKey_NameSet() {
    Key input = KeyFactory.createKey("Object", "yar");
    Key output = (Key) EntityUtils.idToInternalKey(
      "Object", new PrimaryKeyMemberMetaData(false, Key.class), Object.class,
      KeyFactory.keyToString(input));
    assertEquals(input, output);
  }

  public void testEncodedStringToKey_IdSet() {
    Key input = KeyFactory.createKey("Object", 33);
    Key output = (Key) EntityUtils.idToInternalKey(
      "Object", new PrimaryKeyMemberMetaData(false, Key.class), Object.class,
      KeyFactory.keyToString(input));
    assertEquals(input, output);
  }

  public void testLongToEncodedString() {
    String output = (String) EntityUtils.idToInternalKey(
      "Object", new PrimaryKeyMemberMetaData(true, String.class), Object.class, 44);
    assertEquals(KeyFactory.createKey("Object", 44), KeyFactory.stringToKey(output));
  }

  public void testLongToUnencodedString() {
    try {
      EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(false, String.class), Object.class, 44);
      fail("expected exception");
    } catch (NucleusUserException e) {
      // good
    }
  }

  public void testLongToLong() {
    long output = (Long) EntityUtils.idToInternalKey(
      "Object", new PrimaryKeyMemberMetaData(false, Long.class), Object.class, 44);
    assertEquals(44, output);
  }

  public void testLongToKey() {
    Key output = (Key) EntityUtils.idToInternalKey(
      "Object", new PrimaryKeyMemberMetaData(false, Key.class), Object.class, 44);
    assertEquals(KeyFactory.createKey("Object", 44), output);

  }

  public void testKeyToEncodedString() {
    Key key = KeyFactory.createKey("Object", 44);
    String output = (String) EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(true, String.class), Object.class,
        key);
    assertEquals(key, KeyFactory.stringToKey(output));
  }

  public void testKeyToUnencodedString_NameSet() {
    Key key = KeyFactory.createKey("Object", "yar");
    String output = (String) EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(false, String.class), Object.class,
        key);
    assertEquals("yar", output);
  }

  public void testKeyToUnencodedString_IdSet() {
    Key key = KeyFactory.createKey("Object", 44);
    try {
      EntityUtils.idToInternalKey(
          "Object", new PrimaryKeyMemberMetaData(false, String.class), Object.class, key);
      fail("expected exception");
    } catch (NucleusUserException e) {
      // good
    }
  }

  public void testKeyToLong_NameSet() {
    Key key = KeyFactory.createKey("Object", "yar");
    try {
      EntityUtils.idToInternalKey(
          "Object", new PrimaryKeyMemberMetaData(false, Long.class), Object.class, key);
      fail("expected exception");
    } catch (NucleusUserException e) {
      // good
    }
  }

  public void testKeyToKey() {
    Key key = KeyFactory.createKey("Object", "yar");
    Key output = (Key) EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(false, Key.class), Object.class, key);
    assertEquals(key, output);
  }

  public void testNullToEncodedString() {
    Object output = EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(true, String.class), Object.class, null);
    assertNull(output);
  }

  public void testNullToUnencodedString() {
    Object output = EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(false, String.class), Object.class, null);
    assertNull(output);
  }

  public void testNullToLong() {
    Object output = EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(false, Long.class), Object.class, null);
    assertNull(output);
  }

  public void testNullToKey() {
    Object output = EntityUtils.idToInternalKey(
        "Object", new PrimaryKeyMemberMetaData(false, Key.class), Object.class, null);
    assertNull(output);
  }

  public void testEncodedStringOfWrongKind() {
    Key key = KeyFactory.createKey("Object", "yar");
    try {
      EntityUtils.idToInternalKey(
          "NotObject", new PrimaryKeyMemberMetaData(true, String.class), Object.class,
          KeyFactory.keyToString(key));
      fail("expected exception");
    } catch (NucleusUserException e) {
      // good
    }
  }

  public void testKeyOfWrongKind() {
    Key key = KeyFactory.createKey("Object", "yar");
    try {
      EntityUtils.idToInternalKey(
          "NotObject", new PrimaryKeyMemberMetaData(true, Key.class), Object.class, key);
      fail("expected exception");
    } catch (NucleusUserException e) {
      // good
    }
  }

  private static final class PrimaryKeyMemberMetaData extends AbstractMemberMetaData {

    private final boolean hasEncodedPkExtension;
    private final Class<?> type;

    private PrimaryKeyMemberMetaData(boolean hasEncodedPkExtension, Class<?> type) {
      super(null, "Jimmy");
      this.hasEncodedPkExtension = hasEncodedPkExtension;
      this.type = type;
    }

    @Override
    public Class getType() {
      return type;
    }

    @Override
    public boolean hasExtension(String key) {
      return DatastoreManager.ENCODED_PK.equals(key) && hasEncodedPkExtension;
    }
  }
}