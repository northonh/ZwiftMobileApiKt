package br.pro.nobile.zwiftmobileapikt.zwift

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import br.pro.nobile.zwiftmobileapikt.model.secure.SecureZwiftService
import br.pro.nobile.zwiftmobileapikt.model.secure.TokenData

class AuthToken(private val username: String, private val password: String) {
    companion object {
        const val GRANT_TYPE_PASSWORD = "password"
        const val GRANT_TYPE_REFRESH_TOKEN = "refresh_token"
        const val CLIENT_ID = "Zwift_Mobile_Link"
        const val SECONDS_TO_EXPIRE = 5
    }
    private val secureZwiftService: SecureZwiftService =
        SecureZwiftService()

    // Token data from the API response
    private var tokenData: TokenData? = null
    private var asyncAccessToken: MutableLiveData<String?> = MutableLiveData()

    // absolute expire times of the access and refresh tokens
    private var accessTokenExpiration: Long = -1
    private var refreshTokenExpiration: Long = -1

    private fun fetchSyncTokenData() {
        // Prepare and call the correct request
        tokenData = if (haveValidRefreshToken()) {
            secureZwiftService.callSyncRefreshTokenData(
                tokenData?.refreshToken!!,
                GRANT_TYPE_REFRESH_TOKEN, CLIENT_ID
            )
        } else {
            secureZwiftService.callSyncGetTokenData(
                username,
                password,
                GRANT_TYPE_PASSWORD,
                CLIENT_ID
            )
        }
        updateTokenData()
    }

    private fun fetchAsyncTokenData(lifecycleOwner: LifecycleOwner) {
        // Prepare and call the correct request
        if (haveValidRefreshToken()) {
            secureZwiftService.callAsyncRefreshTokenData(
                tokenData?.refreshToken!!,
                GRANT_TYPE_REFRESH_TOKEN, CLIENT_ID
            ).observe(
                lifecycleOwner,
            Observer { asyncTokenData ->
                if (asyncTokenData != null) {
                    tokenData = asyncTokenData
                    asyncAccessToken.value = asyncTokenData.accessToken
                    updateTokenData()
                }
            })
        } else {
            secureZwiftService.callAsyncGetTokenData(
                username,
                password,
                GRANT_TYPE_PASSWORD,
                CLIENT_ID
            ).observe(
                lifecycleOwner,
                Observer { asyncTokenData ->
                    if (asyncTokenData != null) {
                        tokenData = asyncTokenData
                        asyncAccessToken.value = asyncTokenData.accessToken
                        updateTokenData()
                    }
                })
        }
    }

    private fun updateTokenData() {
        accessTokenExpiration = nowInSeconds() + tokenData?.expiresIn!! - SECONDS_TO_EXPIRE
        refreshTokenExpiration = nowInSeconds() + tokenData?.refreshExpiresIn!! - SECONDS_TO_EXPIRE
    }

    fun getSyncAccessToken(): String? {
        if (!haveValidAccessToken()) {
            fetchSyncTokenData()
        }
        return tokenData?.accessToken
    }

    fun getAsyncAccessToken(lifecycleOwner: LifecycleOwner): MutableLiveData<String?> {
        if (!haveValidAccessToken()) {
            fetchAsyncTokenData(lifecycleOwner)
        }
        return asyncAccessToken
    }


    private fun haveValidAccessToken(): Boolean = !(tokenData?.accessToken == null || nowInSeconds() > accessTokenExpiration)
    private fun haveValidRefreshToken(): Boolean = !(tokenData?.refreshToken == null || nowInSeconds() > refreshTokenExpiration)

    private fun nowInSeconds(): Long {
        return System.currentTimeMillis() / 1000;
    }
}