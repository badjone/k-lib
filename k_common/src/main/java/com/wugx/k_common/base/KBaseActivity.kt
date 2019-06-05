package com.wugx.k_common.base

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.widget.Toolbar
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.trello.rxlifecycle3.LifecycleTransformer
import com.trello.rxlifecycle3.android.ActivityEvent
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import com.wugx.k_common.R
import com.wugx.k_common.manager.PageStateManager
import com.wugx.k_utils.base.IBaseView

/**
 * Activity的基类
 *
 * @Author badjone
 * @Date 2019/6/1 11:41
 */
@SuppressLint("Registered")
abstract class KBaseActivity : RxAppCompatActivity(), IBaseView {

    abstract fun initCreate(savedInstanceState: Bundle?)
    abstract fun getLayoutId(): Int
    /**
     * 是否显示toolbar
     */
    open fun isShowTitle(): Boolean = false
    
    /**
     * 是否使用页面回弹效果
     */
    open fun isRebound(): Boolean = true

    /**
     * 判断当前Activity是否在前台。
     */
    private var isActive: Boolean = false
    /**
     * 页面状态
     */
    private var pageState: PageStateManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // 设置只支持竖屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        super.onCreate(savedInstanceState)
        if (null == pageState) {
            pageState = PageStateManager()
        }
        setBaseLayout()

//        EventBus.getDefault().register(this)
        initCreate(savedInstanceState)
    }

    private lateinit var smartRefreshLayout: SmartRefreshLayout
    @Nullable
    private fun setBaseLayout(): View? {
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val layParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT, 1f
        )

        val contentLayout = LayoutInflater.from(this).inflate(getLayoutId(), layout, false)
        if (isShowTitle()) {
            val layoutTitle = View.inflate(this, R.layout.layout_title, null)
            val p = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            initActionBar(layoutTitle)
            layout.addView(layoutTitle, p)
        }

        if (isRebound()) {
            smartRefreshLayout = SmartRefreshLayout(this)
            smartRefreshLayout.setBackgroundResource(R.color.gray_f2f2f2)
            smartRefreshLayout.layoutParams = layParams
            if (contentLayout != null) smartRefreshLayout.addView(contentLayout, layParams)
            layout.addView(smartRefreshLayout, layParams)
        } else {
            layout.addView(contentLayout, layParams)
        }
        setContentView(layout, layParams)
        return contentLayout
    }


    private lateinit var tvTitle: TextView
    private lateinit var mToolbar: Toolbar

    private fun initActionBar(v: View) {
        tvTitle = v.findViewById(R.id.tv_title)
        mToolbar = v.findViewById(R.id.toolbar_layout)
        mToolbar.title = ""
        setSupportActionBar(mToolbar)
        supportActionBar?.let {
            it.setDisplayShowHomeEnabled(false)
            it.setHomeAsUpIndicator(R.mipmap.ic_arrow_back)
            it.setDisplayHomeAsUpEnabled(showBackIcon())
            //设置返回按钮
//            val topActivity = ActivityUtils.getTopActivity()
//            it.setDisplayHomeAsUpEnabled(topActivity::class.java.name != MainActivity::class.java.name)
        }


        val defaultTitle = title.toString()
        if (TextUtils.isEmpty(defaultTitle)) {
            tvTitle.setText(R.string.app_name)
        } else {
            tvTitle.text = defaultTitle
        }
    }

    /**
     * 是否隐藏 toolbar 返回按钮
     */
    open fun showBackIcon(): Boolean = true

    fun setTitleTxt(title: String) {
        if (!isShowTitle()) return
        if (!TextUtils.isEmpty(title)) {
            tvTitle.text = title
        }
    }

    fun setTitleTxt(resId: Int) {
        if (!isShowTitle()) return
        tvTitle.setText(resId)
    }

    fun getSmartRefreshLayout(): SmartRefreshLayout? {
        return if (isRebound()) smartRefreshLayout else null
    }

    override fun onResume() {
        super.onResume()
        isActive = true
    }

    override fun onPause() {
        super.onPause()
        isActive = false
    }

    override fun onDestroy() {
        super.onDestroy()
//        EventBus.getDefault().unregister(this)
    }

//    override fun setContentView(layoutResID: Int) {
//        super.setContentView(layoutResID)
//        setupViews()
//    }

    /**
     * 当Activity中的加载内容服务器返回失败，通过此方法显示提示界面给用户。
     *
     * @param tip
     * 界面中的提示信息
     */
    open fun showErrorView(tip: String) {
        pageState?.showErrorView(this, tip, View.OnClickListener {
            onRetry()
        })
    }

    /**
     * 显示状态页面重试
     */
    open fun onRetry() {
    }

    /**
     * 当Activity中的内容因为网络原因无法显示的时候，通过此方法显示提示界面给用户。
     *
     * @param listener
     * 重新加载点击事件回调
     */
    open fun showBadNetworkView(tip: String) {
        pageState?.showBadNetworkView(this, tip, View.OnClickListener {
            onRetry()
        })
    }

    /**
     * 当Activity中没有任何内容的时候，通过此方法显示提示界面给用户。
     * @param tip
     * 界面中的提示信息
     */
    open fun showNoContentView(tip: String?) {
        pageState?.showNoContentView(this, tip, View.OnClickListener {
            onRetry()
        })
    }

    /**
     * 将load error view进行隐藏。
     */
    open fun hideLoadErrorView() {
        pageState?.hideLoadErrorView()
    }

    /**
     * 将no content view进行隐藏。
     */
    open fun hideNoContentView() {
        pageState?.hideNoContentView()
    }

    /**
     * 将bad network view进行隐藏。
     */
    open fun hideBadNetworkView() {
        pageState?.hideBadNetworkView()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    fun startLoading() {
//        loading?.visibility = View.VISIBLE
//        hideBadNetworkView()
//        hideNoContentView()
//        hideLoadErrorView()
//    }
//
//    fun loadFinished() {
//        loading?.visibility = View.GONE
//    }
//
//    fun loadFailed(msg: String?) {
//        loading?.visibility = View.GONE
//    }


    override fun <T> bindToLife(): LifecycleTransformer<T> {
        return bindUntilEvent<T>(ActivityEvent.DESTROY)
    }

    override fun showLoading() {


    }

    override fun dismissLoading() {


    }

    override fun showError(msg: String) {


    }
}
