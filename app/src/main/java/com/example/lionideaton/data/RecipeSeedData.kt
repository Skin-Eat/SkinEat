package com.example.lionideaton.data

import com.example.lionideaton.data.model.RecipeDetail
import com.example.lionideaton.data.model.SkinBenefit

// Placeholder curated recipe until LLM-based generation from actual basket contents is wired up
// (spec section 11) — see the memory note on remaining gaps.
object RecipeSeedData {
    val featuredRecipe = RecipeDetail(
        name = "연어 브로콜리 지중해식 포케",
        cookingTimeMinutes = 15,
        servings = "1인분",
        ingredients = listOf(
            "연어 120g",
            "브로콜리 50g",
            "파프리카 30g",
            "아몬드 10g",
            "올리브오일 1큰술",
            "레몬즙 약간"
        ),
        steps = listOf(
            "연어를 한입 크기로 썰어 올리브오일과 레몬즙에 재워둡니다.",
            "브로콜리와 파프리카를 살짝 데치거나 잘게 썰어 준비합니다.",
            "그릇에 밥이나 그린 샐러드를 담고 연어와 채소를 올립니다.",
            "아몬드를 잘게 부숴 토핑하고 올리브오일을 살짝 둘러 마무리합니다."
        ),
        skinBenefits = listOf(
            SkinBenefit(
                nutrient = "오메가3 (연어)",
                description = "피부 장벽의 지질막을 채워 수분 손실을 줄이는 데 도움을 줄 수 있어요. 건조함으로 인한 당김이 덜해질 수 있어요."
            ),
            SkinBenefit(
                nutrient = "비타민C (브로콜리·파프리카)",
                description = "콜라겐 합성을 돕고 항산화 작용을 해서, 칙칙함이 줄고 피부 톤이 환해지는 데 도움을 줄 수 있어요."
            ),
            SkinBenefit(
                nutrient = "비타민E (아몬드)",
                description = "항산화 성분이 피부 손상 회복을 돕고, 유수분 밸런스를 맞춰 윤기 있는 피부 표현에 도움을 줄 수 있어요."
            ),
            SkinBenefit(
                nutrient = "저나트륨 구성",
                description = "나트륨이 적은 재료 위주라, 짠 음식 대비 붓기가 덜 생기는 식단이에요."
            )
        )
    )
}
