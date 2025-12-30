plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.nike_fe"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.nike_fe"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Retrofit & OkHttp - Kết nối Backend API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    
    // Gson
    implementation("com.google.code.gson:gson:2.10.1")
    
    // CardView
    implementation("androidx.cardview:cardview:1.0.0")
    
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    
    // CoordinatorLayout
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
    
    // Glide - Image loading library
    implementation("com.github.bumptech.glide:glide:4.16.0")
    
    // ViewPager2
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // CircleImageView
    implementation("de.hdodenhof:circleimageview:3.1.0")
    
    // MPAndroidChart - Thư viện vẽ biểu đồ
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}