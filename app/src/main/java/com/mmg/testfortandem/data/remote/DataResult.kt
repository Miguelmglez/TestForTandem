package com.mmg.testfortandem.data.remote

/**
 * A sealed class representing the result of a data operation, which can be either a success with data or a failure with an error.
 * This abstraction allows the data layer to communicate results to the presentation layer without exposing implementation details
 * of how data is fetched or what specific errors occurred.
 */
sealed interface DataResult<out T> {
    data class Success<T>(val data: T) : DataResult<T>
    data class Failure(val error: DataError) : DataResult<Nothing>
}

/**
 * A sealed class representing the various types of errors that can occur during data operations.
 * This abstraction allows the data layer to categorize errors in a way that the presentation layer can understand and react to,
 * without needing to know the specifics of the underlying exceptions or error codes.
 */
sealed interface DataError {
    data object NoConnection : DataError
    data object Timeout : DataError
    data class Server(val code: Int) : DataError
    data class Unknown(val cause: Throwable) : DataError
}