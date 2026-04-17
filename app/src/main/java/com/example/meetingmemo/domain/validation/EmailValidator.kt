package com.example.meetingmemo.domain.validation

private val EmailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

object EmailValidator {
    fun isValid(email: String): Boolean = EmailRegex.matches(email.trim())
}
