## Usage

首先，安装插件“CompileCheck”并重启IDE

### Setting自定义仓库

<img src="./screenshot/screenshot_setting.png"  width="613" height="400"/>

#### 依赖库的反向依赖关系查询

选择需要解析的依赖库，比如“RxCache1.2”，
右键点击“Check Local Compile”，之后就会显示当前的Project里面Modul所依赖的关系了<br/>
<img src="./screenshot/screenshot_method_for_local.png"  width="602" height="606"/><br/>
<img src="./screenshot/screenshot_result_for_local.png"  width="560" height="300"/>

#### Module目录的compile解析

选择需要检测的Module目录，右键点击“Analyze Project Compile”，
之后就会显示改Module的build.gradle下的依赖库自身依赖的sdk信息了，
该插件只支持一级compile的解析<br/>
<img src="./screenshot/screenshot_method_for_module.png"  width="600" height="947"/><br/>
<img src="./screenshot/screenshot_result_for_module.png"  width="700" height="307"/>

#### 单个compile信息的查询

选择需要解析的compile库，比如“AndroidPluginDemo”，
右键点击“Compile Check”，之后就会显示该库所依赖的第三方库了<br/>
<img src="./screenshot/screenshot_method_for_compile.png"  width="570" height="580"/><br/>
<img src="./screenshot/screenshot_result_for_compile.png"  width="566" height="333"/>
