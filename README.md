# Http Invoker IDEA Plugin

Currently only supports MAC OSX.

Using Http to call the specified interface in IDEA is fast and efficient.

If you are a Java backend developer, please try this plugin.

## Core Features

1. Assemble Http Url and Method information according to configuration information and code comments.
2. The parameters are automatically parsed to assemble the Json data structure.
3. Support to extract cookie information under Google Chrome for interface login authentication.

## Operating requirements

IDEA version must be greater than or equal to 2018.3.6

## Install

**Option One**: Search for Java Http Call Helper in the plugin market to install

![image](https://user-images.githubusercontent.com/41659443/198494263-652d1915-6791-444b-9aa1-5e674a75b028.png)



**Option Two**: Self-service build

First clone the project to the local

```shell
git clone https://github.com/threeone-wang/HttpInvokerIdeaPlugin.git
```

IDEA opens the project and waits for the import to complete.

Execute Task: Gradle --> intellij --> buildPlugin

The generated plugin package is in the build/distributions directory.



**Option Three**：Download the plugin zip package from Github

https://github.com/threeone-wang/HttpInvokerIdeaPlugin/releases/tag/1.0.0



See how IDEA installs plug-ins locally:

https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_disk

## use

### Plugin configuration

![image](https://user-images.githubusercontent.com/41659443/198217931-96614c8d-fe41-4872-813a-b15492ed258a.png)



![image-20221027142950385](https://user-images.githubusercontent.com/41659443/198215169-23a23240-2b56-4a80-9183-b94df1c65e01.png)

### interface call

![image-20221027142950386](https://user-images.githubusercontent.com/41659443/198215766-1f2492bb-42c4-4ed7-90d0-08af9592d51c.png)



![image-ca78489a](https://user-images.githubusercontent.com/41659443/198216481-ca78489a-83ae-490b-b712-92b8e442985a.png)

