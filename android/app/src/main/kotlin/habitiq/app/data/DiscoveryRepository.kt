package habitiq.app.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class DiscoveryRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    /** Lists flats with an active vacancy listing (embedded on flat doc). */
    fun observeActiveVacancies(): Flow<List<VacancyListing>> = callbackFlow {
        val registration = firestore.collection("flats")
            .whereEqualTo("vacancy.active", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val listings = snapshot?.documents.orEmpty().mapNotNull { doc ->
                    val vacancy = doc.get("vacancy") as? Map<*, *> ?: return@mapNotNull null
                    val active = vacancy["active"] as? Boolean ?: false
                    if (!active) return@mapNotNull null
                    VacancyListing(
                        flatId = doc.id,
                        flatName = doc.getString("name") ?: doc.id,
                        active = true,
                        city = vacancy["city"]?.toString().orEmpty(),
                        area = vacancy["area"]?.toString().orEmpty(),
                        rentPerHead = (vacancy["rentPerHead"] as? Number)?.toDouble(),
                        currency = vacancy["currency"]?.toString() ?: "INR",
                        bedsAvailable = (vacancy["bedsAvailable"] as? Number)?.toInt() ?: 1,
                        preferredGender = vacancy["preferredGender"]?.toString() ?: "any",
                        about = vacancy["about"]?.toString().orEmpty()
                    )
                }
                trySend(listings)
            }
        awaitClose { registration.remove() }
    }
}
