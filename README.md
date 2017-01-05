### Usage

首先，安装插件“CompileCheck”并重启IDE

## Module目录的compile解析

选择需要检测的Module目录，右键点击“Analyze Project Compile”，
之后就会显示改Module的build.gradle下的依赖库自身依赖的sdk信息了，
该插件只支持一级compile的解析
<img src="./sceenshot/screenshot_method_for_module.png"  width="600" height="947"/>

## 单个compile信息的查询

选择需要解析的compile库，比如“AndroidPluginDemo”，
右键点击“Compile Check”，之后就会显示改库所依赖的第三方库了
<img src="./sceenshot/screenshot_method_for_compile.png"  width="570" height="580"/>
