package br.eti.rafaelcouto.marvelheroes.data.api

import br.eti.rafaelcouto.marvelheroes.BuildConfig
import br.eti.rafaelcouto.marvelheroes.model.Character
import br.eti.rafaelcouto.marvelheroes.model.CharacterDetails
import br.eti.rafaelcouto.marvelheroes.model.Comic
import br.eti.rafaelcouto.marvelheroes.model.general.ResponseBody
import retrofit2.Retrofit
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
                .client(AuthenticatorClient.build())
                .build()
                .create(INetworkAPI::class.java)
    }

    @GET("v1/public/characters")
    suspend fun getPublicCharacters(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
        @Query("nameStartsWith") name: String?
    ): ResponseBody<Character>

    @GET("v1/public/characters/{characterId}")
    suspend fun getPublicCharacterInfo(
        @Path("characterId") characterId: Int
    ): ResponseBody<CharacterDetails>

    @GET("v1/public/characters/{characterId}/comics")
    suspend fun getPublicCharacterComics(
        @Path("characterId") characterId: Int,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): ResponseBody<Comic>
}
