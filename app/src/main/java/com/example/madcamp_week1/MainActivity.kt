package com.example.madcamp_week1

import androidx.appcompat.app.AppCompatActivity
import android.os. Bundle
import androidx.fragment.app.Fragment
import com.google.android.material. tabs.TabLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val phoneBookFragment: Fragment = phoneBookFragment()
        val imageFragment: Fragment = imageFragment()
        val freeFragment: Fragment = freeFragment()


        supportFragmentManager.beginTransaction().replace(R.id.main_view, phoneBookFragment).commit()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                when(tab.position) {
                    0 -> {
                        supportFragmentManager.beginTransaction().replace(R.id.main_view, phoneBookFragment).commit()
                    }
                    1 -> {
                        supportFragmentManager.beginTransaction().replace(R.id.main_view, imageFragment).commit()
                    }
                    2 -> {
                        supportFragmentManager.beginTransaction().replace(R.id.main_view, freeFragment).commit()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
    }
}