package com.example.meetingmemo.domain.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SummaryValidatorTest {
    @Test
    fun `returns false for short text`() {
        assertFalse(SummaryValidator.hasEnoughSourceText("짧은 메모"))
    }

    @Test
    fun `returns true when text meets minimum length`() {
        assertTrue(SummaryValidator.hasEnoughSourceText("이 메모는 요약을 생성하기에 충분한 길이를 가지고 있습니다."))
    }

    @Test
    fun `ignores surrounding whitespace`() {
        assertTrue(SummaryValidator.hasEnoughSourceText("   공백을 제외하면 충분한 길이의 회의 메모 텍스트입니다.   "))
    }
}
