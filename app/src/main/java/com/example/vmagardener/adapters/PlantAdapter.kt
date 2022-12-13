package com.example.vmagardener.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vmagardener.R
import com.example.vmagardener.activities.AddPlantActivity
import com.example.vmagardener.activities.MainActivity
import com.example.vmagardener.database.DatabaseHandler
import com.example.vmagardener.models.PlantModel
import kotlinx.android.synthetic.main.item_plant.view.*

open class PlantAdapter(
    private val context: Context,
    private var list: ArrayList<PlantModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_plant,
                parent,
                false
            )
        )
    }

    // Adapter kann keine OnClicklistener haben, daher dieser Umweg
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]

        if (holder is MyViewHolder) {
            holder.itemView.ivPlaceImage.setImageURI(Uri.parse(model.image))
            holder.itemView.tvName.text = model.name
            holder.itemView.setOnClickListener {
                if (onClickListener != null) {
                    onClickListener!!.onClick(position,model)
                }
            }
        }
    }

    fun removeAt(position: Int) {
        val dbHandler = DatabaseHandler(context)
        val isDeleted = dbHandler.deletePlant(list[position])
        if(isDeleted > 0 ) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }

    }
    fun notifyEditItem(activity : Activity,position: Int, requestCode : Int) {
        val intent = Intent(context, AddPlantActivity::class.java)
        intent.putExtra(MainActivity.PLANT_OBJECT_DETAILS, list[position])
        activity.startActivityForResult(intent, requestCode)
        notifyItemChanged(position)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    interface OnClickListener {
        fun onClick(position: Int, model: PlantModel) {

        }
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

}