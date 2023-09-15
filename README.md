# an-mvi

### 依赖
```groovy
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
```groovy
    dependencies {
		implementation 'com.github.hhymason:an-util:1.0.0'
	}
```

### 混淆

* 因为使用了反射来封装 viewbinding 所以需要添加混淆文件，如下:
* 
```
-keep class * implements androidx.viewbinding.ViewBinding {*;}
-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
    public static ** bind(***);
    public static ** inflate(...);
}
```
