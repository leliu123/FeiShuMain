import android.annotation.SuppressLint
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity

/**
 * Compose 尺寸适配工具类
 */
@SuppressLint("InternalInsetResource", "LocalContextResourcesRead")
object SizeUtil {

    @Composable
    fun dp2px(dp: Dp): Int {
        val density = LocalDensity.current
        return with(density) { dp.roundToPx() }
    }

    @Composable
    fun px2dp(px: Int): Dp {
        val density = LocalDensity.current
        return with(density) { px.toDp() }
    }

    @Composable
    fun getScreenWidthDp(): Dp {
        val context = LocalContext.current
        val displayMetrics = context.resources.displayMetrics
        return (displayMetrics.widthPixels / displayMetrics.density).dp
    }

    // 获取状态栏高度（dp，适配不同机型）
    @Composable
    fun getStatusBarHeightDp(): Dp {
        val context = LocalContext.current
        val resourceId = context.resources.getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        return if (resourceId > 0) {
            (context.resources.getDimensionPixelSize(resourceId) / context.resources.displayMetrics.density).dp
        } else 0.dp
    }

    // 导航栏高度（dp）
    @Composable
    fun getNavBarHeightDp(): Dp {
        val context = LocalContext.current
        val resourceId = context.resources.getIdentifier(
            "navigation_bar_height", "dimen", "android"
        )
        return if (resourceId > 0) {
            (context.resources.getDimensionPixelSize(resourceId) / context.resources.displayMetrics.density).dp
        } else 0.dp
    }
}