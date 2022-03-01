package developer.unam.testpractico.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import android.util.Log
import developer.unam.testpractico.retrofit.movies.Result

class AppDatabase(private val context: Context) : SQLiteOpenHelper(context, "bd.db", null, 2) {

    object FeedReaderContract {
        // Table contents are grouped together in an anonymous object.
        object FeedEntry : BaseColumns {
            const val TABLE_NAME_POPULAR = "popular"
            const val TABLE_NAME_NOW_PLAYING = "now_playing"
            const val TABLE_NAME_UPCOMING = "upcoming"
            const val TABLE_NAME_TOP_RATED = "top_rated"
            const val TABLE_NAME_ID = "_id"
            const val COLUMN_NAME_TITLE = "poster"
            const val COLUMN_NAME_SUBTITLE = "title"
        }
    }

    private val SQL_CREATE_TABLE_POPULAR = "CREATE TABLE ${FeedReaderContract.FeedEntry.TABLE_NAME_POPULAR} (" +
            "${FeedReaderContract.FeedEntry.TABLE_NAME_ID} INTEGER," +
            "${FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE} TEXT," +
            "${FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE} TEXT" +
            ")"
    private val SQL_CREATE_ENTRIES_NOW_PLAYING = "CREATE TABLE ${FeedReaderContract.FeedEntry.TABLE_NAME_NOW_PLAYING} (" +
            "${FeedReaderContract.FeedEntry.TABLE_NAME_ID} INTEGER," +
            "${FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE} TEXT," +
            "${FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE} TEXT" +
            ")"
    private val SQL_CREATE_ENTRIES_UPCOMING = "CREATE TABLE ${FeedReaderContract.FeedEntry.TABLE_NAME_UPCOMING} (" +
            "${FeedReaderContract.FeedEntry.TABLE_NAME_ID} INTEGER," +
            "${FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE} TEXT," +
            "${FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE} TEXT" +
            ")"
    private val SQL_CREATE_ENTRIES_TOP_RATED = "CREATE TABLE ${FeedReaderContract.FeedEntry.TABLE_NAME_TOP_RATED} (" +
            "${FeedReaderContract.FeedEntry.TABLE_NAME_ID} INTEGER," +
            "${FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE} TEXT," +
            "${FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE} TEXT" +
            ")"
    private val SQL_DELTE_POPULAR = "DROP TABLE IF EXISTS ${FeedReaderContract.FeedEntry.TABLE_NAME_POPULAR}"
    private val SQL_DELTE_NOW_PLAYING = "DROP TABLE IF EXISTS ${FeedReaderContract.FeedEntry.TABLE_NAME_NOW_PLAYING}"
    private val SQL_DELTE_UPCOMING = "DROP TABLE IF EXISTS ${FeedReaderContract.FeedEntry.TABLE_NAME_UPCOMING}"
    private val SQL_DELTE_TOP_RATED = "DROP TABLE IF EXISTS ${FeedReaderContract.FeedEntry.TABLE_NAME_TOP_RATED}"

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(SQL_CREATE_ENTRIES_NOW_PLAYING)
        db?.execSQL(SQL_CREATE_ENTRIES_TOP_RATED)
        db?.execSQL(SQL_CREATE_ENTRIES_UPCOMING)
        db?.execSQL(SQL_CREATE_TABLE_POPULAR)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(SQL_DELTE_POPULAR)
        db?.execSQL(SQL_DELTE_NOW_PLAYING)
        db?.execSQL(SQL_DELTE_UPCOMING)
        db?.execSQL(SQL_DELTE_TOP_RATED)
        onCreate(db)
    }

    fun getAllMoviesPopular():List<Result> {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${FeedReaderContract.FeedEntry.TABLE_NAME_POPULAR}", null
        )
        var list= listOf<Result>()
        while (cursor.moveToNext()) {
            list = list.plus(Result(cursor.getInt(0), cursor.getString(1), cursor.getString(2)))
        }
        return list
    }
    fun getAllMoviesNowPlaying():List<Result> {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${FeedReaderContract.FeedEntry.TABLE_NAME_NOW_PLAYING}", null
        )
        var list= listOf<Result>()
        while (cursor.moveToNext()) {
            list = list.plus(Result(cursor.getInt(0), cursor.getString(1), cursor.getString(2)))
        }
        return list
    }
    fun getAllMoviesUpcoming():List<Result> {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${FeedReaderContract.FeedEntry.TABLE_NAME_UPCOMING}", null
        )
        var list= listOf<Result>()
        while (cursor.moveToNext()) {
            list = list.plus(Result(cursor.getInt(0), cursor.getString(1), cursor.getString(2)))
        }
        return list
    }
    fun getAllMoviesTopRated():List<Result> {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM ${FeedReaderContract.FeedEntry.TABLE_NAME_TOP_RATED}", null
        )
        var list= listOf<Result>()
        while (cursor.moveToNext()) {
            list = list.plus(Result(cursor.getInt(0), cursor.getString(1), cursor.getString(2)))
        }
        return list
    }

    fun deleteMoviesPopular()=writableDatabase.delete(FeedReaderContract.FeedEntry.TABLE_NAME_POPULAR,null,null)
    fun deleteMoviesNowPlaying()=writableDatabase.delete(FeedReaderContract.FeedEntry.TABLE_NAME_NOW_PLAYING,null,null)
    fun deleteMoviesUpcoming()=writableDatabase.delete(FeedReaderContract.FeedEntry.TABLE_NAME_UPCOMING,null,null)
    fun deleteMoviesTopRated()=writableDatabase.delete(FeedReaderContract.FeedEntry.TABLE_NAME_TOP_RATED,null,null)


    fun insertMoviePopular(result: Result){
        val content = ContentValues()
        content.apply {
            put(FeedReaderContract.FeedEntry.TABLE_NAME_ID,result.id)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE,result.title)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE,result.poster_path)
        }
        Log.e("insertE","insertElement")
        writableDatabase.insert(FeedReaderContract.FeedEntry.TABLE_NAME_POPULAR,null,content)
    }
    fun insertMovieTopRated(result: Result){
        val content = ContentValues()
        content.apply {
            put(FeedReaderContract.FeedEntry.TABLE_NAME_ID,result.id)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE,result.title)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE,result.poster_path)
        }
        writableDatabase.insert(FeedReaderContract.FeedEntry.TABLE_NAME_TOP_RATED,null,content)
    }
    fun insertMovieNowPlaying(result: Result){
        val content = ContentValues()
        content.apply {
            put(FeedReaderContract.FeedEntry.TABLE_NAME_ID,result.id)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE,result.title)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE,result.poster_path)
        }
        writableDatabase.insert(FeedReaderContract.FeedEntry.TABLE_NAME_NOW_PLAYING,null,content)
    }
    fun insertMovieUpcoming(result: Result){
        val content = ContentValues()
        content.apply {
            put(FeedReaderContract.FeedEntry.TABLE_NAME_ID,result.id)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_TITLE,result.title)
            put(FeedReaderContract.FeedEntry.COLUMN_NAME_SUBTITLE,result.poster_path)
        }
        writableDatabase.insert(FeedReaderContract.FeedEntry.TABLE_NAME_UPCOMING,null,content)
    }

}