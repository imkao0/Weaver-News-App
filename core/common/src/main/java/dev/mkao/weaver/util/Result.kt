package dev.mkao.weaver.util

import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import timber.log.Timber

/**
 * App-wide error-handling convention: a sealed type that models loading and
 * distinguishes network from HTTP errors.
 */
sealed class Result<out T> {
    data class Success<out T>(val data: T, val message: String? = null) : Result<T>()

    data class GenericError(
        val code: Int? = null,
        val error: ErrorResponse? = null,
        val exception: HttpException,
    ) : Result<Nothing>()

    data class NetworkError(val exception: IOException) : Result<Nothing>()

    data class Loading(val status: Boolean) : Result<Nothing>()

    data class Error(val exception: Throwable? = null, val message: String? = null) :
        Result<Nothing>()
}

data class ErrorResponse(
    val message: String? = null,
    val errors: List<String>? = null,
)

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    apiCall: suspend () -> T,
): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is CancellationException -> throw throwable
                is IOException -> Result.NetworkError(throwable)
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse = parseHttpException(throwable)
                    Result.GenericError(code, errorResponse, throwable)
                }
                else -> {
                    Timber.tag("SafeApiCall")
                        .e(throwable, "Unexpected API call failure: ${throwable.localizedMessage}")
                    // Report unexpected failures as non-fatals; guarded so
                    // JVM unit tests (no FirebaseApp) don't crash.
                    runCatching {
                        FirebaseCrashlytics.getInstance()
                            .recordException(throwable)
                    }
                    Result.Error(
                        throwable,
                        throwable.localizedMessage ?: "An unexpected error occurred",
                    )
                }
            }
        }
    }
}

private fun parseHttpException(httpException: HttpException): ErrorResponse? {
    return try {
        httpException.response()?.errorBody()?.source()?.let { errorSource ->
            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            // GNews returns "errors" as a List<String> or Map<String, String>;
            // parsing as a generic map handles both formats.
            val type = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val adapter = moshi.adapter<Map<String, Any>>(type)
            val rawMap = adapter.fromJson(errorSource)

            val errorsRaw = rawMap?.get("errors")
            val message = when (errorsRaw) {
                is List<*> -> errorsRaw.filterIsInstance<String>().joinToString(". ")
                is Map<*, *> -> errorsRaw.values.filterIsInstance<String>().joinToString(". ")
                is String -> errorsRaw
                else -> rawMap?.get("message") as? String
            }

            ErrorResponse(message = message)
        }
    } catch (exception: Exception) {
        Timber.tag("SafeApiCall").e(exception, "Failed to parse HttpException error body")
        null
    }
}
