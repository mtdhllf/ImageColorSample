package mtdhllf.sample.activityresult

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_second.*

class SecondActivity : AppCompatActivity(R.layout.activity_second){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        back.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent().putExtra("value","I am back !"))
            finish()
        }
    }
}