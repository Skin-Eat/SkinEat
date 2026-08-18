package com.example.lionideaton.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lionideaton.data.FoodSeedData
import com.example.lionideaton.data.model.Food
import com.example.lionideaton.data.model.FoodSource
import com.example.lionideaton.data.model.MealItem
import com.example.lionideaton.data.model.MealLogEntry
import com.example.lionideaton.data.model.MealType
import com.example.lionideaton.data.network.FoodCreateRequest
import com.example.lionideaton.data.network.MealItemRequest
import com.example.lionideaton.data.network.MealLogOut
import com.example.lionideaton.data.network.MealRequest
import com.example.lionideaton.data.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Activity-scoped shared meal log (see CartViewModel for the same pattern), read by
// Home/FoodLog/Analysis/Basket so a meal registered once shows up everywhere.
class MealLogViewModel : ViewModel() {

    private var nextId = 1000L

    private val _entries = MutableStateFlow(seedDemoHistory())
    val entries: StateFlow<List<MealLogEntry>> = _entries.asStateFlow()

    fun registerMeal(
        foodName: String,
        servingCount: Int,
        customFood: Food? = null,
        eatenAt: LocalDateTime = LocalDateTime.now()
    ) {
        // customFood는 사용자가 "직접 추가하기"로 영양정보를 입력한 신규 메뉴 — 있으면 그걸
        // 그대로 쓰고, 없으면 기존처럼 로컬 시드에서 찾아보고 그마저 없으면 0으로 채운다.
        val food = customFood ?: (FoodSeedData.findByName(foodName) ?: unknownFood(foodName))
        val entry = MealLogEntry(
            id = nextId++,
            eatenAt = eatenAt,
            mealType = inferMealType(eatenAt.toLocalTime()),
            items = listOf(MealItem(food = food, portionRatio = servingCount.toDouble()))
        )
        _entries.update { it + entry }

        // 로그인 안 했으면(토큰 없음) 백엔드 호출은 건너뛰고 로컬 데모 상태로만 남긴다 —
        // 회원가입/로그인 연결 전이거나 오프라인이어도 이 화면 자체는 그대로 쓸 수 있게.
        if (NetworkModule.authToken == null) return

        viewModelScope.launch {
            try {
                val foodId = resolveBackendFoodId(foodName, food)
                NetworkModule.api.registerMeal(
                    MealRequest(
                        eatenAt = eatenAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                        mealType = entry.mealType.name,
                        items = listOf(MealItemRequest(foodId = foodId, portionRatio = servingCount.toDouble()))
                    )
                )
            } catch (e: Exception) {
                // 백엔드 반영 실패해도 로컬 상태(위 _entries)는 이미 갱신됐으니 화면은 정상 동작.
                // TODO: 재시도/오프라인 큐 없음 — 데모 이후 개선 대상.
            }
        }
    }

    // 안드로이드 로컬 시드(FoodSeedData)의 id는 백엔드 food 테이블의 id와 다른 값이라(서로
    // 독립적으로 만들어진 시드라 순서가 안 맞음), 이름으로 먼저 검색해서 실제 백엔드 id를
    // 찾는다. 없으면(커스텀 입력 메뉴 등) 새로 만들어서 그 id를 쓴다.
    private suspend fun resolveBackendFoodId(name: String, localFood: Food): Int {
        val found = NetworkModule.api.searchFoods(name, limit = 5).data?.firstOrNull { it.name == name }
        if (found != null) return found.id

        val created = NetworkModule.api.createFood(
            FoodCreateRequest(
                name = name,
                servingG = localFood.servingG,
                energyKcal = localFood.energyKcal,
                carbG = localFood.carbG,
                sugarG = localFood.sugarG,
                proteinG = localFood.proteinG,
                fatG = localFood.fatG
            )
        ).data
        return created?.id ?: error("food 생성 실패")
    }

    // 로그인 직후(MainActivity에서 isLoggedIn 전환 시 호출) 백엔드에 실제로 쌓인 기록을
    // 불러온다. 이게 없으면 앱을 껐다 켤 때마다(authToken이 메모리에만 있어 재로그인하면서)
    // _entries가 seedDemoHistory()로 다시 초기화돼 방금 기록한 식사가 사라진 것처럼 보인다.
    fun loadFromBackend() {
        if (NetworkModule.authToken == null) return
        viewModelScope.launch {
            try {
                val backendEntries = NetworkModule.api.getMeals().data.orEmpty().mapNotNull { it.toEntryOrNull() }
                // 실제 기록이 하나도 없는 신규 계정은 데모 시드를 그대로 둬서 화면이 텅 비지
                // 않게 하고, 하나라도 있으면 그게 진짜 상태이니 데모를 완전히 대체한다.
                if (backendEntries.isNotEmpty()) {
                    _entries.value = backendEntries
                }
            } catch (e: Exception) {
                // 조회 실패해도 기존 상태(데모 시드 또는 이전 값)를 유지해 화면은 계속 동작.
            }
        }
    }

    private fun MealLogOut.toEntryOrNull(): MealLogEntry? {
        val eatenAtValue = eatenAt ?: return null
        val parsedTime = runCatching { LocalDateTime.parse(eatenAtValue) }.getOrNull() ?: return null
        val parsedMealType = mealType?.let { runCatching { MealType.valueOf(it) }.getOrNull() }
            ?: inferMealType(parsedTime.toLocalTime())
        val mappedItems = items.map { item ->
            MealItem(
                food = FoodSeedData.findByName(item.food.name) ?: unknownFood(item.food.name),
                portionRatio = item.portionRatio
            )
        }
        return MealLogEntry(id = id.toLong(), eatenAt = parsedTime, mealType = parsedMealType, items = mappedItems)
    }

    fun todayEntries(): List<MealLogEntry> = entriesForDate(LocalDate.now())

    fun entriesForDate(date: LocalDate): List<MealLogEntry> =
        entries.value.filter { it.eatenAt.toLocalDate() == date }

    fun recentEntries(days: Long = 7): List<MealLogEntry> {
        val cutoff = LocalDateTime.now().minusDays(days)
        return entries.value.filter { it.eatenAt.isAfter(cutoff) }
    }

    companion object {
        // A custom/typed food (not in the seed DB) contributes no nutrient data at all —
        // that's intentional: it should be excluded from averages, not counted as zero.
        private fun unknownFood(name: String) = Food(
            id = -1,
            name = name,
            source = FoodSource.USER_ADDED,
            servingG = 100,
            energyKcal = 0.0,
            carbG = 0.0,
            sugarG = 0.0,
            proteinG = 0.0,
            fatG = 0.0
        )

        private fun inferMealType(time: LocalTime): MealType = when {
            time.isBefore(LocalTime.of(11, 0)) -> MealType.BREAKFAST
            time.isBefore(LocalTime.of(15, 0)) -> MealType.LUNCH
            time.isBefore(LocalTime.of(18, 0)) -> MealType.SNACK
            else -> MealType.DINNER
        }

        // Demo history so Analysis/Basket/Report have enough data to show meaningful patterns
        // before the user logs anything themselves in a live demo. "Today" starts empty and
        // fills in as they register meals. Covers a full ~30 days (not just 7) so the Report
        // screen's "최근 7일" vs "최근 30일" views actually differ — the older 3 weeks lean
        // toward processed/high-GI food, easing into the more balanced most-recent week, so the
        // 30-day average, deficiencies, and patterns read differently from the 7-day ones.
        //
        // 이 시드는 로컬 데모 전용이라 백엔드로는 안 보낸다(registerMeal()을 안 거치고 바로
        // _entries에 넣기 때문) — 실제 DB 분석 결과를 이 합성 히스토리로 오염시키지 않기 위함.
        private fun seedDemoHistory(): List<MealLogEntry> {
            val today = LocalDate.now()
            var id = 1L

            fun entryFor(daysAgo: Long, hour: Int, minute: Int, foodName: String): MealLogEntry {
                val dateTime = today.minusDays(daysAgo).atTime(hour, minute)
                val food = FoodSeedData.findByName(foodName)
                return MealLogEntry(
                    id = id++,
                    eatenAt = dateTime,
                    mealType = inferMealType(dateTime.toLocalTime()),
                    items = listOfNotNull(food).map { MealItem(it) }
                )
            }

            val recentWeek = listOf(
                entryFor(6, 12, 30, "라면"),
                entryFor(6, 19, 0, "치킨"),
                entryFor(5, 12, 40, "마라탕"),
                entryFor(5, 16, 0, "그릭 요거트와 블루베리"),
                entryFor(4, 12, 30, "햄버거"),
                entryFor(4, 19, 30, "피자"),
                entryFor(3, 12, 30, "샐러드"),
                entryFor(3, 19, 0, "연어"),
                entryFor(2, 12, 30, "떡볶이"),
                entryFor(2, 19, 0, "토마토"),
                entryFor(1, 12, 30, "김치찌개"),
                entryFor(1, 19, 0, "고등어")
            )

            val unhealthyPool = listOf("라면", "치킨", "피자", "떡볶이", "햄버거", "마라탕", "김치찌개")
            val balancedPool = listOf("샐러드", "연어", "토마토", "고등어", "그릭 요거트와 블루베리", "아보카도 연어 샐러드", "브로콜리", "키위", "아몬드")

            val olderHistory = (7..29L).flatMap { daysAgo ->
                val leaningUnhealthy = daysAgo >= 14 // weeks 3-4 back lean unhealthy; week 2 back trends healthier
                val lunchPool = if (leaningUnhealthy) unhealthyPool else balancedPool
                val dinnerPool = if (leaningUnhealthy) unhealthyPool else balancedPool
                val lunch = entryFor(daysAgo, 12, 30, lunchPool[(daysAgo % lunchPool.size).toInt()])
                val dinner = if (daysAgo % 2 == 0L) entryFor(daysAgo, 19, 0, dinnerPool[((daysAgo + 3) % dinnerPool.size).toInt()]) else null
                listOfNotNull(lunch, dinner)
            }

            return recentWeek + olderHistory
        }
    }
}
