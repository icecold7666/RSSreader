# 下次开发任务清单

**优先级**: 从高到低

## 🚨 立即开始（主体完成后收尾）

### A. 架构一致性收尾
- [x] 将 ServiceLocator 依赖注入切换回 Hilt（保持现有接口不变）
- [x] 校验 ViewModel/Worker 注入链路在 Debug 构建可用（Worker 通过 Hilt EntryPoint 注入）

### B. 性能基线实测
- [ ] 首次打开 < 2s（冷启动采样）
- [ ] 列表滑动稳定流畅（无明显掉帧）
- [ ] 解析 100 条 < 3s
- [ ] 峰值内存 < 200MB
### 8.1 实现WebView集成
- [x] 集成WebView进行HTML渲染
- [x] 实现文章详情页面的HTML显示
- [x] 添加加载进度条
- [x] 实现HTML内容优化
- [x] 添加JavaScript支持

### 8.2 完善文章详情功能
- [x] 添加图片懒加载
- [x] 实现夜间模式切换
- [x] 添加字体大小调整
- [x] 实现文本分享
- [x] 添加返回顶部功能

### 8.3 性能优化
- [x] 优化列表滚动性能
- [x] 实现图片缓存策略
- [x] 添加加载骨架屏
- [x] 优化内存使用

### 8.4 用户体验优化
- [x] 添加动画效果
- [x] 实现下拉刷新
- [x] 添加错误重试机制
- [x] 优化加载状态提示

## 📝 已完成任务

### 5. 文章展示与阅读功能 (100%)
- [x] 创建ArticleViewModel
- [x] 实现文章列表界面
- [x] 实现文章详情页面
- [x] 创建ArticleItem组件
- [x] 更新导航配置
- [x] 创建收藏界面

### 6. 搜索功能 (100%)
- [x] 创建SearchViewModel
- [x] 实现搜索界面
- [x] 实现搜索过滤逻辑
- [x] 添加搜索历史记录
- [x] 创建收藏列表界面

### 7. 导航系统 (100%)
- [x] 创建NavigationGraph
- [x] 更新MainActivity集成导航
- [x] 创建路由管理

### 11. 扩展功能 (已完成)
- [x] 实现文章分享功能
- [x] 添加导入/导出RSS源功能
- [x] 实现数据备份功能
- [x] 阅读偏好设置持久化（夜间/字体）
- [x] 缓存管理（清理缓存）
- [x] 离线阅读（下载+离线切换）

## 📁 新文件已创建

```
app/src/main/java/com/example/rss/presentation/viewmodel/ArticleViewModel.kt
app/src/main/java/com/example/rss/presentation/ui/components/ArticleItem.kt
app/src/main/java/com/example/rss/presentation/ui/screens/ArticleListScreen.kt
app/src/main/java/com/example/rss/presentation/ui/screens/ArticleDetailScreen.kt
app/src/main/java/com/example/rss/presentation/ui/screens/RssSourceScreen.kt
app/src/main/java/com/example/rss/presentation/ui/screens/SearchScreen.kt
app/src/main/java/com/example/rss/presentation/ui/screens/FavoritesScreen.kt
app/src/main/java/com/example/rss/presentation/navigation/NavigationGraph.kt
app/src/main/java/com/example/rss/MainActivity.kt
```

## 🔧 已修复的技术问题

1. **依赖注入问题**: 修复了DatabaseFactory的Context参数传递问题
2. **Repository实现**: 修复了ArticleRepositoryImpl中的源标题获取逻辑
3. **数据模型转换**: 优化了ArticleEntity到Article的转换逻辑
4. **Gradle构建**: 修复了插件版本冲突问题

## 💡 开发总结

已完成的核心功能：
1. **文章展示系统**: 完整的文章列表和详情页面
2. **搜索系统**: 支持实时搜索和搜索历史
3. **收藏功能**: 文章收藏和收藏列表
4. **导航系统**: 完整的应用导航架构
5. **UI组件**: 可复用的文章项和搜索组件

项目现在具备了完整的RSS阅读器基本功能，用户可以：
- 浏览RSS源中的文章
- 搜索文章内容
- 收藏喜欢的文章
- 查看文章详情
- 管理RSS源列表

---

**开始位置**: 从"A. 架构一致性收尾"开始
