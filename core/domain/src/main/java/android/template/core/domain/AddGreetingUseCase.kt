package android.template.core.domain

import android.template.core.data.GreetingRepository
import javax.inject.Inject

/**
 * Saves a new model.
 */
class AddGreetingUseCase @Inject constructor(
    private val greetingRepository: GreetingRepository,
) {
    suspend operator fun invoke(message: String) = greetingRepository.add(message)
}
