package sk.inventory.models

import androidx.paging.PagingSource
import androidx.paging.PagingState
import sk.inventory.api.RetrofitClient
import java.text.SimpleDateFormat
import java.util.*

class WorkplacesPagingSource(
    private val context: android.content.Context
) : PagingSource<Int, WorkplaceResponse>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, WorkplaceResponse> {
        val page = params.key ?: 1
        val pageSize = params.loadSize

        try {
            val apiService = RetrofitClient.create(context)
            val response = apiService.getWorkplaces(page, pageSize)
            if (response.isSuccessful) {
                val body = response.body()
                val total = (body?.get("total") as? Number)?.toInt() ?: 0
                val data = (body?.get("data") as? List<Map<String, Any?>>)?.mapNotNull {
                    it.toWorkplaceResponse()
                } ?: emptyList()
                val prevKey = if (page > 1) page - 1 else null
                val nextKey = if (data.isNotEmpty() && (page * pageSize < total)) page + 1 else null
                return LoadResult.Page(data, prevKey, nextKey)
            } else {
                return LoadResult.Error(Exception("Ошибка: ${response.code()}"))
            }
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    private fun Map<String, Any?>.toWorkplaceResponse(): WorkplaceResponse? {
        return WorkplaceResponse(
            WorkplaceID = (this["workplaceID"] as? Number)?.toInt() ?: return null,
            Name = this["name"] as? String ?: "",
            Description = this["description"] as? String ?: "",
            Location = this["location"] as? String ?: "",
            PC = this["pc"] as? String ?: "",
            Monitor = this["monitor"] as? String ?: "",
            Telephone = this["telephone"] as? String ?: "",
            CreatedAt = (this["createdAt"] as? String)?.let { dateStr ->
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS", Locale.getDefault()).parse(dateStr)
            },
            CreatedBy = this["createdBy"] as? String,
            QrCode = this["qrcode"] as? String
        )
    }

    override fun getRefreshKey(state: PagingState<Int, WorkplaceResponse>): Int? = state.anchorPosition?.let { anchorPosition ->
        state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1) ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
    }
}