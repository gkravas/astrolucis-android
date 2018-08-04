package com.astrolucis.services

import com.apollographql.apollo.rx2.Rx2Apollo
import com.astrolucis.UpdateUserMutation
import com.astrolucis.exceptions.GraphQLException
import com.astrolucis.fragment.UserFragment
import com.astrolucis.services.interfaces.Preferences
import com.astrolucis.services.interfaces.UserService.Companion.TAG
import com.astrolucis.services.repsonses.LoginResponse
import com.facebook.login.LoginManager
import io.reactivex.Observable

class UserService(private val restService: RestService,
                  private val graphQLService: GraphQLService,
                  private val preferences: Preferences): com.astrolucis.services.interfaces.UserService {

    override fun registerFirebaseToken(token: String, language: String, os: String): Observable<String> {
        return restService.authenticationAPI.registerFirebaseToken(token, language, os)
    }

    override fun unregisterFirebaseToken(token: String, language: String, os: String): Observable<String> {
        return restService.authenticationAPI.unregisterFirebaseToken(token, language, os)
    }

    override fun changeEmail(email: String): Observable<String> {
        return restService.authenticationAPI.changeEmail(email)
    }

    override fun register(email: String, password: String): Observable<String> {
        return restService.authenticationAPI.register(email, password)
    }

    override fun login(email: String, password: String): Observable<LoginResponse> {
        return restService.authenticationAPI.login(email, password, null)
    }

    override fun fbLogin(fbToken: String): Observable<LoginResponse> {
        return restService.authenticationAPI.fbLogin(fbToken)
    }

    override fun sendResetEmail(email: String): Observable<String> {
        return restService.authenticationAPI.sendResetEmail(email)
    }

    override fun resetPassword(password: String): Observable<String> {
        return restService.authenticationAPI.resetPassword(password)
    }

    override fun updateLivingLocation(livingLocation: String): Observable<UserFragment?> {
        return Rx2Apollo.from(graphQLService.apolloClient
                .mutate(UpdateUserMutation.builder()
                        .location(livingLocation)
                        .build()
                )
        ).flatMap {
            if (it.hasErrors()) {
                Observable.error(GraphQLException(TAG, it.errors()))
            } else {
                Observable.just(it.data()?.updateUser()?.fragments()?.userFragment())
            }
        }
    }

    override fun logout() {
        preferences.reset()
        LoginManager.getInstance().logOut()
    }
}
