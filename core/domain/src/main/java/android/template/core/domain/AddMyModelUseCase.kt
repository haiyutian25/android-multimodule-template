package android.template.core.domain

import android.template.core.data.MyModelRepository
import javax.inject.Inject

/**
 * Saves a new model.
 */
class AddMyModelUseCase @Inject constructor(
    private val myModelRepository: MyModelRepository,
) {
    suspend operator fun invoke(name: String) = myModelRepository.add(name)
}
