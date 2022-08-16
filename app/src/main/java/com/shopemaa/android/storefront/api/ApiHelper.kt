package com.shopemaa.android.storefront.api

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.api.http.HttpHeader

object ApiHelper {
    fun apolloClient(): ApolloClient {
        return ApolloClient
            .Builder()
            .serverUrl("https://api.shopemaa.com/query")
            .build()
    }

    fun apolloClient(headers: Map<String, String>): ApolloClient {
        return ApolloClient
            .Builder()
            .httpHeaders(headers.map { kv -> HttpHeader(kv.key, kv.value) })
            .serverUrl("https://api.shopemaa.com/query")
            .build()
    }
}
