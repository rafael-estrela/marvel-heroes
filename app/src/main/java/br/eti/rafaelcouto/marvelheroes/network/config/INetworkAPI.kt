package br.eti.rafaelcouto.marvelheroes.network.config

import br.eti.rafaelcouto.marvelheroes.BuildConfig
import br.eti.rafaelcouto.marvelheroes.model.Character
import br.eti.rafaelcouto.marvelheroes.model.CharacterDetails
import br.eti.rafaelcouto.marvelheroes.model.Comic
import br.eti.rafaelcouto.marvelheroes.model.general.ResponseBody
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface INetworkAPI {
    companion object {
        val baseApi: INetworkAPI
            get() = Retrofit.Builder()
                .baseUrl(BuildConfig.API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .client(AuthenticatorClient.build())
                .build()
                .create(INetworkAPI::class.java)
    }

    @GET("v1/public/characters")
    fun getPublicCharacters(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Single<ResponseBody<Character>>

    @GET("v1/public/characters/{characterId}")
    fun getPublicCharacterInfo(
        @Path("characterId") characterId: Int
    ): Single<ResponseBody<CharacterDetails>>

    @GET("v1/public/characters/{characterId}/comics")
    fun getPublicCharacterComics(
        @Path("characterId") characterId: Int,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Single<ResponseBody<Comic>>
}
