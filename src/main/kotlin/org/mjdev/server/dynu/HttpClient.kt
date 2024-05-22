@file:Suppress("MemberVisibilityCanBePrivate", "PropertyName")

package org.mjdev.server.dynu

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class HttpClient {
    val IPV4: String?
        get() = try {
            val url = URL(IPV_4_API)
            println("Connecting to: $IPV_4_API")
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.setRequestProperty("User-Agent", USER_AGENT)
            con.connectTimeout = TIMEOUT
            con.readTimeout = TIMEOUT
            if (con.responseCode == HttpURLConnection.HTTP_OK) {
                val inp = BufferedReader(InputStreamReader(con.inputStream))
                val ipv4 = inp.readLine()
                inp.close()
                ipv4
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

    fun updateIP(
        user: String,
        password: String
    ): String {
        return try {
            val password256 = password.sha256()
            val apiUrl = String.format(DYNU_API, user, password256, IPV4)
            val url = URL(apiUrl)
            val con = url.openConnection() as HttpURLConnection
            con.requestMethod = "GET"
            con.setRequestProperty("User-Agent", USER_AGENT)
            con.connectTimeout = DYNU_TIMEOUT
            con.readTimeout = DYNU_TIMEOUT
            val responseCode = con.responseCode
            if (con.responseCode == HttpURLConnection.HTTP_OK) {
                val inp = BufferedReader(InputStreamReader(con.inputStream))
                val response = inp.readLine()
                inp.close()
                if (response == "nochg") {
                    return "IP did not change."
                }
                return if (response == "badauth") {
                    "User or password incorrect."
                } else response
            }
            String.format("Api error: Response code:%s", responseCode)
        } catch (e: Exception) {
            String.format("Connection Failed (%s).", e.message)
        }
    }

    companion object {
        private const val TIMEOUT = 3 * 1000
        private const val USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36 OPR/38.0.2220.41"
        private const val IPV_4_API = "http://checkip.amazonaws.com"
        private const val DYNU_TIMEOUT = 60 * 1000
        //myipv6=
        private const val DYNU_API = "http://api.dynu.com/nic/update?username=%s&password=%s&myip=%s"
    }
}