package br.pro.nobile.zwiftmobileapikt.model.secure

import android.util.Log
import androidx.lifecycle.MutableLiveData
import br.pro.nobile.zwiftmobileapikt.model.secure.SecureZwiftApi.Companion.BASE_URL
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class SecureZwiftService {
    private val secureZwiftApi = with (Retrofit.Builder()) {
        baseUrl(BASE_URL)
        addConverterFactory(GsonConverterFactory.create())
        build()
    }.create(SecureZwiftApi::class.java)

    fun callSyncGetTokenData(
        username: String,
        password: String,
        grantType: String,
        clientId: String
    ): TokenData? {
        var tokenData: TokenData? = null
        try {
            val response = secureZwiftApi.getTokenData(
                username,
                password,
                grantType,
                clientId
            ).execute()

            tokenData = if (response.isSuccessful) response.body() else null
        } catch (ioe: IOException) {
            Log.e(this.javaClass.name, "IOException in callGetTokenData")
        }
        return tokenData
    }

    fun callAsyncGetTokenData(
        username: String,
        password: String,
        grantType: String,
        clientId: String
    ): MutableLiveData<TokenData?> {
        val tokenData: MutableLiveData<TokenData?> = MutableLiveData()
        try {
            val response = secureZwiftApi.getTokenData(
                username,
                password,
                grantType,
                clientId
            ).enqueue(
                object : Callback<TokenData?> {
                    override fun onFailure(call: Call<TokenData?>, t: Throwable) {
                        tokenData.value = null
                    }

                    override fun onResponse(
                        call: Call<TokenData?>,
                        response: Response<TokenData?>
                    ) {
                        tokenData.value = response.body()
                    }
                }
            )
        } catch (ioe: IOException) {
            Log.e(this.javaClass.name, "IOException in callGetTokenData")
        }
        return tokenData
    }

    fun callSyncRefreshTokenData(
        refreshToken: String,
        grantType: String,
        clientId: String
    ): TokenData? {
        var tokenData: TokenData? = null
        try {
            val response = secureZwiftApi.refreshTokenData(
                refreshToken,
                grantType,
                clientId
            ).execute()

            tokenData = if (response.isSuccessful) response.body() else null
        }
        catch (ioe: IOException) {
            Log.e(this.javaClass.name, "IOException in callRefreshTokenData")
        }
        return tokenData
    }

    fun callAsyncRefreshTokenData(
        refreshToken: String,
        grantType: String,
        clientId: String
    ): MutableLiveData<TokenData?> {
        val tokenData: MutableLiveData<TokenData?> = MutableLiveData()
        try {
            val response = secureZwiftApi.refreshTokenData(
                refreshToken,
                grantType,
                clientId
            ).enqueue(
                object : Callback<TokenData?> {
                    override fun onFailure(call: Call<TokenData?>, t: Throwable) {
                        tokenData.value = null
                    }

                    override fun onResponse(
                        call: Call<TokenData?>,
                        response: Response<TokenData?>
                    ) {
                        tokenData.value = response.body()
                    }
                }
            )
        }
        catch (ioe: IOException) {
            Log.e(this.javaClass.name, "IOException in callRefreshTokenData")
        }
        return tokenData
    }
}