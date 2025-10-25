package sh.lem.ccholo.util

fun tryParseUuid(uuid: String) = try {
  java.util.UUID.fromString(uuid)
} catch (e: IllegalArgumentException) {
  null
}
