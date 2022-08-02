package jp.techacademy.masaya.ishihara.qa_app

import android.app.Application

class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this

    var Text1 = "aaa"








    }
    companion object {
        lateinit var instance: MainApplication
            private set
    }
}