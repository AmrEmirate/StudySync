package id.studysynsamr.studysyns

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import id.studysynsamr.studysyns.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeFragment())
                    true
                }
                R.id.navigation_calendar -> {
                    loadFragment(CalendarFragment())
                    true
                }
                R.id.navigation_notifications -> {
                    loadFragment(NotificationsFragment())
                    true
                }
                R.id.navigation_settings -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
        
        if (savedInstanceState == null) {
            binding.navView.selectedItemId = R.id.navigation_home
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }
}