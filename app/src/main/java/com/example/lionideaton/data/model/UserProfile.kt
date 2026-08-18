package com.example.lionideaton.data.model

// Mirrors `users.skin_type` enum.
enum class SkinType(val label: String) {
    DRY("건성"),
    NORMAL("중성"),
    OILY("지성"),
    COMBINATION("복합성"),
    COMBINATION_OILY("수부지")
}

// Mirrors `users.concerns` JSON array values.
enum class SkinConcern(val label: String) {
    ATOPY("아토피"),
    ACNE("여드름"),
    SENSITIVE("민감성"),
    PIGMENTATION("미백/잡티"),
    SEBUM("피지/블랙헤드"),
    DARK_CIRCLE("다크서클"),
    INNER_DRYNESS("속건조"),
    WRINKLE("주름/탄력"),
    PORES("모공"),
    REDNESS("홍조"),
    KERATIN("각질"),
    NONE("해당없음")
}

// Mirrors `user_constraint.type` — allergy is a hard exclude, dislike suggests alternates instead.
enum class ConstraintType { ALLERGY, DISLIKE }

data class UserConstraintItem(val type: ConstraintType, val ingredientName: String)

data class UserProfile(
    val email: String = "",
    val nickname: String = "",
    val skinType: SkinType? = null,
    val concerns: List<SkinConcern> = emptyList(),
    val constraints: List<UserConstraintItem> = emptyList()
)
