package pkg.rahulsharmadev.android_info
import android.content.pm.FeatureInfo
import android.content.pm.PackageManager
import android.os.SystemClock
import android.os.Build
import android.content.Context
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import android.provider.Settings
import android.content.ContentResolver
import android.hardware.display.DeviceProductInfo
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import kotlin.collections.HashMap


/**
 * The implementation of [MethodChannel.MethodCallHandler] for the plugin. Responsible for
 * receiving method calls from method channel.
 */
internal class MethodCallHandlerImpl(
    private val packageManager: PackageManager,
    private val windowManager: WindowManager,
    private val applicationContext: Context
) : MethodCallHandler {
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        if (call.method.equals("getAndroidInfo")) {
            val map: MutableMap<String, Any> = HashMap()

            map["board"] = Build.BOARD
            map["bootloader"] = Build.BOOTLOADER
            map["brand"] = Build.BRAND
            map["device"] = Build.DEVICE
            map["fingerprint"] = Build.FINGERPRINT
            map["hardware"] = Build.HARDWARE
            map["host"] = Build.HOST
            map["id"] = Build.ID
            map["model"] = Build.MODEL 
            map["manufacturer"] = Build.MANUFACTURER
            map["buildId"] = Build.DISPLAY // edit
            map["androidId"] = getAndroidId(applicationContext) //add
            map["user"] = Build.USER  //add
            map["product"] = Build.PRODUCT
            map["radioVersion"]=Build.getRadioVersion() //add
            map["elapsedRealtime"] = SystemClock.elapsedRealtime().toInt() //add
           

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                map["supported32BitAbis"] = listOf(*Build.SUPPORTED_32_BIT_ABIS)
                map["supported64BitAbis"] = listOf(*Build.SUPPORTED_64_BIT_ABIS)
                map["supportedAbis"] = listOf<String>(*Build.SUPPORTED_ABIS)
            } else {
                map["supported32BitAbis"] = emptyList<String>()
                map["supported64BitAbis"] = emptyList<String>()
                map["supportedAbis"] = emptyList<String>()
            }

            map["tags"] = Build.TAGS
            map["type"] = Build.TYPE
            map["isPhysicalDevice"] = !isEmulator
            map["systemFeatures"] = getSystemFeatures()

            val version: MutableMap<String, Any> = HashMap()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                version["baseOS"] = Build.VERSION.BASE_OS
                version["previewSdkInt"] = Build.VERSION.PREVIEW_SDK_INT
                version["securityPatch"] = Build.VERSION.SECURITY_PATCH
            }
            version["codename"] = Build.VERSION.CODENAME
            version["incremental"] = Build.VERSION.INCREMENTAL
            version["release"] = Build.VERSION.RELEASE
            version["sdkInt"] = Build.VERSION.SDK_INT
            map["version"] = version

            val display: Display = windowManager.defaultDisplay
            val metrics = DisplayMetrics()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealMetrics(metrics)
            } else {
                display.getMetrics(metrics)
            }

            val displayResult: MutableMap<String, Any> = HashMap()
            displayResult["widthPx"] = metrics.widthPixels.toDouble()
            displayResult["heightPx"] = metrics.heightPixels.toDouble()
            displayResult["xDpi"] = metrics.xdpi
            displayResult["yDpi"] = metrics.ydpi
            displayResult["refreshRate"] = display.getRefreshRate().toInt() //add
            displayResult["isHdrSupport"] = display.isHdr() //add


            map["displayMetrics"] = displayResult

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                map["serialNumber"] = try {
                    Build.getSerial()
                } catch (ex: SecurityException) {
                    Build.UNKNOWN
                }
            } else {
                map["serialNumber"] = Build.SERIAL
            }

            
            result.success(map)

        } else {
            result.notImplemented()
        }
    }


    private fun getAndroidId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
      }


    private fun getSystemFeatures(): List<String> {
        val featureInfos: Array<FeatureInfo> = packageManager.systemAvailableFeatures
        return featureInfos
            .filterNot { featureInfo -> featureInfo.name == null }
            .map { featureInfo -> featureInfo.name }
    }

    /**
     * A simple emulator-detection based on the flutter tools detection logic and a couple of legacy
     * detection systems
     */
    private val isEmulator: Boolean
        get() = ((Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.PRODUCT.contains("sdk")
            || Build.PRODUCT.contains("vbox86p")
            || Build.PRODUCT.contains("emulator")
            || Build.PRODUCT.contains("simulator"))
}
