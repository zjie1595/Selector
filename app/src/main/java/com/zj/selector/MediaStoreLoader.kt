package com.zj.selector

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log

private const val COLUMN_COUNT = "count"
private const val COLUMN_BUCKET_ID = "bucket_id"
private const val COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name"

private const val TAG = "MediaStore查询"

data class Bucket(
    val bucketId: String,
    val displayName: String,
    val itemCount: Int,
    val coverUri: Uri
)

data class Gif(
    val displayName: String,
    val dateAdded: Long,
    val dateModified: Long,
    val photoUri: Uri
)

/**
 * 查询步骤：
 * 1、看看都有哪些文件夹
 * 2、根据文件夹再进行进一步加载
 */

/**
 * 加载包含GIF图片的文件夹列表
 * @receiver Context
 * @return MutableList<Bucket>
 */
fun Context.loadGifBucketList(): MutableList<Bucket> {
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        COLUMN_BUCKET_DISPLAY_NAME,
        COLUMN_BUCKET_ID,
        "COUNT(*) AS $COLUMN_COUNT"
    )
    val selection = "${MediaStore.MediaColumns.MIME_TYPE} = ?) GROUP BY ($COLUMN_BUCKET_ID"
    val selectionArgs = arrayOf(
        "image/gif"
    )
    val sortOrder = "${MediaStore.MediaColumns._ID} DESC"
    val bucketList = mutableListOf<Bucket>()
    contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
        val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
        val bucketIdColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_BUCKET_ID)
        val bucketDisplayNameColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_BUCKET_DISPLAY_NAME)
        val countColumnIndex = cursor.getColumnIndexOrThrow(COLUMN_COUNT)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumnIndex)
            val bucketId = cursor.getString(bucketIdColumnIndex)
            val bucketDisplayName = cursor.getString(bucketDisplayNameColumnIndex)
            val count = cursor.getInt(countColumnIndex)
            val coverUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            bucketList += Bucket(bucketId, bucketDisplayName, count, coverUri)
            Log.i(
                TAG,
                "id = $id bucketId = $bucketId bucketDisplayName = $bucketDisplayName coverUri = $coverUri count = $count"
            )
        }
    }
    Log.i(
        TAG,
        "load bucket list info finish --------------------------------------------------------------------------------"
    )
    return bucketList
}

/**
 * 查询某个文件夹下的GIF图片集合
 * @receiver Context
 * @param bucketId String           文件夹id
 * @param orderColumnName String    排序列名
 * @param order String              排序方式，DESC：降序，ASC：升序
 * @param offset Int                分页偏移个数，从0开始
 * @param limit Int                 分页每页加载个数，默认100
 */
fun Context.loadGifListInBucket(
    bucketId: String,
    orderColumnName: String = MediaStore.MediaColumns.DATE_MODIFIED,
    order: String = "DESC",
    offset: Int = 0,
    limit: Int = 100
): MutableList<Gif> {
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.DATE_MODIFIED,
        MediaStore.MediaColumns.DATE_ADDED,
    )
    val selection =
        "${MediaStore.MediaColumns.MIME_TYPE} = ?) AND (${COLUMN_BUCKET_ID} = ?"
    val selectionArgs = arrayOf(
        "image/gif", bucketId
    )
    val sortOrder = "$orderColumnName $order LIMIT $limit OFFSET $offset"
    val gifList = mutableListOf<Gif>()
    contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
        val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
        val displayNameColumnIndex =
            cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
        val dateAddedColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
        val dateModifiedColumnIndex =
            cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumnIndex)
            val displayName = cursor.getString(displayNameColumnIndex)
            val dateAdded = cursor.getLong(dateAddedColumnIndex)
            val dateModified = cursor.getLong(dateModifiedColumnIndex)
            val gifUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            gifList += Gif(displayName, dateAdded, dateModified, gifUri)
            Log.i(TAG, "gifUri = $gifUri")
        }
    }
    return gifList
}

/**
 * 查询所有的GIF图片
 * @receiver Context
 * @param orderColumnName String    排序列名
 * @param order String              排序方式，DESC：降序，ASC：升序
 * @param offset Int                分页偏移个数，从0开始
 * @param limit Int                 分页每页加载个数，默认100
 */
fun Context.loadAllGif(
    orderColumnName: String = MediaStore.MediaColumns.DATE_MODIFIED,
    order: String = "DESC",
    offset: Int = 0,
    limit: Int = 100
): MutableList<Gif> {
    val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(
        MediaStore.MediaColumns._ID,
        MediaStore.MediaColumns.DISPLAY_NAME,
        MediaStore.MediaColumns.DATE_MODIFIED,
        MediaStore.MediaColumns.DATE_ADDED,
    )
    val selection =
        "${MediaStore.MediaColumns.MIME_TYPE} = ?"
    val selectionArgs = arrayOf(
        "image/gif"
    )
    val sortOrder = "$orderColumnName $order LIMIT $limit OFFSET $offset"
    val gifList = mutableListOf<Gif>()
    contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
        val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
        val displayNameColumnIndex =
            cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
        val dateAddedColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
        val dateModifiedColumnIndex =
            cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
        while (cursor.moveToNext()) {
            val id = cursor.getLong(idColumnIndex)
            val displayName = cursor.getString(displayNameColumnIndex)
            val dateAdded = cursor.getLong(dateAddedColumnIndex)
            val dateModified = cursor.getLong(dateModifiedColumnIndex)
            val gifUri =
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
            gifList += Gif(displayName, dateAdded, dateModified, gifUri)
            Log.i(TAG, "gifUri = $gifUri")
        }
    }
    return gifList
}