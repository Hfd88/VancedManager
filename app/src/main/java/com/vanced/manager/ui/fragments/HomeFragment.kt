package com.vanced.manager.ui.fragments

import android.animation.ObjectAnimator
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.TextView
import androidx.core.animation.addListener
import androidx.viewpager2.widget.ViewPager2
import com.dezlum.codelabs.getjson.GetJson
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.JsonObject
import com.vanced.manager.R
import com.vanced.manager.adapter.SectionPageAdapter
import com.vanced.manager.core.fragments.Home

class HomeFragment : Home() {

    private lateinit var sectionPageAdapter: SectionPageAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = getString(R.string.title_home)
        setHasOptionsMenu(true)

        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        connectionStatus()
        checkNetwork()

        super.onViewCreated(view, savedInstanceState)

        sectionPageAdapter = SectionPageAdapter(this)
        val tabLayout = view.findViewById(R.id.tablayout) as TabLayout
        viewPager = view.findViewById(R.id.viewpager)
        viewPager.adapter = sectionPageAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Vanced"
                1 -> tab.text = "MicroG"
                2 -> tab.text = "Manager"
            }
        }.attach()

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)
        super .onCreateOptionsMenu(menu, inflater)
    }

    private fun checkNetwork() {
        if (!GetJson().isConnected(requireContext())) {

            netUnavailable()

        }

    }

    private var networkCallback = object: ConnectivityManager.NetworkCallback() {

        override fun onLost(network: Network) {
            super.onLost(network)

            netUnavailable()

        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)

            val microginstallbtn = view?.findViewById<Button>(R.id.microg_installbtn)
            val vancedinstallbtn = view?.findViewById<Button>(R.id.vanced_installbtn)

            microginstallbtn?.visibility = View.VISIBLE
            vancedinstallbtn?.visibility = View.VISIBLE

            val microgLatestTxt = view?.findViewById<TextView>(R.id.microg_latest_version)
            val vancedLatestTxt = view?.findViewById<TextView>(R.id.vanced_latest_version)

            val vancedVer: JsonObject = GetJson().AsJSONObject("https://x1nto.github.io/VancedFiles/vanced.json")
            val microgVer: JsonObject = GetJson().AsJSONObject("https://x1nto.github.io/VancedFiles/microg.json")
            vancedLatestTxt?.text = vancedVer.get("version").asString
            microgLatestTxt?.text = microgVer.get("version").asString

            activity?.runOnUiThread {

                val networkErrorLayout = view?.findViewById<MaterialCardView>(R.id.home_network_wrapper)
                val oa2 = ObjectAnimator.ofFloat(networkErrorLayout, "yFraction", 0f, 0.3f)
                val oa3 = ObjectAnimator.ofFloat(networkErrorLayout, "yFraction", 0.3f, -1f)

                oa2.start()
                oa3.apply {
                    oa3.addListener(onEnd = {
                        networkErrorLayout?.visibility = View.GONE
                    })
                    start()
                }


            }

        }

    }

    private fun netUnavailable() {

        val microginstallbtn = view?.findViewById<Button>(R.id.microg_installbtn)
        val vancedinstallbtn = view?.findViewById<Button>(R.id.vanced_installbtn)

        microginstallbtn?.visibility = View.GONE
        vancedinstallbtn?.visibility = View.GONE

        val vancedLatestTxt = view?.findViewById<TextView>(R.id.vanced_latest_version)
        val microgLatestTxt = view?.findViewById<TextView>(R.id.microg_latest_version)

        if (GetJson().isConnected(requireContext())) {

            vancedLatestTxt?.text = getString(R.string.unavailable)
            microgLatestTxt?.text = getString(R.string.unavailable)

            val networkErrorLayout = view?.findViewById<MaterialCardView>(R.id.home_network_wrapper)
            if (networkErrorLayout?.visibility != View.VISIBLE) {

                activity?.runOnUiThread {

                    val oa2 = ObjectAnimator.ofFloat(networkErrorLayout, "yFraction", -1f, 0.3f)
                    val oa3 = ObjectAnimator.ofFloat(networkErrorLayout, "yFraction", 0.3f, 0f)


                    oa2.apply {
                        oa2.addListener(onStart = {
                            networkErrorLayout?.visibility = View.VISIBLE
                        })
                        start()
                    }
                    oa3.start()

                }
            }

        }
    }

    private fun connectionStatus() {
        val connectivityManager = context?.applicationContext?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {}
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)
    }

}

