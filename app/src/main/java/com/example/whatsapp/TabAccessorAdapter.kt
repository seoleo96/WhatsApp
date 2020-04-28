package com.example.whatsapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class TabAccessorAdapter : FragmentPagerAdapter {
    constructor(fm: FragmentManager) : super(fm)

    override fun getItem(position: Int): Fragment {

       return when (position) {
            0 -> {
                return ChatFragment()
            }

            1 -> {
                return GroupFragment()
            }

            2 -> {
                return ContactFragment()
            }
           else -> {
                return RequestFragment()
            }
        }
    }

    override fun getCount(): Int {
        return 4
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> {
                return "Chat"
            }

            1 -> {
                return "Group"
            }

            2 -> {
                return "Contact"
            }

            else -> {
                return "Request"
            }
        }
    }
}