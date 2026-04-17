package com.example.meetingmemo.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EmailValidatorTest {
    @Test
    fun `returns true for valid email`() {
        assertTrue(EmailValidator.isValid("user@example.com"))
    }

    @Test
    fun `returns false for invalid email`() {
        assertFalse(EmailValidator.isValid("not-an-email"))
    }

    @Test
    fun `trims whitespace before validation`() {
        assertTrue(EmailValidator.isValid("  user@example.com  "))
    }
}
