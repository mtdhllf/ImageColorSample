package mtdhllf.sample.activityresult

import android.graphics.Bitmap
import androidx.activity.invoke
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

class TakePhotoObserver(
    private val registry: ActivityResultRegistry,
    private val func: (Bitmap) -> Unit
) : DefaultLifecycleObserver {

    private lateinit var takePhotoLauncher: ActivityResultLauncher<Void?>

    override fun onCreate(owner: LifecycleOwner) {
        takePhotoLauncher = registry.registerActivityResultCallback(
            "key",
            ActivityResultContracts.TakePicture()
        ) { bitmap ->
            func(bitmap)
        }
    }

    fun takePicture(){
        takePhotoLauncher()
    }
}