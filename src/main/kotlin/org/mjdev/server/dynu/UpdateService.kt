@file:Suppress("MemberVisibilityCanBePrivate")

package org.mjdev.server.dynu

class UpdateService : Thread() {
    private var data: Data = Data()
    private var client: HttpClient? = null

    var isRunning = false
        private set

    var user: String
        get() = data.user
        set(value) {
            data.user = value
        }

    var password: String
        get() = data.password
        set(password) {
            data.password = password
        }

    var ttl: Int
        get() = data.ttl
        set(value) {
            data.ttl = value
        }

    fun startService() {
        if (data.isEmpty) {
            isRunning = false
        } else if (!isRunning) {
            isRunning = true
            client = HttpClient()
            println("Process started.")
            start()
        }
    }

    override fun run() {
        while (isRunning) {
            if (data.isEmpty) {
                println("User data not found")
            } else {
                println("Calling api.")
                val response = client?.updateIP(data.user, data.password)
                println(response ?: "http response empty")
                var nextCallTime = String.format("%s seconds", data.ttl)
                if (data.ttl >= 60) {
                    nextCallTime = String.format("%s minutes(s)", data.ttl / 60f)
                }
                println(String.format("Next call in %s.", nextCallTime))
                try {
                    sleep(data.ttl * 1000L)
                } catch (_: InterruptedException) {
                }
            }
        }
    }

    fun stopService() {
        isRunning = false
        println("Process stopped.")
    }
}