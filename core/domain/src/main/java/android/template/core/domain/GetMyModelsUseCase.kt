package android.template.core.domain

import android.template.core.data.MyModelRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Returns the saved models.
 *
 * A use case combines one or more repositories into a single, focused operation. This template
 * has a single repository, but in a real app a use case would typically combine several of them.
 */
class GetMyModelsUseCase @Inject constructor(
    private val myModelRepository: MyModelRepository,
) {
    operator fun invoke(): Flow<List<String>> = myModelRepository.myModels
}
