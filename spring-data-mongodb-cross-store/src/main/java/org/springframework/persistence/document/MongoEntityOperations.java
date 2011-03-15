package org.springframework.persistence.document;

import java.lang.reflect.Field;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.document.mongodb.MongoTemplate;
import org.springframework.persistence.OrderedEntityOperations;
import org.springframework.persistence.RelatedEntity;
import org.springframework.persistence.support.ChangeSet;
import org.springframework.persistence.support.ChangeSetBacked;
import org.springframework.persistence.support.ChangeSetPersister.NotFoundException;
import org.springframework.persistence.support.EntityInstantiator;
import org.springframework.persistence.support.HashMapChangeSet;

public class MongoEntityOperations extends OrderedEntityOperations<Object, ChangeSetBacked> {

  @Autowired
  private MongoTemplate mongoTemplate;

  private EntityInstantiator<ChangeSetBacked, ChangeSet> entityInstantiator;

  private MongoChangeSetPersister changeSetPersister;

  public void setEntityInstantiator(EntityInstantiator<ChangeSetBacked, ChangeSet> entityInstantiator) {
    this.entityInstantiator = entityInstantiator;
  }

  @Autowired
  public void setChangeSetPersister(MongoChangeSetPersister changeSetPersister) {
    this.changeSetPersister = changeSetPersister;
  }


  @Override
  public boolean cacheInEntity() {
    return true;
  }

  @Override
  public ChangeSetBacked findEntity(Class<ChangeSetBacked> entityClass, Object key) throws DataAccessException {
    try {
      ChangeSet cs = new HashMapChangeSet();
      changeSetPersister.getPersistentState(entityClass, key, cs);
      return entityInstantiator.createEntityFromState(cs, entityClass);
    } catch (NotFoundException ex) {
      return null;
    }
  }

  @Override
  public Object findUniqueKey(ChangeSetBacked entity) throws DataAccessException {
    return entity.getId();
  }

  @Override
  public boolean isTransactional() {
    // TODO
    return false;
  }

  @Override
  public boolean isTransient(ChangeSetBacked entity) throws DataAccessException {
    return entity.getId() == null;
  }

  @Override
  public Object makePersistent(Object owner, ChangeSetBacked entity, Field f, RelatedEntity fs) throws DataAccessException {
    changeSetPersister.persistState(entity.getClass(), entity.getChangeSet());
    return entity.getId();
  }

  @Override
  public boolean supports(Class<?> entityClass, RelatedEntity fs) {
    return entityClass.isAnnotationPresent(DocumentEntity.class);
  }

}
