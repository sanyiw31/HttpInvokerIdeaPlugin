# Http Invoker IDEA Plugin

目前仅支持MAC OSX。

在IDEA中使用Http调用指定接口，快捷高效。

如果你是一名Java后端开发，敬请尝试本插件。

## 核心特性

1. 根据配置信息、代码注解，拼装Http Url、Methond信息。
2. 参数自动解析拼装Json数据结构。
3. 支持提取谷歌浏览器下的Cookie信息，用于接口登录鉴权。

## 运行要求

IDEA版本需大于等于2018.3.6

## 安装

方法一：自助构建

先clone工程到本地

```shell
git clone https://github.com/threeone-wang/HttpInvokerIdeaPlugin.git
```

IDEA打开工程，待导入完毕后。

执行Task：Gradle ——》 intellij ——》 buildPlugin

生成的插件包在 build/distributions 目录下。



方法二：Github中下载插件压缩包

https://github.com/threeone-wang/HttpInvokerIdeaPlugin/releases/tag/1.0.0



IDEA从本地安装插件方式见：

https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_disk

## 使用

### 插件配置

![image](https://user-images.githubusercontent.com/41659443/198217931-96614c8d-fe41-4872-813a-b15492ed258a.png)



![image-20221027142950385](https://user-images.githubusercontent.com/41659443/198215169-23a23240-2b56-4a80-9183-b94df1c65e01.png)

### 接口调用

![image-20221027142950386](https://user-images.githubusercontent.com/41659443/198215766-1f2492bb-42c4-4ed7-90d0-08af9592d51c.png)



![image-ca78489a](https://user-images.githubusercontent.com/41659443/198216481-ca78489a-83ae-490b-b712-92b8e442985a.png)

