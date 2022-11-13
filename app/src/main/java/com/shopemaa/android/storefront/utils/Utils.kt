package com.shopemaa.android.storefront.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.core.util.PatternsCompat
import com.google.gson.Gson
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.shopemaa.android.storefront.api.graphql.StoreBySecretQuery
import com.shopemaa.android.storefront.contants.Constants
import com.shopemaa.android.storefront.storage.CacheStorage
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.io.File
import java.util.*
import kotlin.math.roundToInt

object Utils {
    private val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    const val LocalCallbackSuccessUrl = "https://local-callback-success/"
    const val LocalCallbackFailureUrl = "https://local-callback-failure/"

    fun formatAmount(ctx: Context, amount: Int): String {
        return String.format("%s %.2f", getStore(ctx).currency, amount.toDouble() / 100.00)
    }

    fun formatAmount(ctx: Context, amount: Int, reverseCurrency: Boolean): String {
        if (reverseCurrency.not()) {
            return formatAmount(ctx, amount)
        }
        return String.format("%.2f %s", amount.toDouble() / 100.00, getStore(ctx).currency)
    }

    fun discountedPrice(discount: Int, amount: Int): Int {
        return amount - ((discount * amount) / 100)
    }

    fun formatDateWithTime(v: String): String {
        return DateTime.parse(v, dateTimeFormatter).toLocalDateTime().toString("dd/MM/yyyy hh:mm a")
    }

    fun formatDate(v: String): String {
        return DateTime.parse(v, dateTimeFormatter).toLocalDateTime().toString("dd/MM/yyyy")
    }

    fun isEmail(v: String): Boolean {
        return PatternsCompat.EMAIL_ADDRESS.matcher(v).matches()
    }

    fun getScreenWidthDp(ctx: Context): Int {
        return (ctx.resources.displayMetrics.widthPixels / ctx.resources.displayMetrics.scaledDensity).roundToInt()
    }

    fun getScreenHeightDp(ctx: Context): Int {
        return (ctx.resources.displayMetrics.heightPixels / ctx.resources.displayMetrics.scaledDensity).roundToInt()
    }

    fun getStore(ctx: Context): StoreBySecretQuery.StoreBySecret {
        val c = CacheStorage(ctx)
        return Gson().fromJson(
            c.get(Constants.shopLabel),
            StoreBySecretQuery.StoreBySecret::class.java
        )
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    @Throws(Exception::class)
    fun parseQRCodeFromImage(f: File): String {
        var result: String = ""
        try {
            val bitMap =
                BitmapFactory.decodeFile(f.absolutePath) ?: throw Exception("Bitmap is NULL")
            val intArray = JUtil.getIntArray(bitMap.width * bitMap.height)
            bitMap.getPixels(intArray, 0, bitMap.width, 0, 0, bitMap.width, bitMap.height)
            bitMap.recycle()
            val source = RGBLuminanceSource(bitMap.width, bitMap.height, intArray)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val tmpHintsMap: Map<DecodeHintType, Any> = mutableMapOf()
            tmpHintsMap.plus(Pair(DecodeHintType.TRY_HARDER, true))
            tmpHintsMap.plus(
                Pair(
                    DecodeHintType.POSSIBLE_FORMATS,
                    EnumSet.allOf(BarcodeFormat::class.java)
                )
            )
            val reader = MultiFormatReader()
            val decoded = reader.decode(binaryBitmap, tmpHintsMap)
            result = decoded.text
        } catch (e: NotFoundException) {
            try {
                var bitMap =
                    BitmapFactory.decodeFile(f.absolutePath) ?: throw Exception("Bitmap is NULL")
                bitMap = rotateBitmap(bitMap, 90f)
                val intArray = JUtil.getIntArray(bitMap.width * bitMap.height)
                bitMap.getPixels(intArray, 0, bitMap.width, 0, 0, bitMap.width, bitMap.height)
                bitMap.recycle()
                val source = RGBLuminanceSource(bitMap.width, bitMap.height, intArray)
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                val tmpHintsMap: Map<DecodeHintType, Any> = mutableMapOf()
                tmpHintsMap.plus(Pair(DecodeHintType.TRY_HARDER, true))
                tmpHintsMap.plus(
                    Pair(
                        DecodeHintType.POSSIBLE_FORMATS,
                        EnumSet.allOf(BarcodeFormat::class.java)
                    )
                )
                val reader = MultiFormatReader()
                val decoded = reader.decode(binaryBitmap, tmpHintsMap)
                result = decoded.text
            } catch (e: Exception) {
                throw e
            }
        } catch (e: Exception) {
            throw e
        }
        return result
    }
}
