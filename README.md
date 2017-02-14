# 1 欧酷天气

> 这是第一行代码（第二版）最后一个案例练习。感谢郭神分享。

# 2 主要知识点

## 2.1 第三方框架

- OkHttp3 : 网络请求。
- litePal : 数据库操作。
- gson    : JSON数据解析。
- Glide   : 图片加载。

在 `build.gradle` 中添加依赖。

    // 数据库框架依赖
    compile 'org.litepal.android:core:1.4.1'
    // 网络框架
    compile 'com.squareup.okhttp3:okhttp:3.4.1'
    // gson 解析
    compile 'com.google.code.gson:gson:2.7'
    // 图片加载框架
    compile 'com.github.bumptech.glide:glide:3.7.0'