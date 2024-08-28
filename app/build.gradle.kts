import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
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

        // MySQL 연결 정보
        val databaseUrl = localProperties.getProperty("database_url") ?: ""
        val databaseUser = localProperties.getProperty("database_user") ?: ""
        val databasePassword = localProperties.getProperty("database_password") ?: ""

        buildConfigField("String", "DB_URL", databaseUrl)
        buildConfigField("String", "DB_USER", databaseUser)
        buildConfigField("String", "DB_PASSWORD", databasePassword)

        // aws s3
        val bucketAccessKey = localProperties.getProperty("bucket_accessKey") ?:""
        val bucketSecretKey = localProperties.getProperty("bucket_secretKey") ?:""
        val bucketName = localProperties.getProperty("bucket_name") ?:""

        buildConfigField("String", "BK_ACCESSKEY", bucketAccessKey)
        buildConfigField("String", "BK_SECRETKEY", bucketSecretKey)
        buildConfigField("String", "BK_NAME", bucketName)

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
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    // 원형 이미지 라이브러리
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // 구글 장소 api
    implementation("com.google.android.libraries.places:places:3.3.0")

    // 카카오 로그인 api
    implementation("com.kakao.sdk:v2-user:2.20.1")
    implementation("androidx.security:security-crypto:1.0.0")

    // MySQL
    implementation("mysql:mysql-connector-java:5.1.48")

    // hikari
    implementation("com.zaxxer:HikariCP:2.7.9")

    // aws s3
    implementation("com.amazonaws:aws-android-sdk-s3:2.22.0")
    implementation("com.amazonaws:aws-android-sdk-core:2.22.0")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    
    // libphonenumber (국가별 전화번호 변환 대응)
    implementation("com.googlecode.libphonenumber:libphonenumber:8.13.42")

    // 리사이클러뷰 스와이프 새로고침
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    // 스켈레톤 UI
    implementation("com.facebook.shimmer:shimmer:0.5.0")
}