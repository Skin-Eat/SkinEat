package com.example.lionideaton.data

import com.example.lionideaton.data.model.Food
import com.example.lionideaton.data.model.FoodSource

// Placeholder nutrition values (estimated, not sourced from the real MFDS DB the backend
// will eventually seed). Good enough for demo purposes; swap for real data once the
// backend's `food` table is populated.
object FoodSeedData {
    val foods: List<Food> = listOf(
        Food(1, "김치찌개", FoodSource.MFDS, 350, 250.0, 15.0, 4.0, 18.0, 12.0, satFatG = 4.0, vitCMg = 10.0, zincMg = 1.5),
        Food(2, "라면", FoodSource.MFDS, 500, 500.0, 78.0, 5.0, 10.0, 16.0, satFatG = 8.0, zincMg = 0.8, isHighGi = true),
        Food(3, "마라탕", FoodSource.MFDS, 600, 650.0, 40.0, 6.0, 25.0, 40.0, satFatG = 15.0, vitCMg = 8.0, zincMg = 2.0),
        Food(4, "치킨", FoodSource.MFDS, 150, 280.0, 10.0, 1.0, 20.0, 18.0, satFatG = 5.0, zincMg = 1.2),
        Food(5, "피자", FoodSource.MFDS, 150, 285.0, 33.0, 4.0, 12.0, 11.0, satFatG = 5.0, vitCMg = 2.0, zincMg = 1.5, isDairy = true, isHighGi = true),
        Food(6, "햄버거", FoodSource.MFDS, 250, 550.0, 45.0, 9.0, 25.0, 30.0, satFatG = 11.0, vitCMg = 3.0, zincMg = 4.0, isDairy = true, isHighGi = true),
        Food(7, "떡볶이", FoodSource.MFDS, 300, 480.0, 90.0, 25.0, 8.0, 8.0, satFatG = 2.0, vitCMg = 5.0, zincMg = 1.0, isHighGi = true),
        Food(8, "샐러드", FoodSource.MFDS, 250, 150.0, 12.0, 5.0, 5.0, 9.0, satFatG = 1.5, omega3Mg = 200.0, vitAUg = 300.0, vitCMg = 25.0, vitEMg = 2.0, zincMg = 0.8),
        Food(9, "연어", FoodSource.MFDS, 100, 210.0, 0.0, 0.0, 22.0, 13.0, satFatG = 2.5, omega3Mg = 2200.0, vitAUg = 50.0, vitEMg = 1.1, zincMg = 0.5),
        Food(10, "고등어", FoodSource.MFDS, 100, 205.0, 0.0, 0.0, 20.0, 14.0, satFatG = 3.5, omega3Mg = 2600.0, vitAUg = 40.0, vitEMg = 1.5, zincMg = 0.7),
        Food(11, "브로콜리", FoodSource.MFDS, 100, 34.0, 7.0, 1.7, 2.8, 0.4, satFatG = 0.1, vitAUg = 31.0, vitCMg = 89.0, vitEMg = 0.8, zincMg = 0.4),
        Food(12, "아몬드", FoodSource.MFDS, 28, 164.0, 6.0, 1.2, 6.0, 14.0, satFatG = 1.1, vitEMg = 7.3, zincMg = 0.9),
        Food(13, "키위", FoodSource.MFDS, 76, 42.0, 10.0, 6.0, 0.8, 0.4, vitAUg = 4.0, vitCMg = 71.0, vitEMg = 1.0, zincMg = 0.1),
        Food(14, "토마토", FoodSource.MFDS, 123, 22.0, 4.8, 3.2, 1.1, 0.2, vitAUg = 42.0, vitCMg = 17.0, vitEMg = 0.5, zincMg = 0.2),
        Food(15, "아보카도 연어 샐러드", FoodSource.MFDS, 300, 320.0, 12.0, 3.0, 20.0, 22.0, satFatG = 3.0, omega3Mg = 1200.0, vitAUg = 60.0, vitCMg = 20.0, vitEMg = 3.0, zincMg = 0.9),
        Food(16, "그릭 요거트와 블루베리", FoodSource.MFDS, 200, 180.0, 20.0, 14.0, 12.0, 5.0, satFatG = 3.0, vitAUg = 10.0, vitCMg = 8.0, vitEMg = 0.3, zincMg = 0.8, isDairy = true)
    )

    fun findByName(name: String): Food? = foods.firstOrNull { it.name == name }
}
