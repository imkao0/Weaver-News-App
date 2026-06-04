package dev.mkao.weaver

import dev.mkao.weaver.util.Result
import dev.mkao.weaver.util.safeApiCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class SafeApiCallTest {

    private val testDispatcher = StandardTestDispatcher()

    @Test
    fun `safeApiCall returns Success when apiCall is successful`() = runTest(testDispatcher) {
        val expectedData = "Success Data"
        val result = safeApiCall(testDispatcher) { expectedData }

        assertTrue(result is Result.Success)
        assertEquals(expectedData, (result as Result.Success).data)
    }

    @Test
    fun `safeApiCall returns NetworkError when IOException is thrown`() = runTest(testDispatcher) {
        val exception = IOException("Network Failure")
        val result = safeApiCall(testDispatcher) { throw exception }

        assertTrue(result is Result.NetworkError)
        assertEquals(exception, (result as Result.NetworkError).exception)
    }

    @Test
    fun `safeApiCall returns GenericError when HttpException is thrown`() = runTest(testDispatcher) {
        val response = Response.error<String>(404, "Not Found".toResponseBody())
        val exception = HttpException(response)
        val result = safeApiCall(testDispatcher) { throw exception }

        assertTrue(result is Result.GenericError)
        assertEquals(404, (result as Result.GenericError).code)
    }

    @Test
    fun `safeApiCall returns Error when unexpected exception is thrown`() = runTest(testDispatcher) {
        val exception = IllegalArgumentException("Unexpected error")
        val result = safeApiCall(testDispatcher) { throw exception }

        assertTrue(result is Result.Error)
        assertEquals(exception, (result as Result.Error).exception)
    }
}
