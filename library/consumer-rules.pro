-keep class * implements androidx.viewbinding.ViewBinding {*;}

-keepclassmembers class * implements androidx.viewbinding.ViewBinding {
public static ** bind(***);
public static ** inflate(...);
}

