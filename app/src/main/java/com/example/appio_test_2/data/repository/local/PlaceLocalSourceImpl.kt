package com.example.appio_test_2.data.repository.local

import com.example.appio_test_2.domain.PlaceLocalSource
import com.example.appio_test_2.utils.DispatchersProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PlaceLocalSourceImpl(
    private val dao: PlaceDao,
    private val dispatchersProvider: DispatchersProvider
) : PlaceLocalSource {
    override suspend fun getAllPlace(): Flow<List<PlaceEntity>> =
        withContext(dispatchersProvider.io) {
            dao.getAll()
        }

    override suspend fun deletePlace(id: Long) = dao.deleteById(id)

    override suspend fun insertPlace(place: PlaceEntity) = withContext(dispatchersProvider.io) {
        dao.insert(place)
    }

    override suspend fun updatePlace(place: PlaceEntity) = withContext(dispatchersProvider.io) {
        dao.update(place)
    }
}
