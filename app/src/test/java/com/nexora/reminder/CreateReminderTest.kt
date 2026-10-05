package com.nexora.reminder.domain.usecase

import com.nexora.reminder.domain.model.ReminderMode
import com.nexora.reminder.domain.repository.ReminderRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class CreateReminderTest {
    private val repo: ReminderRepository = mockk()
    private val useCase = CreateReminder(repo)

    @Test
    fun `invoke trims and creates`() = runTest {
        coEvery { repo.create("Workout", "", 30L, "", "", ReminderMode.ONCE) } returns 1L
        val id = useCase("  Workout  ")
        assertEquals(1L, id)
        coVerify { repo.create("Workout", "", 30L, "", "", ReminderMode.ONCE) }
    }

    @Test
    fun `invoke accepts custom interval`() = runTest {
        coEvery { repo.create("Gym", "", 120L, "", "", ReminderMode.ONCE) } returns 2L
        val id = useCase("Gym", "", 120L)
        assertEquals(2L, id)
    }

    @Test
    fun `invoke accepts voice`() = runTest {
        coEvery { repo.create("Water", "", 30L, "Hey drink water", "/tmp/a.wav", ReminderMode.ONCE) } returns 3L
        val id = useCase("Water", "", 30L, "Hey drink water", "/tmp/a.wav")
        assertEquals(3L, id)
    }

    @Test
    fun `invoke accepts repeat mode`() = runTest {
        coEvery { repo.create("Water", "", 30L, "", "", ReminderMode.REPEAT) } returns 4L
        val id = useCase("Water", "", 30L, "", "", ReminderMode.REPEAT)
        assertEquals(4L, id)
    }

    @Test
    fun `invoke accepts daily time`() = runTest {
        coEvery {
            repo.create("Meds", "", 30L, "", "", ReminderMode.DAILY_TIME, 480)
        } returns 5L
        val id = useCase("Meds", "", 30L, "", "", ReminderMode.DAILY_TIME, 480)
        assertEquals(5L, id)
    }

    @Test
    fun `invoke rejects blank name`() = runTest {
        try {
            useCase("   ")
            fail("should throw")
        } catch (e: IllegalArgumentException) {
            // expected
        }
    }

    @Test
    fun `invoke rejects long name`() = runTest {
        try {
            useCase("a".repeat(51))
            fail("should throw")
        } catch (e: IllegalArgumentException) {
        }
    }

    @Test
    fun `invoke rejects bad interval`() = runTest {
        try {
            useCase("Workout", "", 10L)
            fail("should throw")
        } catch (e: IllegalArgumentException) {
        }
        try {
            useCase("Workout", "", 400L)
            fail("should throw")
        } catch (e: IllegalArgumentException) {
        }
    }
}
