package com.example.medtek.utils

import android.util.Log
import com.example.medtek.network.base.BaseException
import com.example.medtek.network.base.ErrorResponse
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import java.io.IOException

object RxUtils {
    fun <T> applyScheduler(): ObservableTransformer<T, T> {
        return ObservableTransformer {
            it
        }
    }

    fun <T> applyApiCall(): ObservableTransformer<T, T> {
        return ObservableTransformer { tObservable ->
            tObservable.subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.single())
                    .doOnError {
                        it.printStackTrace()
                    }
                    .onErrorResumeNext { throwable: Throwable ->
                        throwable.printStackTrace()
                        val httpException = throwable as? HttpException
                        if (httpException != null) {
                            try {
                                val res = httpException.response()?.errorBody()?.string()
                                Log.d("zzz res", Gson().toJson(res))
                                val gson = Gson()
//                                val type = object : TypeToken<BaseResponse<JsonElement>>() {}.type
//                                val baseResponse = gson.fromJson<BaseResponse<JsonElement>>(res.toString(), type)
                                val errorResponse = gson.fromJson<ErrorResponse>(res.toString(), ErrorResponse::class.java)
                                val e =
                                        httpException.response()?.code()?.let { BaseException(it, errorResponse) }
                                Log.d("zzz", "on error resume next")
                                return@onErrorResumeNext Observable.error(e)
//                                return@onErrorResumeNext Observable.error(throwable)

                            } catch (e: IOException) {
                                return@onErrorResumeNext Observable.error(e)
//                                Observable.empty<T>()
                            }
                        } else {
                            return@onErrorResumeNext Observable.error(throwable)
//                            Observable.empty<T>()
                        }
                    }
//                    .flatMap { res ->
//                        if (res == null){
//                            Log.d("xxx","null")
//                            return@flatMap Observable.error<T>(NullPointerException())
//                        } else if ((res as BaseResponse).error){
//                            Log.d("xxx","next")
//                            return@flatMap Observable.just(res as T)
//                        }
//                        Log.d("xxx","error")
//                        return@flatMap Observable.error<T>(BaseException(res as ErrorResponse))
//                    }
        }
    }
}