package com.astrolucis

import android.os.Bundle
import com.astrolucis.core.BaseActivity
import com.astrolucis.utils.routing.AppRouter
import org.koin.android.ext.android.inject


class SplashActivity : BaseActivity() {

    val appRouter: AppRouter by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appRouter.identifyFrom(intent.data).also {
            appRouter.goTo(it.kClass, this, it.bundle, true)
        }
    }
}
