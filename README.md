# an-mvi


### 混淆
* 因为使用了反射来封装 viewbinding 所以需要添加混淆文件，如下:
-keep class * implements androidx.viewbinding.ViewBinding {*;}

-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
public static ** bind(***);
public static ** inflate(...);
}
