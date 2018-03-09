package io.github.alphacalculus.alphacalculus

import android.app.Application

class TheApp : Application() {

    private var userManager: UserModel? = null

    var isAuthed = false
        private set
    var username = "未登录"
        private set
    var uid = 0
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        userManager = UserModel(applicationContext)
    }

    fun logout() {
        this.isAuthed = false
        this.username = "未登录"
        this.uid = 0
    }

    fun doAuth(username: String, password: String) {
        this.isAuthed = userManager!!.auth(username, password)
        if (isAuthed) {
            this.username = username
            this.uid = userManager!!.getUid(username)
        }
    }

    fun auth(username: String, password: String): Boolean {
        this.doAuth(username, password)
        return this.isAuthed
    }

    fun register(username: String, password: String): Boolean {
        try {
            userManager!!.register(username, password)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    companion object {
        var instance: TheApp? = null
            private set
    }

}
