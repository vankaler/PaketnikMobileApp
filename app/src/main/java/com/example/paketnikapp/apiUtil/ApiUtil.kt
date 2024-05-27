package com.example.paketnikapp.apiUtil

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document

class ApiUtil {

    private val database: MongoDatabase? = DatabaseUtil.getDatabase()

    fun getCollection(collectionName: String): MongoCollection<Document>? {
        return database?.getCollection(collectionName)
    }

    fun insertDocument(collectionName: String, document: Document) {
        val collection = getCollection(collectionName)
        collection?.insertOne(document)
    }

    fun findDocumentByField(collectionName: String, fieldName: String, fieldValue: String): Document? {
        val collection = getCollection(collectionName)
        return collection?.find(Document(fieldName, fieldValue))?.firstOrNull()
    }

    fun updateDocument(collectionName: String, filter: Document, update: Document) {
        val collection = getCollection(collectionName)
        collection?.updateOne(filter, Document("\$set", update))
    }

    fun deleteDocument(collectionName: String, filter: Document) {
        val collection = getCollection(collectionName)
        collection?.deleteOne(filter)
    }

    fun getAllDocuments(collectionName: String): List<Document> {
        val collection = getCollection(collectionName)
        return collection?.find()?.toList() ?: emptyList()
    }


}
