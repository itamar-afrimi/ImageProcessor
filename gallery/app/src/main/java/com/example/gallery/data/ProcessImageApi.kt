package com.example.gallery.data

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

interface ProcessImageApi {

    @POST("/")
    suspend fun uploadReq(
        @Body body: FileNameJson
    ): Response<UploadReqResponse>

    @PUT
    suspend fun uploadImage(
        @Url url: String,
        @Body body: RequestBody
    ) : Response<Unit>

    @GET
    suspend fun getProcessedImageUrl(
        @Url url: String = BASE_PROCESSED_URL,
        @Query("jobId") jobId: String,
    ) : Response<ProcessedImageReqResponse>


    companion object{
        // TODO need to be url of uploading lambda
        private const val BASE_UPLOAD_URL = "https://plvcauj4kbsipvm24mang44dii0dasqq.lambda-url.us-west-2.on.aws/" // todo change
        // TODO need to be url of getter lambda.
        const val BASE_PROCESSED_URL = "https://qfx63bv277m3vp6r2ujcrwsrga0uvzzt.lambda-url.us-west-2.on.aws/" // todo change

        val instance: ProcessImageApi by lazy {
            val retrofit: Retrofit = createRetrofit()
            retrofit.create(ProcessImageApi::class.java)
        }

        private fun createRetrofit(): Retrofit {

            // Create converter
            val moshi: Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

            // Create logger
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)

            // Create client
            val httpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .build()

            // Build Retrofit
            return Retrofit.Builder()
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .baseUrl(BASE_UPLOAD_URL)
                .client(httpClient)
                .build()
        }
    }
}
