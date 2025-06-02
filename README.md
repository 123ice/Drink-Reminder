# 💧 Drink Reminder - 智能饮水提醒应用

<div align="center">

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/kotlin-%237F52FF.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)

*一款基于现代Android技术栈的智能饮水提醒应用，帮助用户养成健康的饮水习惯*

</div>

## 📱 项目介绍

Drink Reminder 是一款专为Android平台开发的智能饮水提醒应用。应用采用现代化的UI设计和先进的技术架构，为用户提供个性化的饮水提醒服务，并通过数据可视化帮助用户追踪和分析饮水习惯。

该项目从休息提醒应用重构而来，经过完整的架构升级和功能扩展，现已发展为功能完善的饮水健康管理工具。

## ✨ 核心特点

### 🎯 智能提醒系统
- **全屏提醒界面** - 锁屏状态下的醒目提醒显示
- **自定义提醒间隔** - 支持个性化的提醒频率设置
- **智能时间管理** - 可设置工作时间段，避免夜间打扰
- **工作日智能识别** - 支持周一到周日的灵活提醒计划

### 📊 数据统计与可视化
- **实时进度追踪** - 今日饮水量实时更新显示
- **历史数据图表** - 7天/30天饮水趋势可视化
- **完成度分析** - 目标达成率和统计概览
- **详细记录管理** - 每日饮水记录的详细展示

### 🎨 现代化用户体验
- **Material Design 3** - 遵循最新设计规范
- **响应式UI** - 流畅的动画和交互体验
- **底部导航** - 设置和统计功能便捷切换
- **深色模式支持** - 适应不同使用场景

### 🏗️ 企业级架构
- **模块化设计** - Features模块清晰分离
- **MVVM架构** - 数据和UI层完全解耦
- **Repository模式** - 统一的数据访问层
- **Clean Architecture** - 高可维护性和可测试性

## 🛠️ 技术栈

- **Kotlin** - 100% Kotlin开发
- **Jetpack Compose** - 现代化声明式UI框架
- **Room Database** - 本地数据持久化
- **MVVM架构** - 数据和UI层解耦
- **Kotlin Coroutines & Flow** - 异步编程和响应式数据流
- **Material Design 3** - 最新设计语言
- **Foreground Service** - 后台提醒服务
- **Canvas API** - 自定义图表绘制

## 📁 项目结构

```
app/src/main/java/com/example/reminder/
├── 📱 MainActivity.kt              # 应用入口
├── 💧 DrinkActivity.kt            # 全屏提醒界面
├── 🔔 ReminderService.kt          # 后台提醒服务
├── 🏗️ core/                       # 核心模块
│   └── database/                  # 数据库配置
│       └── DrinkDatabase.kt
├── 🎯 features/                   # 功能模块
│   ├── drink/                     # 饮水功能模块
│   │   ├── data/                  # 数据层
│   │   │   ├── database/          # 数据库实体&DAO
│   │   │   └── repository/        # 数据仓库
│   │   ├── domain/                # 业务逻辑层
│   │   │   ├── model/             # 数据模型
│   │   │   └── repository/        # 仓库接口
│   │   └── presentation/          # 表现层
│   │       └── settings/          # 设置界面
│   └── statistics/                # 统计功能模块
│       └── presentation/          # 统计界面
└── 🎨 ui/                         # UI主题
    └── theme/
```

## 🚀 开发路线

### Phase 1: 基础架构 ✅
- [x] 项目重构为饮水提醒应用
- [x] 建立模块化架构（Features）
- [x] 集成Room数据库
- [x] 实现MVVM架构模式

### Phase 2: 核心功能 ✅
- [x] 全屏饮水提醒界面
- [x] 后台服务和通知系统
- [x] 饮水记录数据持久化
- [x] 实时进度追踪

### Phase 3: 数据可视化 ✅
- [x] 统计界面开发
- [x] 自定义图表组件
- [x] 历史数据分析
- [x] 完成度统计

### Phase 4: 用户体验优化 ✅
- [x] Material Design 3适配
- [x] 底部导航实现
- [x] 响应式UI设计
- [x] 错误处理和边界情况

## 📋 TODO List

- [ ] **用户引导** - 首次使用引导流程
- [ ] **数据导出** - 支持导出饮水记录
- [ ] **提醒音效** - 自定义提醒声音
- [ ] **深色模式** - 完整的主题切换支持
- [ ] **健康建议** - 基于数据的个性化建议
- [ ] **社交分享** - 成就分享功能
- [ ] **多语言支持** - 国际化适配
- [ ] **云端同步** - 数据备份与同步
- [ ] **智能手表集成** - Wear OS支持
- [ ] **健康数据集成** - Google Fit整合
- [ ] **AI推荐** - 智能饮水计划推荐
- [ ] **小组件** - 桌面Widget支持

## 🏃‍♂️ 快速开始

### 环境要求
- Android Studio Flamingo 或更高版本
- Kotlin 1.8+
- Android SDK 24+ (Android 7.0)
- JDK 11 或更高版本

### 安装步骤
1. **克隆仓库**
   ```bash
   git clone https://github.com/123ice/Drink-Reminder.git
   cd Drink-Reminder
   ```

2. **打开项目**
   - 使用Android Studio打开项目
   - 等待Gradle同步完成

3. **运行应用**
   - 连接Android设备或启动模拟器
   - 点击运行按钮或执行 `./gradlew installDebug`

### 构建发布版本
```bash
./gradlew assembleRelease
```

## 📞 联系方式

- **开发者**: 123ice
- **GitHub**: [@123ice](https://github.com/123ice)
- **项目地址**: [Drink-Reminder](https://github.com/123ice/Drink-Reminder)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给一个Star！⭐**

*让我们一起养成健康的饮水习惯* 💧

</div> 