package habitiq.app.discover

/**
 * Discovery is a state-driven product system, not a single screen.
 * Compatibility signals and trust tags are separate — never summed into a score.
 */

enum class DiscoveryIntent {
    FIND_FLAT,
    FIND_FLATMATE
}

enum class DiscoveryPostType {
    VACANCY,
    LOOKING
}

enum class DiscoveryPostStatus {
    DRAFT,
    PUBLISHED,
    PAUSED,
    MATCHED,
    EXPIRED,
    CLOSED,
    REMOVED
}

enum class ConnectionStatus {
    NONE,
    REQUEST_SENT,
    ACCEPTED,
    DECLINED,
    BLOCKED,
    EXPIRED,
    CONVERSATION_OPEN,
    MATCHED
}

/** Qualitative only. Never a number. Thresholds for Plus are not defined — do not invent them. */
enum class TrustTier {
    UNRATED,
    NEW_TO_HABITIQ,
    HABITIQ_MEMBER
}

data class TrustPresentation(
    val tier: TrustTier,
    val label: String,
    val explanation: String
)

data class CompatibilitySignal(
    val id: String,
    val label: String
)

data class DiscoveryConnection(
    val id: String,
    val fromUid: String,
    val toUid: String,
    val listingFlatId: String? = null,
    val seekerId: String? = null,
    val message: String = "",
    val status: ConnectionStatus = ConnectionStatus.REQUEST_SENT,
    val createdAt: String = "",
    val updatedAt: String = ""
) {
    fun partnerId(myUid: String): String = if (fromUid == myUid) toUid else fromUid

    fun effectiveStatus(): ConnectionStatus = when (status) {
        ConnectionStatus.ACCEPTED, ConnectionStatus.CONVERSATION_OPEN, ConnectionStatus.MATCHED -> status
        else -> status
    }
}

data class DiscoveryReport(
    val id: String,
    val reporterUid: String,
    val targetUid: String? = null,
    val listingFlatId: String? = null,
    val reason: String,
    val createdAt: String
)

object TrustCopy {
    fun forVacancy(memberCount: Int, consentAllowed: Boolean): TrustPresentation {
        if (!consentAllowed) {
            return TrustPresentation(
                TrustTier.UNRATED,
                "Unrated",
                "This person has not allowed Oddroof to use activity history for a trust tag."
            )
        }
        return if (memberCount > 0) {
            TrustPresentation(
                TrustTier.HABITIQ_MEMBER,
                "Oddroof member",
                "This listing comes from a flat already using Oddroof for shared living. It is not a numerical trust score."
            )
        } else {
            TrustPresentation(
                TrustTier.NEW_TO_HABITIQ,
                "New to Oddroof",
                "New to Oddroof is a neutral tag. It does not mean low trust."
            )
        }
    }

    fun forSeeker(consentAllowed: Boolean): TrustPresentation {
        if (!consentAllowed) {
            return TrustPresentation(
                TrustTier.UNRATED,
                "Unrated",
                "Activity history is not used for a trust tag when consent is off."
            )
        }
        return TrustPresentation(
            TrustTier.NEW_TO_HABITIQ,
            "New to Oddroof",
            "New to Oddroof is a neutral tag. Oddroof does not show a trust percentage, and a single report does not change this tag."
        )
    }
}

object PostStatusLogic {
    /** Keep `active` for web compatibility; status is additive when present. */
    fun fromVacancy(active: Boolean, statusRaw: String?): DiscoveryPostStatus {
        val parsed = statusRaw?.uppercase()?.let { raw ->
            DiscoveryPostStatus.entries.find { it.name == raw }
        }
        if (parsed != null) return parsed
        return if (active) DiscoveryPostStatus.PUBLISHED else DiscoveryPostStatus.CLOSED
    }

    fun fromSeeker(active: Boolean): DiscoveryPostStatus {
        return if (active) DiscoveryPostStatus.PUBLISHED else DiscoveryPostStatus.PAUSED
    }
}
