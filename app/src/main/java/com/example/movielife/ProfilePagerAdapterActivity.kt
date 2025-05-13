package com.example.movielife

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.movielife.ui.profile.ProfileHistorialFragment
import com.example.movielife.ui.profile.ProfilePostFragment

class ProfilePagerAdapterActivity(
    fragment: FragmentActivity,
    private val uid: String
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ProfilePostFragment.newInstance(uid)
            1 -> ProfileHistorialFragment.newInstance(uid)
            else -> throw IllegalArgumentException("Invalid position")
        }
    }
}