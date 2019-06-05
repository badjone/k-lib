package com.wugx.k_common

import android.content.Context
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.FalsifyFooter
import com.scwang.smartrefresh.layout.header.FalsifyHeader
import com.wugx.k_common.util.utilcode.util.Utils

/**
 *  初始化入口
 * @Author badjone
 * @Date 2019/5/31 18:40
 */
object KCommon {
    // 请求url
    lateinit var base_url: String
    // 请求公共配置参数
    lateinit var map: Map<String, String>

    /**
     * 初始化必须调用方法
     */
    fun create(appContext: Context):KCommon {
        Utils.init(appContext)
        initSmartLayout()
        return this@KCommon
    }

    /**
     * 设置网络请求及参数
     *
     * paramsMap 公共参数...
     */
    fun initNetParams(baseUrl: String, paramsMap: Map<String, String>): KCommon {
        base_url = baseUrl
        map = paramsMap
        return this@KCommon
    }


    private fun initSmartLayout() {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { mContext, _ ->
            FalsifyHeader(mContext)
        }
        //设置全局的Footer构建器
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            FalsifyFooter(context)
        }
    }

}