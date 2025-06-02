package com.example.movielife.ui.adapters

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.movielife.ui.actor.DetailActorPeliculaFragment
import com.example.movielife.ui.actor.DetailActorSerieFragment

class PeliculaSerieViewPagerAdapter(
    activity: AppCompatActivity,
    private val actorId: Int,
    private val role: String
) : FragmentStateAdapter(activity) {

    override fun getItemCount() = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> DetailActorPeliculaFragment.newInstance(actorId, role)
            1 -> DetailActorSerieFragment.newInstance(actorId, role)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}
