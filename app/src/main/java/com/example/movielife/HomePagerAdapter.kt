package com.example.movielife

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.movielife.ui.home.ParatiHomeFragment
import com.example.movielife.ui.home.SeguidosHomeFragment
import com.example.movielife.ui.profile.ProfileHistorialFragment
import com.example.movielife.ui.profile.ProfilePostFragment

class HomePagerAdapter(
    fragment: Fragment,
    private val uid: String
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ParatiHomeFragment.newInstance(uid)
            1 -> SeguidosHomeFragment.newInstance(uid)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}