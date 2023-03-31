package project.stn991614740.grocerymanagerapp

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputBinding
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainActivity : AppCompatActivity() {





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.startButton)
        button.setOnClickListener {
            val Intent = Intent(this, MyFridgeFragment::class.java)
            startActivity(Intent)

            val myfridgefragment = MyFridgeFragment()
            val scanfragment = ScanFragment()
            val dataconfirmation = DataConfirmationFragment()

            val bottomnavigationview = findViewById<BottomNavigationView>(R.id.bottomNavigationView)

            replaceFragment(myfridgefragment)

            bottomnavigationview.setOnItemSelectedListener {
                when(it.itemId){
                    R.id.myfridge -> replaceFragment(myfridgefragment)
                    R.id.scan -> replaceFragment(scanfragment)
                    R.id.confrim -> replaceFragment(dataconfirmation)
                }
                true
            }
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        if(fragment != null){
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.frame_layout, fragment)
            transaction.commit()
    }




    fun fakeTest() {
        //asdf asdf
    }

}
}