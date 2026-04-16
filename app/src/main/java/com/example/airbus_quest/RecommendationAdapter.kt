package com.example.airbus_quest

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

data class RecommendationItem(
    val message: String,
    val aqiLevel: Int,  // 1-5
    val timeAgo: String
)

class RecommendationAdapter(
    private val context: Context,
    private val items: List<RecommendationItem>
) : RecyclerView.Adapter<RecommendationAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewAccentLine: View = view.findViewById(R.id.viewAccentLine)
        val ivAlertIcon: ImageView = view.findViewById(R.id.ivAlertIcon)
        val tvAlertMessage: TextView = view.findViewById(R.id.tvAlertMessage)
        val tvAlertTime: TextView = view.findViewById(R.id.tvAlertTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_recommendation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val colorRes = when (item.aqiLevel) {
            1 -> R.color.aqi_good
            2, 3 -> R.color.aqi_moderate
            4 -> R.color.aqi_unhealthy
            else -> R.color.aqi_hazardous
        }
        val color = ContextCompat.getColor(context, colorRes)

        holder.viewAccentLine.setBackgroundColor(color)
        holder.ivAlertIcon.setColorFilter(color)
        holder.tvAlertMessage.text = item.message
        holder.tvAlertTime.text = item.timeAgo
    }

    override fun getItemCount(): Int = items.size
}