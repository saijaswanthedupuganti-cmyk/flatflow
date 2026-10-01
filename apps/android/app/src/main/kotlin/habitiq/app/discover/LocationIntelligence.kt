package habitiq.app.discover

/**
 * Reserved location intelligence for Discovery Phase 2.
 *
 * Google Maps, Places, geocoding and routing are NOT in the current APK Discovery flow.
 * [DiscoverFlags.DISTANCE_INTELLIGENCE] stays false until a mapping service is approved.
 *
 * Workplace/college coordinates are commute anchors — never a home address.
 * Flat coordinates, if ever stored for Discovery, must be neighbourhood-precision only.
 */
data class ApproxNeighborhoodLocation(
    val city: String = "",
    val area: String = "",
    val areaPlaceId: String? = null,
    val approxLat: Double? = null,
    val approxLng: Double? = null,
    val precision: String = PRECISION_NEIGHBORHOOD
) {
    fun isNeighborhoodSafe(): Boolean =
        precision == PRECISION_NEIGHBORHOOD &&
            approxLat != null &&
            approxLng != null

    fun toFirestoreMap(): Map<String, Any?> = buildMap {
        put("city", city)
        put("area", area)
        put("precision", PRECISION_NEIGHBORHOOD)
        areaPlaceId?.takeIf { it.isNotBlank() }?.let { put("areaPlaceId", it) }
        approxLat?.let { put("approxLat", it) }
        approxLng?.let { put("approxLng", it) }
    }

    companion object {
        const val PRECISION_NEIGHBORHOOD = "neighborhood"
        const val FIELD_ON_FLAT = "discoveryApproxLocation"

        fun parse(raw: Any?): ApproxNeighborhoodLocation? {
            val map = raw as? Map<*, *> ?: return null
            val precision = map["precision"]?.toString() ?: PRECISION_NEIGHBORHOOD
            if (precision != PRECISION_NEIGHBORHOOD) return null
            return ApproxNeighborhoodLocation(
                city = map["city"]?.toString().orEmpty(),
                area = map["area"]?.toString().orEmpty(),
                areaPlaceId = map["areaPlaceId"]?.toString()?.takeIf { it.isNotBlank() },
                approxLat = (map["approxLat"] as? Number)?.toDouble(),
                approxLng = (map["approxLng"] as? Number)?.toDouble(),
                precision = PRECISION_NEIGHBORHOOD
            )
        }
    }
}

data class CommuteAnchor(
    val label: String = "",
    val placeId: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
) {
    fun toFirestoreMap(): Map<String, Any?> = buildMap {
        put("label", label)
        placeId?.takeIf { it.isNotBlank() }?.let { put("placeId", it) }
        lat?.let { put("lat", it) }
        lng?.let { put("lng", it) }
    }

    companion object {
        fun parse(raw: Any?): CommuteAnchor? {
            val map = raw as? Map<*, *> ?: return null
            val label = map["label"]?.toString().orEmpty()
            val placeId = map["placeId"]?.toString()?.takeIf { it.isNotBlank() }
            val lat = (map["lat"] as? Number)?.toDouble()
            val lng = (map["lng"] as? Number)?.toDouble()
            if (label.isBlank() && placeId == null && lat == null) return null
            return CommuteAnchor(label, placeId, lat, lng)
        }
    }
}

data class CommuteAnchors(
    val workplace: CommuteAnchor? = null,
    val college: CommuteAnchor? = null
) {
    fun toFirestoreMap(): Map<String, Any?> = buildMap {
        workplace?.let { put("workplace", it.toFirestoreMap()) }
        college?.let { put("college", it.toFirestoreMap()) }
    }

    companion object {
        const val FIELD_ON_SEEKER = "commuteAnchors"

        fun parse(raw: Any?): CommuteAnchors? {
            val map = raw as? Map<*, *> ?: return null
            val parsed = CommuteAnchors(
                workplace = CommuteAnchor.parse(map["workplace"]),
                college = CommuteAnchor.parse(map["college"])
            )
            return parsed.takeIf { it.workplace != null || it.college != null }
        }
    }
}

object LocationIntelligence {
    /** Distance / commute UI and ranking stay off until this flag is on AND a mapping service ships. */
    fun isEnabled(): Boolean = DiscoverFlags.DISTANCE_INTELLIGENCE

    /**
     * Never use rooftop/home coordinates from create-flat maps, pincode, or landmark
     * as a Discovery distance origin.
     */
    fun listingLocationForFutureDistance(listingApprox: ApproxNeighborhoodLocation?): ApproxNeighborhoodLocation? {
        if (!listingApprox.isNeighborhoodSafe()) return null
        return listingApprox
    }

    private fun ApproxNeighborhoodLocation?.isNeighborhoodSafe(): Boolean =
        this?.isNeighborhoodSafe() == true
}
