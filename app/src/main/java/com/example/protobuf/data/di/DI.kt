package com.example.protobuf.data.di

import android.content.Context
import com.example.protobuf.BuildConfig
import com.example.protobuf.common.Constants.BASE_URL
import com.example.protobuf.data.api.ApiService
import com.example.protobuf.presentation.MainViewModel
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.KoinApplication
import org.koin.core.context.GlobalContext
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.util.concurrent.TimeUnit

object DI {

    private lateinit var koinApp: KoinApplication
    private val contentType = "application/json".toMediaType()

    fun init(appContext: Context){
        koinApp = GlobalContext.startKoin {
            androidLogger()
            androidContext(appContext)
            modules(
                coreModule(appContext),
                presentationModule(appContext),
                networkingModule(appContext)
            )
        }
    }

    private fun coreModule(appContext: Context) = module {
        single { Json {
            coerceInputValues = true
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = BuildConfig.DEBUG
        } }
//        single { DataStoreManager(appContext) }
    }

    private fun presentationModule(appContext: Context) =  module {
        viewModel { MainViewModel() }
    }

    private fun networkingModule(appContext: Context) = module {
        single {
            Retrofit.Builder()
                .client(
                    okhttpClient(
                        logLevel = HttpLoggingInterceptor.Level.HEADERS
                    )
                )
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(get<Json>().asConverterFactory(contentType))
                .baseUrl(BASE_URL)
                .build()
                .create(ApiService::class.java)
        }
    }

    private fun okhttpClient(
        writeTimeout: Long = 60,
        callTimeout: Long = 60,
        connectTimeout: Long = 60,
        readTimeout: Long = 60,
        logLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.NONE
    ): OkHttpClient {

        val builder = OkHttpClient.Builder()
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .callTimeout(callTimeout, TimeUnit.SECONDS)
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)

        if (logLevel != HttpLoggingInterceptor.Level.NONE && BuildConfig.DEBUG) {
            builder.addInterceptor(HttpLoggingInterceptor().apply {
                level = logLevel
            })
        }

        return builder.build()
    }


}