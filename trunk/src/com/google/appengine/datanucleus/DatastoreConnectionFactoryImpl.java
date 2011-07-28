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
package com.google.appengine.datanucleus;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceConfig;

import org.datanucleus.store.StoreManager;
import org.datanucleus.store.connection.AbstractConnectionFactory;
import org.datanucleus.store.connection.ManagedConnection;
import org.datanucleus.store.connection.ManagedConnectionResourceListener;
import org.datanucleus.PersistenceConfiguration;
import org.datanucleus.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.xa.XAResource;

/**
 * Factory for connections to the datastore.
 *
 * @author Max Ross <maxr@google.com>
 */
public class DatastoreConnectionFactoryImpl extends AbstractConnectionFactory {

  public static final String AUTO_CREATE_TXNS_PROPERTY =
      "datanucleus.appengine.autoCreateDatastoreTxns";

  private final boolean isAutoCreateTransaction;

  /**
   * Constructs a connection factory for the datastore.
   * This connection factory either creates connections that are all
   * AutoCreateTransaction or connections that are all non-AutoCreateTransaction.
   * AutoCreateTransaction connections manage an underlying datastore transaction.
   * Non-AutoCreateTransaction connections do not manage an underlying datastore
   * transaction.   The type of connection that this factory provides
   * is controlled via the {@link #AUTO_CREATE_TXNS_PROPERTY} property, which
   * can be specified in jdoconfig.xml (for JDO) or persistence.xml (for JPA).
   * Note that the default value for this property is {@code true}.
   *
   * JDO Example:
   * <pre>
   * <jdoconfig xmlns="http://java.sun.com/xml/ns/jdo/jdoconfig"
   * xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   * xsi:noNamespaceSchemaLocation="http://java.sun.com/xml/ns/jdo/jdoconfig">
   *
   *  <persistence-manager-factory name="transactional">
   *     <property name="javax.jdo.PersistenceManagerFactoryClass" value="org.datanucleus.api.jdo.JDOPersistenceManagerFactory"/>
   *     <property name="javax.jdo.option.ConnectionURL" value="appengine"/>
   *  </persistence-manager-factory>
   *
   *  <persistence-manager-factory name="nontransactional">
   *     <property name="javax.jdo.PersistenceManagerFactoryClass" value="org.datanucleus.api.jdo.JDOPersistenceManagerFactory"/>
   *     <property name="javax.jdo.option.ConnectionURL" value="appengine"/>
   *     <property name="datanucleus.appengine.autoCreateDatastoreTxns" value="false"/>
   *  </persistence-manager-factory>
   *
   * </jdoconfig>
   * </pre>
   *
   * JPA Example:
   * <pre>
   * <persistence-unit name="transactional">
   *     <properties>
   *         <property name="datanucleus.ConnectionURL" value="appengine"/>
   *     </properties>
   * </persistence-unit>
   *
   * <persistence-unit name="nontransactional">
   *     <properties>
   *         <property name="datanucleus.ConnectionURL" value="appengine"/>
   *         <property name="datanucleus.appengine.autoCreateDatastoreTxns" value="false"/>
   *     </properties>
   * </persistence-unit>
   * </pre>
   *
   * @param nucContext The OMFContext
   * @param resourceType The resource type of the connection, either tx or
   * notx.  We ignore this parameter because it isn't a valid indication of
   * whether or not this connection factory creates transactional connections.
   */
  public DatastoreConnectionFactoryImpl(StoreManager storeMgr, String resourceType) {
    super(storeMgr, resourceType);

    PersistenceConfiguration conf = storeMgr.getNucleusContext().getPersistenceConfiguration();
    if (conf.getProperty(DatastoreConnectionFactoryImpl.AUTO_CREATE_TXNS_PROPERTY) == null) {
        // User hasn't configured the "auto-create" property, so set it
        conf.setProperty(DatastoreConnectionFactoryImpl.AUTO_CREATE_TXNS_PROPERTY, Boolean.TRUE.toString());
    }
    this.isAutoCreateTransaction = conf.getBooleanProperty(AUTO_CREATE_TXNS_PROPERTY);
  }

  public void close() {}

  /**
   * {@inheritDoc}
   */
  public ManagedConnection getConnection(Object poolKey, Transaction txn, Map options) {
    return storeMgr.getConnectionManager().allocateConnection(this, poolKey, txn, options);
  }

  /**
   * {@inheritDoc}
   */
  public ManagedConnection createManagedConnection(Object poolKey, Map transactionOptions) {
    return new DatastoreManagedConnection(storeMgr, isAutoCreateTransaction());
  }

  boolean isAutoCreateTransaction() {
    return isAutoCreateTransaction;
  }

  static class DatastoreManagedConnection implements ManagedConnection {
    private boolean managed = false;
    private boolean locked = false;
    private final List<ManagedConnectionResourceListener> listeners =
        new ArrayList<ManagedConnectionResourceListener>();
    private final XAResource datastoreXAResource;

    DatastoreManagedConnection(StoreManager storeMgr, boolean autoCreateTransaction) {
      if (autoCreateTransaction) {
        DatastoreServiceConfig config = ((DatastoreManager) storeMgr).getDefaultDatastoreServiceConfigForWrites();
        DatastoreService datastoreService = DatastoreServiceFactoryInternal.getDatastoreService(config);
        datastoreXAResource = new DatastoreXAResource(datastoreService);
      } else {
        datastoreXAResource = new EmulatedXAResource();
      }
    }

    public Object getConnection() {
      if (datastoreXAResource instanceof DatastoreXAResource) {
        // Return the DatastoreTransaction
        return ((DatastoreXAResource)datastoreXAResource).getCurrentTransaction();
      }
      return null;
    }

    public XAResource getXAResource() {
      return datastoreXAResource;
    }

    public void release() {
      if (!managed) {        
        close();
      }
    }

    public void close() {
      // nothing to close
      for (ManagedConnectionResourceListener listener : listeners) {
        listener.managedConnectionPreClose();
      }
      for (ManagedConnectionResourceListener listener : listeners) {
        listener.managedConnectionPostClose();
      }
    }

    public void setManagedResource() {
      managed = true;
    }

    public boolean isLocked() {
      return locked;
    }

    public void lock() {
      locked = true;
    }

    public void unlock() {
      locked = false;
    }

    public void transactionFlushed() {
        for (ManagedConnectionResourceListener listener : listeners) {
            listener.transactionFlushed();
        }
    }

    public void transactionPreClose() {
        for (ManagedConnectionResourceListener listener : listeners) {
            listener.transactionPreClose();
        }
    }

    public void addListener(ManagedConnectionResourceListener listener) {
      listeners.add(listener);
    }

    public void removeListener(ManagedConnectionResourceListener listener) {
      listeners.remove(listener);
    }
  }
}
