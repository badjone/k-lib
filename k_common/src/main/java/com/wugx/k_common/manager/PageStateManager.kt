package com.wugx.k_common.manager

import android.app.Activity
import android.text.TextUtils
import android.view.View
import android.view.ViewStub
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.wugx.k_common.R

/**
 * 无数据 网络异常 错误 状态页管理类
 * @Author badjone
 * @Date 2019/6/1 17:08
 */
class PageStateManager {

    /**
     * Activity中由于服务器异常导致加载失败显示的布局。
     */
    private var loadErrorView: View? = null

    /**
     * Activity中由于网络异常导致加载失败显示的布局。
     */
    private var badNetworkView: View? = null

    /**
     * Activity中当界面上没有任何内容时展示的布局。
     */
    private var noContentView: View? = null

    /**
     * 当Activity中的加载内容服务器返回失败，通过此方法显示提示界面给用户。
     *
     * @param tip
     * 界面中的提示信息
     */
    open fun showErrorView(ct: Activity, tip: String?, listener: View.OnClickListener) {
        if (loadErrorView != null) {
            loadErrorView?.visibility = View.VISIBLE
            return
        }

        val viewStub = ct.findViewById<ViewStub>(R.id.viewError)
        if (viewStub != null) {
            loadErrorView = viewStub.inflate()
            loadErrorView?.findViewById<ConstraintLayout>(R.id.cl_error)?.setOnClickListener(listener)
            val loadErrorText = loadErrorView?.findViewById<TextView>(R.id.tv_network_msg)
            if (!TextUtils.isEmpty(tip)) {
                loadErrorText?.text = tip
            }
        }
    }

    /**
     * 显示状态页面重试
     */

    /**
     * 当Activity中的内容因为网络原因无法显示的时候，通过此方法显示提示界面给用户。
     *
     * @param listener
     * 重新加载点击事件回调
     */
    open fun showBadNetworkView(ct: Activity, tip: String?, listener: View.OnClickListener) {
        if (badNetworkView != null) {
            badNetworkView?.visibility = View.VISIBLE
            return
        }
        val viewStub = ct.findViewById<ViewStub>(R.id.viewNetworkError)
        if (viewStub != null) {
            badNetworkView = viewStub.inflate()
            val badNetworkRootView = badNetworkView?.findViewById<TextView>(R.id.tv_network_msg)
            loadErrorView?.findViewById<ConstraintLayout>(R.id.cl_no_network)?.setOnClickListener(listener)
            if (!TextUtils.isEmpty(tip)) {
                badNetworkRootView?.text = tip
            }
        }
    }

    /**
     * 当Activity中没有任何内容的时候，通过此方法显示提示界面给用户。
     * @param tip
     * 界面中的提示信息
     */
    open fun showNoContentView(ct: Activity, tip: String?, listener: View.OnClickListener) {
        if (noContentView != null) {
            noContentView?.visibility = View.VISIBLE
            return
        }
        val viewStub = ct.findViewById<ViewStub>(R.id.viewNoContent)
        if (viewStub != null) {
            noContentView = viewStub.inflate()
            val noContentText = noContentView?.findViewById<TextView>(R.id.tv_no_data_msg)
            loadErrorView?.findViewById<ConstraintLayout>(R.id.cl_no_data)?.setOnClickListener(listener)
            if (!TextUtils.isEmpty(tip)) {
                noContentText?.text = tip
            }
        }
    }

    /**
     * 将load error view进行隐藏。
     */
    open fun hideLoadErrorView() {
        loadErrorView?.visibility = View.GONE
    }

    /**
     * 将no content view进行隐藏。
     */
    open fun hideNoContentView() {
        noContentView?.visibility = View.GONE
    }

    /**
     * 将bad network view进行隐藏。
     */
    open fun hideBadNetworkView() {
        badNetworkView?.visibility = View.GONE
    }

}