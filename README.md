# Asteroid

Asteroid is a simple Android game about avoiding incoming asteroids. It is made entirely using the Android Canvas API. All in-game graphics are either stored as vector drawables or drawn during runtime.

[![Build Status](https://travis-ci.com/fennifith/Asteroid.svg?branch=master)](https://travis-ci.com/fennifith/Asteroid)
[![Discord](https://img.shields.io/discord/514625116706177035.svg?logo=discord&colorB=7289da)](https://discord.gg/hTAZHJt)

|Main Screen|Gameplay|
|--------|--------|
|![img](./.github/main.png?raw=true)|![img](./.github/gameplay.png?raw=true)|

## Install

The app is published on Google Play:

[<img src="https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png"
    alt="Get it on Google Play"
    height="80">](https://play.google.com/store/apps/details?id=james.asteroid)

Alternatively, you can download the latest APK from [the GitHub releases](../../releases/).

## How to Play

Your "ship" is drawn at the bottom of the screen. You can press and hold the left and right sides of the screen to move left and right, respectively. Avoid the incoming asteroids. Double-tap to fire the current weapon. This launches a projectile that can destroy asteroids. Destroying asteroids gets you points. After destroying a certain amount of asteroids, you get a reward - more ammunition or a new weapon.

As you destroy more asteroids, the game speeds up and it becomes harder to evade them. Also, you have an ammunition counter at the bottom of the screen that decreases as you fire more projectiles. If you are hit by an asteroid or you run out of ammunition, you lose the game. The objective is to hit as many asteroids as you can, and get as many points as possible, without losing.

## License

```
Copyright 2018 James Fenn

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
