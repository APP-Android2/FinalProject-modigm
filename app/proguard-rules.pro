# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.mysql.jdbc.Driver { *; }
-keep class com.mysql.jdbc.NonRegisteringDriver { *; }

# 소스 파일 및 라인 정보 유지
-keepattributes SourceFile,LineNumberTable

# 소스 파일의 변수명 변경
-renamesourcefileattribute SourceFile

# Firebase Cloud Messaging
-keep class com.google.firebase.** { *; }
-keep interface com.google.firebase.** { *; }
# FCM 관련 클래스 유지
-keep class com.google.android.** { *; }
-keep class com.google.** { *; }

# Retrofit
-keep class retrofit2.** { *; }
-keep class com.squareup.retrofit2.** { *; }
-keep class okhttp3.** { *; }

# kakao login
-keep interface com.kakao.sdk.** { *; }

# 카카오 SDK 관련 클래스와 메서드를 난독화에서 제외
-keep class com.kakao.sdk.** { *; }
-keep class com.kakao.auth.** { *; }
-keep class com.kakao.network.** { *; }
-keep class com.kakao.util.** { *; }
-keep class com.kakao.util.helper.** { *; }
-keep class com.kakao.sdk.**.model.* { <fields>; }
-keep class * extends com.google.gson.TypeAdapter

-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.*
-dontwarn org.openjsse.**

# kr.co.lion.modigm 패키지의 모든 model 패키지 및 그 하위 패키지 내 클래스의 필드 이름 보호
# 서버 응답 데이터 필드 이름 난독화 예외 처리
-keep class kr.co.lion.modigm.**.model.** {
    <fields>;
}

# Kotlin 특수 클래스 유지
-keepclassmembers class **$WhenMappings { <fields>; }
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { public <methods>; }

-dontwarn org.apache.http.**
-dontwarn android.net.http.AndroidHttpClient
-keep class android.net.http.** { *; }
-keep class org.apache.http.** { *; }

# Please add these rules to your existing keep rules in order to suppress warnings.
# This is generated automatically by the Android Gradle plugin.
-dontwarn com.codahale.metrics.Gauge
-dontwarn com.codahale.metrics.Histogram
-dontwarn com.codahale.metrics.Meter
-dontwarn com.codahale.metrics.Metric
-dontwarn com.codahale.metrics.MetricFilter
-dontwarn com.codahale.metrics.MetricRegistry
-dontwarn com.codahale.metrics.Timer
-dontwarn com.codahale.metrics.health.HealthCheck
-dontwarn com.codahale.metrics.health.HealthCheckRegistry
-dontwarn io.micrometer.core.instrument.Counter$Builder
-dontwarn io.micrometer.core.instrument.Counter
-dontwarn io.micrometer.core.instrument.Gauge$Builder
-dontwarn io.micrometer.core.instrument.Gauge
-dontwarn io.micrometer.core.instrument.MeterRegistry
-dontwarn io.micrometer.core.instrument.Timer$Builder
-dontwarn io.micrometer.core.instrument.Timer
-dontwarn java.lang.management.ManagementFactory
-dontwarn java.lang.management.ThreadInfo
-dontwarn java.lang.management.ThreadMXBean
-dontwarn java.rmi.server.UID
-dontwarn javax.management.MBeanServer
-dontwarn javax.management.ObjectInstance
-dontwarn javax.management.ObjectName
-dontwarn org.slf4j.impl.StaticLoggerBinder
-dontwarn java.lang.reflect.AnnotatedType

-keep class com.mysql.jdbc.** { *; }
-dontwarn com.mysql.jdbc.**

-keep class com.amazonaws.** { *; }
-keepattributes Signature, *Annotation*