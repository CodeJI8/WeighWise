# Weighwise: Decision Maker

A decision-making engine built with Kotlin and Jetpack Compose (Material 3).

## Features
- **Zero Permissions**: No internet, storage, contacts, or analytics. Completely offline and private.
- **Advanced Math Engine**: Weighted scoring, AHP pairwise comparison, deal-breaker elimination.
- **Robustness Analysis**: Monte Carlo simulations and sensitivity analysis to test if a decision is a "Clear Win" or a "Toss-up".
- **Visuals**: Animated balance scale, fluid layout, Material 3 theming with a custom design system.

## Design System
- **Colors**: Calm palette (Light #FAF7F2, Dark #0A1226). Teal primary (#12B5A6), Amber highlights (#FFB020).
- **Typography**: Uses Sora for headings, Inter for body (Requires manual TTF bundling for actual fonts, falls back to SansSerif).
- **Motion**: Spring animations, staggered lists.

## Building
1. Open the project in Android Studio.
2. If you want true Sora and Inter fonts, download their `.ttf` files from Google Fonts and place them in `app/src/main/res/font/sora.ttf` and `inter.ttf`.
3. Build and Run.

## Architecture
- **MVVM** with Hilt for Dependency Injection.
- **Room** for local database.
- **Navigation Compose** for single-activity flow.
