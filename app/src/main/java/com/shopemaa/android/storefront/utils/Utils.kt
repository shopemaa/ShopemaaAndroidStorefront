package com.shopemaa.android.storefront.utils

import android.content.Context
import androidx.core.util.PatternsCompat
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import kotlin.math.roundToInt

object Utils {
    private val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")

    fun formatAmount(amount: Int): String {
        return String.format("BDT %.2f", amount.toDouble() / 100.00)
    }

    fun formatAmount(amount: Int, reverseCurrency: Boolean): String {
        if (reverseCurrency.not()) {
            return formatAmount(amount)
        }
        return String.format("%.2f BDT", amount.toDouble() / 100.00)
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
}
