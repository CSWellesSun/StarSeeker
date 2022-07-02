# StarSeeker

## Logo

<img src="app\src\main\res\mipmap-xxhdpi\logo.png" alt="logo" style="zoom: 5%;" />

## Abstract

For astrophiles, it is rather difficult to quickly identify the name or get information of the stars in sight.

To determine the name of the target star, they usually use starmap apps, such as star walk2 and Stellarium, to simulate their viewpoint. However, the apps interfere with the experience of stargazing, because they distract astrophiles' attention and harm their eyesight at night. 

Therefore, we propose a new way to observe and learn about stars, that is, by wearing smart glasses to calculate viewpoint and star position and receive verbal cues in real time. Our interaction method can free astrophiles of extra devices, protect their eyesight and satisfy their needs of learning astronomy at any time and any place. 

We interview two photographers and an astrophile to evaluate our product and get high praise in that the interaction method are handy and eye protective.

## Environment
IDE: Android Studio 2021.2.1

Hardware: Huawei Eyewear 3

CompileSDKVersion: 29

JDKBuildToolVersion: 29.0.3 & 28.0.3

MinSDKVersion: 23

TargetSDKVersion: 29

Gradle: 3.5.3

Python: 3.8.3

Dependencies: 

- numpy 1.23.0

- astropy 5.1

- imufusion 1.0.4

## How to Run

### Android App

On Android Studio, you can first use `Build-Make Project` to build the project. Then you can generate an APK file by `Build-Generate Signed Bundle/APK...`, or you can link your phone to the Android Studio by USB and then debug directly.

### Server

The server python code is saved in `Server` folder, you should run the program in another server like Windows, Linux or MacOS. You should set the phone and the server in the same net environment, and set the ip and port the same, then you can use the `Find The Star` function.

### Attention

Currently, users can't do any operations without Huawei Smart Glasses, but you can modify the code.

## 分工情况

cyp：nlp、语音识别、UI界面设计、声音播放、视频剪辑、展示部分ppt

sxj：socket编程、安卓前端实现、连接相关后端

yfy：server端天文库与相关计算（astropy）、视角计算（imufusion）、展示部分ppt

cjy: 全部的UI设计！

## Video

https://www.bilibili.com/video/BV1Gf4y1Z7Bg
