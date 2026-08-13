package habitiq.app.flats

data class FlatInfo(
    val id: String,
    val name: String,
    val adminUid: String,
    val memberCount: Int,
    val joinMode: String = "auto",
    val vacancy: habitiq.app.data.VacancyData? = null
)
