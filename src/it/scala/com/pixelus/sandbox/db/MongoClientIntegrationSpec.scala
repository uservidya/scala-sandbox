package com.pixelus.sandbox.db

import com.mongodb.{DBObject, BasicDBObject}
import org.scalatest._

class MongoClientIntegrationSpec
    extends FunSuite
            with ShouldMatchers
            with BeforeAndAfter
            with BeforeAndAfterAll {

  val CollectionName: String = "myCollection"

  var client: MongoClient = _
  var db: DB = _

  override def beforeAll() {
    client = new MongoClient()
    db = client.createDB("mydb")
  }

  before {
  }

  after {
    db.administrableCollection(CollectionName).drop();
  }

  override def afterAll() {
    client.dropDB("mydb")
  }

  private def createDBObject(): DBObject = {
    createDBObject(Map("key" -> "value"))
  }

  private def createDBObject(values: Map[String, String]): DBObject = {
    val obj = new BasicDBObject()
    for ((key, value) <- values) {
      obj.put(key, value)
    }
    obj
  }

  test("#collectionNames should return valid collection names") {
    db.updatableCollection(CollectionName) += createDBObject() // Need to create at least one item for the collection to exist.

    db.collectionNames should contain(CollectionName)
  }

  test("#getCount should return number of matching objects in collection") {
    val collection = db.updatableCollection(CollectionName)
    collection += createDBObject()
    collection.getCount(createDBObject()) should equal(1)

    collection += createDBObject()
    collection.getCount(createDBObject()) should equal(2)
  }

  test("#name should get collection name") {
    db.readOnlyCollection(CollectionName).name should equal("myCollection")
  }

  test("#fullName should get collection full name") {
    db.readOnlyCollection(CollectionName).fullName should equal("mydb.myCollection")
  }

  test("#find should find multiple objects that match query") {
    val collection = db.updatableCollection(CollectionName)
    collection += createDBObject(Map("id" -> "1", "name" -> "commonName"))
    collection += createDBObject(Map("id" -> "2", "name" -> "commonName"))

    collection.find(createDBObject(Map("name" -> "commonName"))).size() should equal(2)
  }

  test("#find with query limit should find any objects that match but limit results to 3 of 5") {
    val collection = db.updatableCollection(CollectionName)
    collection += createDBObject(Map("id" -> "1", "name" -> "commonName"))
    collection += createDBObject(Map("id" -> "2", "name" -> "commonName"))
    collection += createDBObject(Map("id" -> "3", "name" -> "commonName"))
    collection += createDBObject(Map("id" -> "4", "name" -> "commonName"))
    collection += createDBObject(Map("id" -> "5", "name" -> "commonName"))

    val query = Query(createDBObject(Map("name" -> "commonName"))).limit(3);
    collection.find(query).size() should equal(3)
  }

  test("#find should not find any objects that match query") {
    val collection = db.updatableCollection(CollectionName)
    collection += createDBObject(Map("id" -> "1", "name" -> "commonName"))
    collection += createDBObject(Map("id" -> "2", "name" -> "commonName"))

    collection.find(createDBObject(Map("name" -> "uncommonName"))).size() should equal(0)
  }

  test("#findOne should find one object among many that match query") {
    val collection = db.updatableCollection(CollectionName)
    collection += createDBObject(Map("id" -> "1", "name" -> "commonName"))
    collection += createDBObject(Map("id" -> "2", "name" -> "commonName"))

    collection.findOne(createDBObject(Map("name" -> "commonName"))).get("name") should equal("commonName")
  }

  test("#findOne should find one object among many") {
    val collection = db.updatableCollection(CollectionName)
    collection += createDBObject(Map("id" -> "1", "name" -> "commonName"))
    collection += createDBObject(Map("id" -> "2", "name" -> "commonName"))

    collection.findOne should not be (null)
  }

  test("#appendOperator should add object to updatable collection") {
    val dummyObject = createDBObject()
    val collection = db.updatableCollection(CollectionName)

    collection.getCount(dummyObject) should equal(0)
    collection += createDBObject()
    collection.getCount(dummyObject) should equal(1)
  }

  test("#removeOperator should remove object from updatable collection") {

    val deleteObject = createDBObject()
    val collection = db.updatableCollection(CollectionName)
    collection += deleteObject

    collection.getCount(deleteObject) should equal(1)
    collection -= (deleteObject)
    collection.getCount(deleteObject) should equal(0)
  }

  test("#drop should drop collection from administrable collection") {
    db.updatableCollection(CollectionName) += createDBObject() // Need to create at least one item for the collection to exist!
    val collection = db.administrableCollection(CollectionName)

    db.collectionNames should contain(CollectionName)
    collection.drop()

    db.collectionNames should not contain (CollectionName)
  }

  // Drop indexes test
}


