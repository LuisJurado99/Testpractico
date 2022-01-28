package developer.unam.testpractico.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import developer.unam.testpractico.retrofit.movies.Result

class AppDatabase(private val context: Context) : SQLiteOpenHelper(context, "bd.db", null, 1) {

    object FeedReaderContract {
        // Table contents are grouped together in an anonymous object.
        object FeedEntry : BaseColumns {
            const val TABLE_NAME = "movies"
            const val TABLE_NAME_ID = "_id"
            const val COLUMN_NAME_TITLE = "poster"
            const val COLUMN_NAME_SUBTITLE = "title"
        }
    }

    private val SQL_CREATE_ENTRIES = "CREATE TABLE ${FeedReaderContract.FeedEntry.TABLE_NAME} (" +
            "${FeedReaderContract.FeedEntry.TABLE_NAME_ID} INTEGER," +
            "${FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE} TEXT," +
            "${FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE} TEXT" +
            ")"
    private val SQL_DELTE = "DROP TABLE IF EXISTS movie"

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELTE)
        onCreate(db)
    }

    fun getAllMovies():List<Result> {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${FeedReaderContract.FeedEntry.TABLE_NAME}", null
        )
        var list= listOf<Result>()
        while (cursor.moveToNext()) {
            list = list.plus(Result(cursor.getInt(0), cursor.getString(1), cursor.getString(2)))
        }
        return list
    }

    fun deleteMovies(){
        writableDatabase.delete(FeedReaderContract.FeedEntry.TABLE_NAME,null,null)
    }

    fun insertMovie(result: Result){
        val content = ContentValues()
        content.apply {
            put(FeedReaderContract.FeedEntry.TABLE_NAME_ID,result.id)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE,result.title)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE,result.poster_path)
        }
        writableDatabase.insert(FeedReaderContract.FeedEntry.TABLE_NAME,null,content)
    }

}