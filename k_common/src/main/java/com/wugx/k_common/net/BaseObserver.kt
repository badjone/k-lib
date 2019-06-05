package com.wugx.k_utils.net

import com.google.gson.JsonParseException
import com.wugx.k_common.R
import com.wugx.k_common.util.utilcode.util.LogUtils
import com.wugx.k_common.util.utilcode.util.ToastUtils
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import org.json.JSONException
import retrofit2.HttpException
import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.UnknownHostException
import java.text.ParseException

/**
 * 拦截返回数据处理
 */
abstract class BaseObserver<T> : Observer<T> {

    abstract fun success(data: T)

    abstract fun failure(statusCode: String, result: String)

    /**
     * 返回基础数据，只拦截基本错误信息
     */
    open fun callBaseData(result: T): Boolean {
        return false
    }


    override fun onSubscribe(d: Disposable) {
    }

    override fun onNext(t: T) {
        if (callBaseData(t)) {
            return
        }

    }

    override fun onComplete() {

    }

    override fun onError(e: Throwable) {
        LogUtils.e("DefaultObserver onError>>> ${e.toString()}")

        if (e is HttpException) {     //   HTTP错误
            onException(ExceptionReason.BAD_NETWORK)
        } else if (e is ConnectException || e is UnknownHostException) {   //   连接错误
            onException(ExceptionReason.CONNECT_ERROR)
        } else if (e is InterruptedIOException) {   //  连接超时
            onException(ExceptionReason.CONNECT_TIMEOUT)
        } else if (e is JsonParseException
                || e is JSONException
                || e is ParseException) {   //  解析错误
            onException(ExceptionReason.PARSE_ERROR)
        }

//        else if (e is ApiException) {
//            onException(ExceptionReason.CUSTOM_ERROR)
//        }
//        else if (e is NoDataExceptionException) {
//            success(null)
//        }
        else {
            onException(ExceptionReason.UNKNOWN_ERROR)
        }
        onComplete()

    }

    /**
     * 请求异常
     *
     * @param reason
     */
    private fun onException(reason: ExceptionReason) {
        when (reason) {
            BaseObserver.ExceptionReason.CONNECT_ERROR -> ToastUtils.showShort(R.string.connect_error)
            BaseObserver.ExceptionReason.CONNECT_TIMEOUT -> ToastUtils.showShort(R.string.connect_timeout)
            BaseObserver.ExceptionReason.BAD_NETWORK -> ToastUtils.showShort(R.string.bad_network)
            BaseObserver.ExceptionReason.PARSE_ERROR -> ToastUtils.showShort(R.string.parse_error)
            BaseObserver.ExceptionReason.UNKNOWN_ERROR -> ToastUtils.showShort(R.string.unknown_error)
            BaseObserver.ExceptionReason.DATA_NULL -> ToastUtils.showShort(R.string.data_null_error)
            BaseObserver.ExceptionReason.CUSTOM_ERROR -> ToastUtils.showShort(R.string.custom_error)
            else -> ToastUtils.showShort(R.string.unknown_error)
        }
    }


    /**
     * 请求网络失败原因
     */
    enum class ExceptionReason {
        /**
         * 解析数据失败
         */
        PARSE_ERROR,
        /**
         * 网络问题
         */
        BAD_NETWORK,
        /**
         * 连接错误
         */
        CONNECT_ERROR,
        /**
         * 连接超时
         */
        CONNECT_TIMEOUT,
        /**
         * 空数据
         */
        DATA_NULL,
        /**
         * 自定义异常
         */
        CUSTOM_ERROR,

        /**
         * 认证失败
         */
        AUTHOR_ERROR,
        /**
         * 未知错误
         */
        UNKNOWN_ERROR
    }
}
