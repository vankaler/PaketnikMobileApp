package com.example.paketnikapp.apiUtil

import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.Document

class ApiUtil {

    private val database: MongoDatabase? = DatabaseUtil.getDatabase()

    fun getCollection(collectionName: String): MongoCollection<Document>? {
        return database?.getCollection(collectionName)
    }
}
