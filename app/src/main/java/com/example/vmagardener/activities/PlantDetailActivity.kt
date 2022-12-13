package com.example.vmagardener.activities

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.vmagardener.R
import com.example.vmagardener.models.PlantModel
import kotlinx.android.synthetic.main.activity_plant_detail.*

class PlantDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plant_detail)

        // Parcelabel anstatt Serializable, weil es 10 x schneller ist
        val plantModelObject = intent.getParcelableExtra(MainActivity.PLANT_OBJECT_DETAILS) as PlantModel?
        if (plantModelObject != null) {
            setSupportActionBar(toolbarPlantDetails)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = plantModelObject.name

            toolbarPlantDetails.setNavigationOnClickListener {
                onBackPressed()
            }
            ivPlaceImage.setImageURI(Uri.parse(plantModelObject.image))
            tvDescription.text = plantModelObject.description
            tvName.text = plantModelObject.name
            ("Schneiden: " + plantModelObject.intvCut).also { tvCut.text = it }
            ("Düngung: " + plantModelObject.intvFertilize).also { tvFertilize.text = it }
            ("Wässerung: " + plantModelObject.intvWater).also { tvWater.text = it }
        }
    }



}