package mtdhllf.sample.activityresult

import android.graphics.Bitmap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LiveData

class TakePhotoLiveData(private val registry: ActivityResultRegistry) : LiveData<Bitmap>() {

    lateinit var takePhotoLauncher: ActivityResultLauncher<Void?>

    override fun onActive() {
        super.onActive()
        takePhotoLauncher = registry.registerActivityResultCallback(
            "key",
            ActivityResultContracts.TakePicture()
        ) { result ->
            value = result
        }
    }

    override fun onInactive() {
        super.onInactive()
        takePhotoLauncher.dispose()
    }

}