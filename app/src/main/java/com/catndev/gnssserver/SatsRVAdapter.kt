package com.catndev.gnssserver

import android.location.GnssStatus
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SatsRVAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val typeHeader = 0
    private val typeItem = 1
    var satellitesList: List<LocationService.Satellite> = emptyList<LocationService.Satellite>()

    class SatHeader(itemView: View, parent: ViewGroup, b: Boolean) : RecyclerView.ViewHolder(itemView) {}

    class SatHolder(itemView: View, parent: ViewGroup, b: Boolean) : RecyclerView.ViewHolder(itemView) {

        private val azimuthTextView: TextView = itemView.findViewById(R.id.azimuth_textView)
        private val carrierTextView: TextView = itemView.findViewById(R.id.carrier_textView)
        private val codeTextView: TextView = itemView.findViewById(R.id.code_textView)
        private val cn0TextView: TextView = itemView.findViewById(R.id.cn0_textView)
        private val cn0ProgressBar: ProgressBar = itemView.findViewById(R.id.cn0_progressBar)
        private val elevationTextView: TextView = itemView.findViewById(R.id.elevation_textView)
        private val idTextView: TextView =  itemView.findViewById(R.id.id_textView)

        fun setInfo(sat: LocationService.Satellite) {
            azimuthTextView.text = String.format("%.0f°", sat.azimuth)
            carrierTextView.text = sat.carrierBand
            codeTextView.text = when (sat.constellationType) {
                GnssStatus.CONSTELLATION_BEIDOU -> "\uD83C\uDDE8\uD83C\uDDF3"
                GnssStatus.CONSTELLATION_GPS -> "\uD83C\uDDFA\uD83C\uDDF8"
                GnssStatus.CONSTELLATION_GALILEO -> "\uD83C\uDDEA\uD83C\uDDFA"
                GnssStatus.CONSTELLATION_GLONASS -> "\uD83C\uDDF7\uD83C\uDDFA"
                GnssStatus.CONSTELLATION_IRNSS -> "\uD83C\uDDEE\uD83C\uDDF3"
                GnssStatus.CONSTELLATION_QZSS -> "\uD83C\uDDEF\uD83C\uDDF5"
                else -> {"\uD83D\uDEF0"}
            }
            cn0TextView.text = String.format("%.1f", sat.cn0)
            cn0ProgressBar.progress = (sat.cn0 * 10).toInt()
            elevationTextView.text = String.format("%.0f°", sat.elevation)
            idTextView.text = sat.id.toString()
        }
    }

    override fun getItemCount(): Int {
        return satellitesList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) typeHeader else typeItem
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if ((holder is SatHolder) && (position > 0) && (position < (satellitesList.size + 1))) {
            holder.setInfo(satellitesList[position - 1])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return if (viewType == typeItem) {
            SatHolder(layoutInflater.inflate(R.layout.satellite_item, parent, false), parent, false)
        } else SatHeader(layoutInflater.inflate(R.layout.sat_header_item, parent, false), parent, false)
    }


}