package com.wugx.k_common.base

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.appcompat.widget.Toolbar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.trello.rxlifecycle3.LifecycleTransformer
import com.trello.rxlifecycle3.android.ActivityEvent
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity
import com.wugx.k_common.R
import com.wugx.k_common.manager.PageStateManager
import com.wugx.k_common.util.utilcode.util.ActivityUtils
import com.wugx.k_utils.base.IBaseView
import java.lang.ref.WeakReference

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

    val handler: Handler = MyHandler(this@KBaseActivity)
    private var internalReceiver: BroadcastReceiver? = null
    /**
     * 是否显示toolbar
     */
    open fun isShowTitle(): Boolean = false

    /**
     * 是否使用页面回弹效果
     */
    open fun isRebound(): Boolean = true

    open fun isUseBaseLayout(): Boolean = false

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

        val parentLayout = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT, 1f
        )
        setContentView(layout, parentLayout)
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


    protected fun registerReceiver(actionArray: Array<String>?) {
        if (actionArray == null) {
            return
        }
        val _itFilter = IntentFilter()
        for (action in actionArray) {
            _itFilter.addAction(action)
        }

        if (internalReceiver == null) {
            internalReceiver = InternalReceiver()
        }
        LocalBroadcastManager.getInstance(application).registerReceiver(internalReceiver!!, _itFilter)
    }

    protected fun sendBroadcast(action: String) {
        LocalBroadcastManager.getInstance(application).sendBroadcast(Intent(action))
    }

    override fun sendBroadcast(intent: Intent) {
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    /**
     * 用handler时子类可重写该方法
     */
    protected fun handlerMessage(msg: Message) {

    }

    /**
     * 如果子界面需要拦截处理注册的广播
     * 需要实现该方法
     *
     * @param context
     * @param intent
     */
    protected fun handleReceiver(context: Context, intent: Intent) {
        // 广播处理
        if (intent == null) {
            return
        }
    }

    private inner class InternalReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            if (intent == null || intent.action == null) {
                return
            }
            handleReceiver(context, intent)
        }
    }

    companion object {

        private class MyHandler(ka: KBaseActivity) : Handler() {
            private var mWf: WeakReference<KBaseActivity> = WeakReference(ka)

            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val at = this.mWf.get()
                at?.let {
                    it.handlerMessage(msg)
                }
            }
        }
    }

    fun startActivity(bundle: Bundle?, clz: Class<out Activity>) {
        if (bundle == null) {
            ActivityUtils.startActivity(this@KBaseActivity, clz, R.anim.slide_right_in, R.anim.slide_left_out)
        } else {
            ActivityUtils.startActivity(
                bundle,
                this@KBaseActivity,
                clz,
                R.anim.slide_right_in,
                R.anim.slide_left_out
            )
        }
    }
}
