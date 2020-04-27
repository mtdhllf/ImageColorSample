package mtdhllf.sample.activityresult

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.invoke
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.github.mtdhllf.kit.coroutines.Run
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var takePhotoObserver: TakePhotoObserver
    private lateinit var takePhotoLiveData: TakePhotoLiveData

    private val startActivity =
        prepareCall(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult? ->
            toast(result?.data?.getStringExtra("value") ?: "")
        }

    private val dial = prepareCall(ActivityResultContracts.Dial()) { result ->
        toast("dial $result")
    }

    private val requestPermission =
        prepareCall(ActivityResultContracts.RequestPermission()) { result ->
            toast("request permission $result")
        }

    private val requestPermissions =
        prepareCall(ActivityResultContracts.RequestPermissions()) { result ->
        }

    private val takePicture = prepareCall(ActivityResultContracts.TakePicture()) { result ->
        toast("take picture : $result")
        photo.setImageBitmap(result)
        Run.onBackground {
            val allColor = ConcurrentHashMap<Int,Int>()
            for (y in (0 until result.height)) {
                for (x in (0 until result.width)){
                    val color = result.getPixel(x, y)
                    if(allColor.containsKey(color)){
                        allColor[color] = allColor[color]!!+1
                    }else{
                        allColor[color] = 1
                    }
                }
            }
            Log.e("color","size="+allColor.keys.size+" width="+result.width+" height="+result.height)

            val sortColor = allColor.toList().sortedByDescending { it.second }.toMap().toMutableMap()

//            sortColor.forEach {
//                Log.d("${it.first}","${it.second}")
//            }

            val hsv1 = FloatArray(3)
            val hsv2 = FloatArray(3)

            Log.i("合并前","${sortColor.size}")


            for (i in (0 until 10)) {
                if(sortColor.size>i){
                    val iterator = sortColor.iterator()
                    var first = iterator.next()

                    (0 until i).forEach { _ ->
                        first = iterator.next()
                    }
                    while (iterator.hasNext()){
                        val second = iterator.next()
                        Color.colorToHSV(first.key,hsv1)
                        Color.colorToHSV(second.key,hsv2)
                        if(checkColorSimilar(hsv1,hsv2)){
                            first.setValue(first.value+second.value)
                            iterator.remove()
                        }
                    }
                }
            }


            Log.i("合并后","${sortColor.size}")

            sortColor.toList().sortedByDescending { it.second }.take(10).forEachIndexed { index, it ->
                val hsv = FloatArray(3)
                Color.colorToHSV(it.first,hsv)
                Log.e("${it.first}-----${it.second}","h=${hsv[0]} s=${hsv[1]} v=${hsv[2]}")

                Run.onUiASync {
                    val button = when (index) {
                        0 -> color1
                        1 -> color2
                        2 -> color3
                        3 -> color4
                        4 -> color5
                        5 -> color6
                        6 -> color7
                        7 -> color8
                        8 -> color9
                        else -> color10
                    }
                    button.setBackgroundColor(it.first)
                    button.text = "${it.second}"
                }
            }

        }
    }

    private fun checkColorSimilar(hsv1: FloatArray, hsv2: FloatArray): Boolean {
        val similarH = 10f
        val similarS = 0.05
        val similarV = 0.05
        when {
            hsv1[0] > 360 - similarH && hsv2[0] < similarH -> {
                hsv1[0] = 360f - hsv1[0]
            }
        }
        return when {
            abs(hsv1[0] - hsv2[0]) < similarH -> true
            abs(hsv1[0] - hsv2[0]) < similarH * 2 && abs(hsv1[1] - hsv2[1]) < similarS -> true
            abs(hsv1[0] - hsv2[0]) < similarH * 2 && abs(hsv1[2] - hsv2[2]) < similarV -> true
            else -> false
        }

    }

    private class TakePicDrawable : ActivityResultContract<Void, Drawable>() {

        override fun createIntent(input: Void?): Intent {
            return Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Drawable? {
            if (resultCode != Activity.RESULT_OK || intent == null) return null
            val bitmap = intent.getParcelableExtra<Bitmap>("data")
            return BitmapDrawable(bitmap)
        }
    }

    private val takePictureCustom = prepareCall(TakePicDrawable()) { result ->
        toast("take picture : $result")
        photo.setImageDrawable(result)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        takePhotoLiveData = TakePhotoLiveData(activityResultRegistry)
        takePhotoLiveData.observeForever(Observer { bitmap ->
            photo.setImageBitmap(bitmap)
        })

        takePhotoObserver = TakePhotoObserver(activityResultRegistry) { bitmap ->
            photo.setImageBitmap(bitmap)
        }
        lifecycle.addObserver(takePhotoObserver)

        jumpBt.setOnClickListener { startActivity.launch(Intent(this, SecondActivity::class.java)) }
        permissionBt.setOnClickListener { requestPermission.launch(Manifest.permission.READ_PHONE_STATE) }
        dialBt.setOnClickListener { dial("123456789") }
        pictureBt.setOnClickListener { takePicture() }
        pictureCustomBt.setOnClickListener { takePictureCustom() }
        customObserverBt.setOnClickListener { takePhotoObserver.takePicture() }
        liveDataBt.setOnClickListener { takePhotoLiveData.takePhotoLauncher() }
    }

}
