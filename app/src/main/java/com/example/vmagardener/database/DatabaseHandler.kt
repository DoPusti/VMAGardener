package com.example.vmagardener.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.vmagardener.models.PlantModel

class DatabaseHandler(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_PLANT_TABLE = ("CREATE TABLE " + TABLE_PLANT + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_IMAGE + " TEXT,"
                + KEY_DESCRIPTION + " TEXT,"
                + KEY_PLANTDATE + " TEXT,"
                + KEY_LOCATION + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT,"
                + KEY_INTVWATER + " TEXT,"
                + KEY_INTVCUT + " TEXT,"
                + KEY_INTVFERTILIZE + " TEXT)"

                )
        db?.execSQL(CREATE_PLANT_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_PLANT")
        onCreate(db)
    }

    fun addPlant(plant: PlantModel): Long {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        /* Werte aus dem Model Plant-Model */
        /* ID wird automatisch vergeben, wegen PrimaryKey */
        contentValues.put(KEY_NAME, plant.name)
        contentValues.put(KEY_IMAGE, plant.image)
        contentValues.put(KEY_DESCRIPTION, plant.description)
        contentValues.put(KEY_PLANTDATE, plant.plantDate)
        contentValues.put(KEY_LOCATION, plant.location)
        contentValues.put(KEY_LATITUDE, plant.latitude)
        contentValues.put(KEY_LONGITUDE, plant.longitude)
        contentValues.put(KEY_INTVWATER, plant.intvWater)
        contentValues.put(KEY_INTVCUT, plant.intvCut)
        contentValues.put(KEY_INTVFERTILIZE, plant.intvFertilize)

        val result = db.insert(TABLE_PLANT, null, contentValues)
        db.close()
        return result
    }

    fun updatePlant(plant: PlantModel): Int {
        val db = this.writableDatabase

        val contentValues = ContentValues()
        /* Werte aus dem Model Plant-Model */
        /* ID wird automatisch vergeben, wegen PrimaryKey */
        contentValues.put(KEY_NAME, plant.name)
        contentValues.put(KEY_IMAGE, plant.image)
        contentValues.put(KEY_DESCRIPTION, plant.description)
        contentValues.put(KEY_PLANTDATE, plant.plantDate)
        contentValues.put(KEY_LOCATION, plant.location)
        contentValues.put(KEY_LATITUDE, plant.latitude)
        contentValues.put(KEY_LONGITUDE, plant.longitude)
        contentValues.put(KEY_INTVWATER, plant.intvWater)
        contentValues.put(KEY_INTVCUT, plant.intvCut)
        contentValues.put(KEY_INTVFERTILIZE, plant.intvFertilize)

        val success = db.update(TABLE_PLANT, contentValues, KEY_ID + "=" + plant.id, null)
        db.close()
        return success
    }


    @SuppressLint("Range")
    fun getListOfPlants(): ArrayList<PlantModel> {
        val plantList: ArrayList<PlantModel> = ArrayList()
        val selectQuery = "SELECT * FROM $TABLE_PLANT"
        val db = this.readableDatabase

        try {
            val cursor: Cursor = db.rawQuery(selectQuery, null)
            if (cursor.moveToFirst()) {
                /* Min 1 Eintrag gefunden */
                do {
                    val plant = PlantModel(
                        cursor.getInt(cursor.getColumnIndex(KEY_ID)),
                        cursor.getString(cursor.getColumnIndex(KEY_NAME)),
                        cursor.getString(cursor.getColumnIndex(KEY_IMAGE)),
                        cursor.getString(cursor.getColumnIndex(KEY_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(KEY_PLANTDATE)),
                        cursor.getString(cursor.getColumnIndex(KEY_LOCATION)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndex(KEY_LONGITUDE)),
                        cursor.getString(cursor.getColumnIndex(KEY_INTVWATER)),
                        cursor.getString(cursor.getColumnIndex(KEY_INTVCUT)),
                        cursor.getString(cursor.getColumnIndex(KEY_INTVFERTILIZE))


                    )
                    plantList.add(plant)
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: SQLException) {
            db.execSQL(selectQuery)
            return ArrayList()
        }
        return plantList


    }

    fun deletePlant(plantModel: PlantModel) : Int{
        val db = this.writableDatabase
        val success = db.delete(TABLE_PLANT, KEY_ID + "=" + plantModel.id, null)
        db.close()
        return success
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "PlantDatabase"
        private const val TABLE_PLANT = "PlantTable"

        private const val KEY_ID = "_id"
        private const val KEY_NAME = "name"
        private const val KEY_IMAGE = "image"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_PLANTDATE = "plantDate"
        private const val KEY_LOCATION = "location"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_INTVWATER = "intvWater"
        private const val KEY_INTVCUT = "intvCut"
        private const val KEY_INTVFERTILIZE = "intvFertilize"


    }
}