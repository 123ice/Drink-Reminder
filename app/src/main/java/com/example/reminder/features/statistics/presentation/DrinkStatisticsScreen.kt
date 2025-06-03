package com.example.reminder.features.statistics.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.reminder.features.drink.data.repository.DrinkRecordRepository
import com.example.reminder.features.drink.data.database.DayStats
import com.example.reminder.features.drink.domain.repository.DrinkSettingsRepository
import kotlinx.coroutines.launch

@Composable
fun DrinkStatisticsScreen(
    viewModel: DrinkStatisticsViewModel = viewModel()
) {
    val context = LocalContext.current
    val repository = remember { DrinkRecordRepository(context) }
    val settings = DrinkSettingsRepository.loadSettings(context)
    
    var selectedPeriod by remember { mutableStateOf("7天") }
    var statsData by remember { mutableStateOf<List<DayStats>>(emptyList()) }
    var todayTotal by remember { mutableStateOf(0) }
    
    // 加载数据
    LaunchedEffect(selectedPeriod) {
        statsData = when (selectedPeriod) {
            "7天" -> repository.getLast7DaysStats()
            "30天" -> repository.getLast30DaysStats()
            else -> repository.getLast7DaysStats()
        }
        todayTotal = repository.getTodayTotalAmount()
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // 标题
            Text(
                "📊 饮水统计",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            // 今日概况卡片
            TodayOverviewCard(
                todayTotal = todayTotal,
                dailyGoal = settings.dailyGoal
            )
        }
        
        item {
            // 时间段选择
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("7天", "30天").forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { selectedPeriod = period },
                        label = { Text(period) }
                    )
                }
            }
        }
        
        item {
            // 图表卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "近${selectedPeriod}饮水量趋势",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    if (statsData.isNotEmpty()) {
                        DrinkChart(
                            data = statsData,
                            dailyGoal = settings.dailyGoal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("暂无数据")
                        }
                    }
                }
            }
        }
        
        item {
            // 统计详情
            StatisticsDetailsCard(
                data = statsData,
                dailyGoal = settings.dailyGoal
            )
        }
        
        if (statsData.isNotEmpty()) {
            item {
                Text(
                    "每日详情",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            items(statsData) { dayStats ->
                DayStatsItem(
                    dayStats = dayStats,
                    dailyGoal = settings.dailyGoal
                )
            }
        }
    }
}

@Composable
fun TodayOverviewCard(
    todayTotal: Int,
    dailyGoal: Int
) {
    val progress = (todayTotal.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "今日饮水",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "${todayTotal}ml",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Text(
                "目标: ${dailyGoal}ml",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            
            Spacer(Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = if (progress >= 1f) Color.Green else MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                if (progress >= 1f) "🎉 目标已完成！" else "完成度: ${(progress * 100).toInt()}%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun StatisticsDetailsCard(
    data: List<DayStats>,
    dailyGoal: Int
) {
    if (data.isEmpty()) return
    
    val totalAmount = data.sumOf { it.totalAmount }
    val averageAmount = if (data.isNotEmpty()) totalAmount / data.size else 0
    val completedDays = data.count { it.totalAmount >= dailyGoal }
    val completionRate = if (data.isNotEmpty()) (completedDays.toFloat() / data.size * 100).toInt() else 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "统计概览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("总饮水量", "${totalAmount}ml")
                StatItem("日均饮水", "${averageAmount}ml")
            }
            
            Spacer(Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatItem("完成天数", "${completedDays}天")
                StatItem("完成率", "${completionRate}%")
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DayStatsItem(
    dayStats: DayStats,
    dailyGoal: Int
) {
    val progress = (dayStats.totalAmount.toFloat() / dailyGoal.toFloat()).coerceIn(0f, 1f)
    val isCompleted = dayStats.totalAmount >= dailyGoal
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    dayStats.date,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${dayStats.totalAmount}ml",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall
                )
                if (isCompleted) {
                    Text("✅", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun DrinkChart(
    data: List<DayStats>,
    dailyGoal: Int,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    Canvas(modifier = modifier) {
        if (data.isEmpty()) return@Canvas
        
        val canvasWidth = size.width
        val canvasHeight = size.height
        val padding = 40f
        
        val chartWidth = canvasWidth - 2 * padding
        val chartHeight = canvasHeight - 2 * padding
        
        val maxValue = maxOf(data.maxOfOrNull { it.totalAmount } ?: 0, dailyGoal)
        val stepX = chartWidth / (data.size - 1).coerceAtLeast(1)
        
        // 绘制目标线
        val goalY = padding + chartHeight - (dailyGoal.toFloat() / maxValue * chartHeight)
        drawLine(
            color = Color.Red.copy(alpha = 0.5f),
            start = Offset(padding, goalY),
            end = Offset(canvasWidth - padding, goalY),
            strokeWidth = 2f
        )
        
        // 绘制数据点和连线
        val path = Path()
        data.forEachIndexed { index, dayStats ->
            val x = padding + index * stepX
            val y = padding + chartHeight - (dayStats.totalAmount.toFloat() / maxValue * chartHeight)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            
            // 绘制数据点
            drawCircle(
                color = primaryColor,
                radius = 6f,
                center = Offset(x, y)
            )
        }
        
        // 绘制连线
        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 3f)
        )
    }
}

class DrinkStatisticsViewModel : ViewModel() {
    // ViewModel可以后续扩展更多功能
} 