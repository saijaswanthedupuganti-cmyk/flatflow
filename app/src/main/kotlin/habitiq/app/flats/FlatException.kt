package habitiq.app.flats

import java.io.IOException

sealed class FlatException(message: String) : Exception(message)
class FlatNotFoundException : FlatException("Flat not found. Check the invite code and try again.")
class FlatFullException : FlatException("This flat is full (maximum 8 members).")
class AlreadyMemberException : FlatException("You are already a member of this flat.")

fun mapFlatError(exception: Exception): String = when (exception) {
    is FlatException -> exception.message ?: "Something went wrong. Please try again."
    is IOException -> "No internet connection. Please try again."
    else -> "Something went wrong. Please try again."
}
