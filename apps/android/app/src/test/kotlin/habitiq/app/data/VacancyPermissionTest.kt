package habitiq.app.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VacancyPermissionTest {
    @Test fun `admin manages any vacancy`() = assertTrue(canManageVacancy(isAdmin = true, postedBy = "u2", uid = "u1"))
    @Test fun `poster manages their own vacancy`() = assertTrue(canManageVacancy(isAdmin = false, postedBy = "u2", uid = "u2"))
    @Test fun `other members cannot`() = assertFalse(canManageVacancy(isAdmin = false, postedBy = "u2", uid = "u3"))
    @Test fun `legacy vacancy without poster is admin only`() = assertFalse(canManageVacancy(isAdmin = false, postedBy = null, uid = "u3"))
    @Test fun `signed out cannot`() = assertFalse(canManageVacancy(isAdmin = false, postedBy = "u2", uid = null))
}
