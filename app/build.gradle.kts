import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "kr.co.lion.modigm"
    compileSdk = 34

    defaultConfig {
        applicationId = "kr.co.lion.modigm"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Kotlin DSL에서는 아래와 같이 API 키를 추가합니다.
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        val placeApiKey = localProperties.getProperty("place_api_key") ?: ""
        val kakaoNativeAppKey = localProperties.getProperty("kakao_native_app_key") ?: ""

        // 장소 API
        buildConfigField("String", "PLACE_API_KEY", "$placeApiKey")
        // 카카오 로그인 API
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "$kakaoNativeAppKey")



        // manifestPlaceholders 설정
        manifestPlaceholders["PLACE_API_KEY"] = placeApiKey
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = kakaoNativeAppKey

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.functions.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // by viewModels 사용
    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // FireBase 등록
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation("com.google.firebase:firebase-analytics")

    // FireStore 등록
    implementation("com.google.firebase:firebase-firestore:24.11.1")
    implementation("com.google.firebase:firebase-storage:20.3.0")

    // Firebase Auth 등록
    implementation("com.google.firebase:firebase-auth-ktx")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // 원형 이미지 라이브러리
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // 구글 장소 api
    implementation("com.google.android.libraries.places:places:3.3.0")

    // 카카오 로그인 api
    implementation("com.kakao.sdk:v2-user:2.20.1")
    implementation("androidx.security:security-crypto:1.0.0")
}