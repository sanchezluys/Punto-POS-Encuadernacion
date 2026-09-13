package com.example.data.model

import java.util.Locale

/**
 * Supported currencies in the workshop.
 * - COP: Colombian Peso
 * - USD: US Dollar
 * - SOL: Peruvian Sol
 */
enum class AppCurrency(
    val code: String,
    val displayName: String,
    val country: String,
    val symbol: String,
    val flagEmoji: String,
    val defaultConversionRate: Double, // Conversion rate relative to 1 USD
    val thousandsSeparator: Char,
    val decimalSeparator: Char
) {
    COP(
        code = "COP",
        displayName = "Peso Colombiano",
        country = "Colombia",
        symbol = "$",
        flagEmoji = "🇨🇴",
        defaultConversionRate = 4000.0,
        thousandsSeparator = '.',
        decimalSeparator = ','
    ),
    USD(
        code = "USD",
        displayName = "Dólar Estadounidense",
        country = "EE. UU. / Internacional",
        symbol = "$",
        flagEmoji = "🇺🇸",
        defaultConversionRate = 1.0,
        thousandsSeparator = ',',
        decimalSeparator = '.'
    ),
    SOL(
        code = "SOL",
        displayName = "Sol Peruano",
        country = "Perú",
        symbol = "S/",
        flagEmoji = "🇵🇪",
        defaultConversionRate = 3.80,
        thousandsSeparator = ',',
        decimalSeparator = '.'
    );

    companion object {
        fun fromCode(code: String): AppCurrency {
            return values().firstOrNull { it.code.equals(code, ignoreCase = true) } ?: USD
        }
    }
}

/**
 * Configuration for currency and numeric formatting.
 */
data class CurrencySettings(
    val currency: AppCurrency = AppCurrency.USD,
    val useThousandsSeparator: Boolean = true,
    val useDecimals: Boolean = true,
    val applyConversionRate: Boolean = false,
    val customExchangeRate: Double = 1.0
)

object CurrencyFormatter {

    /**
     * Formats an amount according to user-defined CurrencySettings:
     * - Currency code and symbol (COP, USD, SOL)
     * - Optional thousands separator (e.g. 1,250 or 1.250 vs 1250)
     * - Optional decimals (2 decimals vs integer rounded)
     * - Optional conversion rate multiplier
     */
    fun format(
        amount: Double,
        settings: CurrencySettings,
        includeCode: Boolean = true
    ): String {
        val rate = if (settings.applyConversionRate) {
            if (settings.customExchangeRate > 0) settings.customExchangeRate else settings.currency.defaultConversionRate
        } else 1.0

        val effectiveAmount = amount * rate
        val isNegative = effectiveAmount < 0
        val positiveAmount = kotlin.math.abs(effectiveAmount)

        val thousandSep = settings.currency.thousandsSeparator
        val decimalSep = settings.currency.decimalSeparator

        val numberString: String = if (settings.useDecimals) {
            val roundedCents = kotlin.math.round(positiveAmount * 100).toLong()
            val intPart = roundedCents / 100
            val fracPart = roundedCents % 100
            val fracString = String.format(Locale.US, "%02d", fracPart)

            val intString = if (settings.useThousandsSeparator) {
                formatWithGroupSeparator(intPart, thousandSep)
            } else {
                intPart.toString()
            }
            "$intString$decimalSep$fracString"
        } else {
            val intPart = kotlin.math.round(positiveAmount).toLong()
            if (settings.useThousandsSeparator) {
                formatWithGroupSeparator(intPart, thousandSep)
            } else {
                intPart.toString()
            }
        }

        val sign = if (isNegative) "-" else ""
        val symbol = settings.currency.symbol

        return when (settings.currency) {
            AppCurrency.COP -> {
                if (includeCode) "$sign$symbol $numberString COP" else "$sign$symbol $numberString"
            }
            AppCurrency.SOL -> {
                "$sign$symbol $numberString"
            }
            AppCurrency.USD -> {
                if (includeCode) "$sign$symbol$numberString USD" else "$sign$symbol$numberString"
            }
        }
    }

    private fun formatWithGroupSeparator(value: Long, separator: Char): String {
        val str = value.toString()
        val len = str.length
        if (len <= 3) return str

        val sb = StringBuilder()
        val firstGroup = if (len % 3 == 0) 3 else len % 3
        sb.append(str.substring(0, firstGroup))

        var i = firstGroup
        while (i < len) {
            sb.append(separator)
            sb.append(str.substring(i, i + 3))
            i += 3
        }
        return sb.toString()
    }
}
