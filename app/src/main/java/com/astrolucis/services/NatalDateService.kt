package com.astrolucis.services

import com.apollographql.apollo.rx2.Rx2Apollo
import com.astrolucis.CreateNatalDateMutation
import com.astrolucis.GetNatalDatesQuery
import com.astrolucis.UpdateNatalDateMutation
import com.astrolucis.exceptions.GraphQLException
import com.astrolucis.fragment.NatalDateFragment
import com.astrolucis.fragment.UserFragment
import com.astrolucis.services.interfaces.NatalDateService.Companion.TAG
import com.astrolucis.services.interfaces.Preferences
import io.reactivex.Observable

class NatalDateService(private val graphQLService: GraphQLService,
                       private val preferences: Preferences): com.astrolucis.services.interfaces.NatalDateService {

    override fun getAll(): Observable<UserFragment> {
        return Rx2Apollo.from(graphQLService.apolloClient
                .query(GetNatalDatesQuery
                        .builder()
                        .build()
                )
        ).flatMap {
            preferences.me = it.data()?.me()?.fragments()?.userFragment()
            Observable.just(preferences.me)
        }
    }

    override fun createNatalDateMutation(date: String, location: String, name: String,
                                primary: Boolean, type: String): Observable<NatalDateFragment?>? {
        return Rx2Apollo.from(graphQLService.apolloClient
                .mutate(CreateNatalDateMutation
                        .builder()
                        .date(date)
                        .location(location)
                        .name(name)
                        .primary(primary)
                        .type(type)
                        .build()
                )
        ).flatMap {
            if (it.hasErrors()) {
                Observable.error(GraphQLException(TAG, it.errors()))
            } else {
                Observable.just(it.data()?.createNatalDate()?.fragments()?.natalDateFragment())
            }
        }
    }

    override fun updateNatalDateMutation(id: Long, date: String, location: String, name: String,
                                primary: Boolean, type: String): Observable<NatalDateFragment>? {
        return Rx2Apollo.from(graphQLService.apolloClient
                .mutate(UpdateNatalDateMutation
                        .builder()
                        .id(id)
                        .date(date)
                        .location(location)
                        .name(name)
                        .primary(primary)
                        .type(type)
                        .build()
                )
        ).flatMap {
            if (it.hasErrors()) {
                Observable.error(GraphQLException(TAG, it.errors()))
            } else {
                Observable.just(it.data()?.updateNatalDate()?.fragments()?.natalDateFragment())
            }
        }
    }
}