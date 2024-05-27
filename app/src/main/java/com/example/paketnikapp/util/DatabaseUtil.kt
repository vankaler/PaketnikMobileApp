import com.mongodb.client.MongoClients
import com.mongodb.client.MongoDatabase
import com.google.gson.Gson
import java.io.File

object DatabaseUtil {
    private var database: MongoDatabase? = null

    data class DatabaseConfig(var url: String, var database: String)

    fun connect() {
        val config = readConfig()
        val client = MongoClients.create(config.url)
        database = client.getDatabase(config.database)
    }

    private fun readConfig(): DatabaseConfig {
        val configFile = File("app/src/main/java/com/example/paketnikapp/db/config.json")
        val config = Gson().fromJson(configFile.readText(), DatabaseConfig::class.java)
        return(DatabaseConfig(config.url, config.database))
    }

    fun getDatabase(): MongoDatabase? {
        if (database == null) {
            connect()
        }
        return database
    }
}