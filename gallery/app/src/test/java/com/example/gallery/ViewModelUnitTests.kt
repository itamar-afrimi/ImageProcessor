
import android.content.ContentProvider
import android.content.ContentResolver
import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.gallery.data.*
import com.example.gallery.domain.ImageProcessorRepository
import com.example.gallery.domain.RequestBodyBuilder
import com.example.gallery.presentation.viewmodel.ApiCallStatus
import com.example.gallery.presentation.viewmodel.GalleryImageViewModel
import com.google.common.truth.Truth
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import okhttp3.RequestBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest=Config.NONE)
@OptIn(ExperimentalCoroutinesApi::class)

class GalleryImageViewModelTest {

    private lateinit var repository: FakeImageProcessorRepository
    private lateinit var requestBodyBuilder: RequestBodyBuilder
    private lateinit var mockContentResolver : ContentResolver

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        repository = FakeImageProcessorRepository()
        requestBodyBuilder = mockk<RequestBodyBuilder>()
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }
    @After
    fun after() {
        Dispatchers.resetMain()
    }
//
    @Test
    fun `uploadReq should update statusString and set jobId and url`() = runTest {
        val viewModel = GalleryImageViewModel(requestBodyBuilder, repository,
            UnconfinedTestDispatcher()
        )
        val jobRequest = "test-job-request"
        val uploadRequest = UploadReq( "MyFile.jpg","test-job-request")

        viewModel.uploadReq(jobRequest)
//        print(viewModel.statusString.value)
        Truth.assertThat(viewModel.statusString.value).isEqualTo(ApiCallStatus.URL_FOR_UPLOAD_FETCHED)
        Truth.assertThat(viewModel.url).isEqualTo(uploadRequest.url)
        Truth.assertThat(viewModel.jobId).isEqualTo(uploadRequest.jobId)

    }

    @Test
    fun `uploadImage should update statusString to ERROR_NO_URL when url is  null`() = runTest {
        val viewModel = GalleryImageViewModel(requestBodyBuilder, repository,
            UnconfinedTestDispatcher()
        )
        val uri : Uri? = null
        every { requestBodyBuilder.build(any()) }returns null
        viewModel.test = -1
        viewModel.uploadImage(uri)

        assertEquals(ApiCallStatus.ERROR_NO_URL, viewModel.statusString.value)
        assertNull(viewModel.url)
        assertNull(viewModel.jobId)
    }

    @Test
    fun `uploadImage should update statusString to IMAGE_UPLOADED when url is ok`() = runTest {
        val uri = Uri.parse("fakeuri")
        val viewModel = GalleryImageViewModel(requestBodyBuilder, repository,
            UnconfinedTestDispatcher()
        )
        every { requestBodyBuilder.build(any()) }returns null
        viewModel.test = 1
        viewModel.url = uri.toString()
        viewModel.uploadReq("hi")
        viewModel.uploadImage(uri)
        assertEquals(ApiCallStatus.WAITING_FOR_RESPONSE, viewModel.statusString.value)
        assertEquals("hi" , viewModel.url)
        assertEquals("MyFile.jpg", viewModel.jobId)

    }

}
class FakeImageProcessorRepository : ImageProcessorRepository(ProcessImageApi.instance)
{

     override suspend fun uploadReq(fileName: String, jonRequest: String): Result<UploadReq> {
        val file = UploadReq(fileName, jonRequest)
        print(file)
        return Result.success(file)
    }

    override suspend fun uploadImage(url: String, body: RequestBody): Result<Unit> {

//        return if (url.equals(null)){
//            Result.failure(Throwable())
//
//        } else {
           return Result.success(Unit)

//        }
    }}

    suspend fun getProcessedImageUrl(jobId: String): Result<ProcessedImageUrlResults> {
        val response = ProcessImageApi.instance.getProcessedImageUrl()
        return when {
            response.isSuccessful && response.body() != null -> {
                val body: ProcessedImageReqResponse = response.body()!!
                print(body)
                when (body.url) {
                    null -> when (body.status) {
                        null -> Result.failure(UploadImageError("Error: can't get image processing status"))
                        else -> Result.success(ProcessedImageUrlResults.Processing(status = body.status))
                    }
                    else -> Result.success(ProcessedImageUrlResults.Ready(url = body.url))
                }
            }
            else -> {
                when {
                    response.code() == 404 -> Result.failure(UploadImageError("JobId does not exist"))
                    else -> Result.failure(UploadImageError("Error: can't get processed image url"))
                }
            }
        }
    }
