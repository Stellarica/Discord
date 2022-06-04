package io.github.hydrazinemc.bot

import dev.kord.common.entity.Snowflake

/*
 * avoids this:
 * java.lang.NumberFormatException: Invalid number format: 'null'
	at kotlin.text.StringsKt__StringNumberConversionsKt.numberFormatError(StringNumberConversions.kt:203)
	at kotlin.text.UStringsKt.toULong(UStrings.kt:109)
	at dev.kord.common.entity.Snowflake.<init>(Snowflake.kt:49)
 */
fun getSnowflake(id: String?): Snowflake? {
	id ?: return null
	try { return Snowflake(id) }
	catch (e: NumberFormatException) { return null }
}
fun getSnowflake(id: Long?) = id?.let { Snowflake(it) }
