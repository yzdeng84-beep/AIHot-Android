# AI HOT - Android 客户端

[![Build and Release APK](https://github.com/yzdeng84-beep/AIHot-Android/actions/workflows/build.yml/badge.svg)](https://github.com/yzdeng84-beep/AIHot-Android/actions/workflows/build.yml)

**AI HOT** 是 [aihot.virxact.com](https://aihot.virxact.com/) 的非官方安卓客户端应用，将网站封装为原生 Android App，提供更流畅的移动端体验。

AI HOT 是由 [数字生命卡兹克](https://github.com/KKKKhazix) 推出的 AI 热点监控平台，提供 AI 精选资讯、AI 日报、低粉爆文等功能。

## ✨ 功能特性

- 📱 原生安卓体验，启动屏 + 流畅加载
- 🔄 下拉刷新获取最新 AI 资讯
- ⬅️ 返回键智能导航网页历史
- 📡 断网自动检测 + 一键重试
- 🌙 深色主题，与网站风格一致
- 🔗 支持从浏览器深度链接打开
- 📦 APK 体积小巧，无需额外权限

## 📥 下载安装

前往 [Releases](https://github.com/yzdeng84-beep/AIHot-Android/releases) 页面下载最新版本 APK。

> **安装提示**：首次安装时，系统会提示"允许安装未知来源的应用"，请选择"允许"即可。

## 🛠️ 构建指南

### 环境要求

- Android Studio Hedgehog (2023.1) 或更高版本
- JDK 17
- Gradle 8.11.1

### 构建步骤

```bash
# 克隆仓库
git clone https://github.com/yzdeng84-beep/AIHot-Android.git
cd AIHot-Android

# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease
```

APK 文件位于 `app/build/outputs/apk/` 目录。

### GitHub Actions

推送代码到 `main` 分支会自动构建 Debug APK。推送 `v*` 标签（如 `v1.0.0`）会触发 Release 构建并自动发布。

## 📋 技术栈

- **Kotlin** - 开发语言
- **Android WebView** - 网页容器
- **Material 3** - UI 组件
- **AndroidX SplashScreen** - 启动屏
- **SwipeRefreshLayout** - 下拉刷新
- **GitHub Actions** - CI/CD 自动构建

## 📄 许可

本项目仅供学习交流使用。AI HOT 网站内容版权归 [数字生命卡兹克/虚实传媒](https://aihot.virxact.com/) 所有。

## 🙏 致谢

- [数字生命卡兹克](https://github.com/KKKKhazix) - AI HOT 原作者
- [khazix-skills](https://github.com/KKKKhazix/khazix-skills) - AI HOT Agent Skill
