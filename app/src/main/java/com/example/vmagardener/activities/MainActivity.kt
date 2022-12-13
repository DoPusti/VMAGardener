package com.example.vmagardener.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vmagardener.R
import com.example.vmagardener.adapters.PlantAdapter
import com.example.vmagardener.database.DatabaseHandler
import com.example.vmagardener.models.PlantModel
import com.example.vmagardener.utils.SwipeToDeleteCallback
import com.example.vmagardener.utils.SwipeToEditCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /* Direkter Zugriff über kotlin-android-extensions */
        fabAddPlant.setOnClickListener {
            val intent = Intent(this, AddPlantActivity::class.java)
            startActivityForResult(intent, ADD_PLANT_ACTIVITY_REQUEST_CODE)

        }
        getPlantListFromLocalDB()


    }

    private fun setupPlantRecyclerView(plantList: ArrayList<PlantModel>) {
        rvPlantList.layoutManager = LinearLayoutManager(this)

        rvPlantList.setHasFixedSize(true)
        val plantAdaper = PlantAdapter(this, plantList)
        rvPlantList.adapter = plantAdaper

        plantAdaper.setOnClickListener(object : PlantAdapter.OnClickListener {
            override fun onClick(position: Int, model: PlantModel) {
                val intent = Intent(this@MainActivity, PlantDetailActivity::class.java)
                intent.putExtra(PLANT_OBJECT_DETAILS,model)
                startActivity(intent)
            }
        })
        val editSwipeHandler = object  : SwipeToEditCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rvPlantList.adapter as PlantAdapter
                adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition,
                    ADD_PLANT_ACTIVITY_REQUEST_CODE)
            }

        }
        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(rvPlantList)


        val deleteSwipeHandler = object  : SwipeToDeleteCallback(this) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val adapter = rvPlantList.adapter as PlantAdapter
                adapter.removeAt(viewHolder.adapterPosition)
                getPlantListFromLocalDB()
            }

        }
        val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        deleteItemTouchHelper.attachToRecyclerView(rvPlantList)
    }

    private fun getPlantListFromLocalDB() {
        val dbHandler = DatabaseHandler(this)
        val getPlantList = dbHandler.getListOfPlants()
        if (getPlantList.size > 0) {
            for (i in getPlantList) {
                i.name?.let { Log.e("DBAusgabe Name", it) }
                i.description?.let { Log.e("DBAusgabe Description", it) }
                i.plantDate?.let { Log.e("DBAusgabe Date", it) }

            }
            rvPlantList.visibility = View.VISIBLE
            tvNoRecordsAvailable.visibility = View.GONE
            setupPlantRecyclerView(getPlantList)
        } else {
            rvPlantList.visibility = View.GONE
            tvNoRecordsAvailable.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_PLANT_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                getPlantListFromLocalDB()
            } else {
                Log.e("Activity", "Abgebrochen oder zurück gedrückt")
            }
        }

    }

    companion object {
        private const val ADD_PLANT_ACTIVITY_REQUEST_CODE = 1
        const val PLANT_OBJECT_DETAILS = "PLANT_DETAILS"
    }

}