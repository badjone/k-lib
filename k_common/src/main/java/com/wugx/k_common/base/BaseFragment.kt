package com.wugx.k_utils.base

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.NonNull
import androidx.appcompat.R.attr.title
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.trello.rxlifecycle3.LifecycleTransformer
import com.trello.rxlifecycle3.android.FragmentEvent
import com.trello.rxlifecycle3.components.support.RxFragment
import com.wugx.k_common.R
import com.wugx.k_common.base.KBaseActivity
import com.wugx.k_common.util.utilcode.util.Utils


/**
 * fragment基类
 *
 * @author wugx
 * @date 2018/1/24.
 */

abstract class BaseFragment : RxFragment(), IBaseView {
    lateinit var baseActivity: KBaseActivity
    private lateinit var tvTitle: TextView

    open fun isShowTitle(): Boolean = false
    open fun isRebound(): Boolean = true

    private lateinit var smartRefreshLayout: SmartRefreshLayout
    private lateinit var layoutView: View
    private lateinit var layoutTitle: View
    private lateinit var mToolBar: Toolbar
    private lateinit var actionbar: ActionBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //        baseActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        layoutView = setView(inflater)
        return layoutView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData(layoutView)
        loadData()
        setListener()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            loadData()
            setListener()
        }
    }

    @NonNull
    private fun setView(inflater: LayoutInflater): LinearLayout {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT, 1f
        )
        val linearLayout = LinearLayout(baseActivity)
        linearLayout.orientation = LinearLayout.VERTICAL
        linearLayout.layoutParams = params

        if (isShowTitle()) {
            layoutTitle = View.inflate(baseActivity, R.layout.layout_title, null)
            val p = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            initActionBar()
            linearLayout.addView(layoutTitle, p)
        }

        var view = inflater.inflate(layoutId(), null)
        view?.let {
            it.layoutParams = params
            it.setBackgroundResource(R.color.gray_f2f2f2)
            if (isRebound()) {
                smartRefreshLayout = SmartRefreshLayout(baseActivity)
                smartRefreshLayout.setBackgroundColor(ContextCompat.getColor(Utils.getApp(), R.color.gray_f2f2f2))
                smartRefreshLayout.layoutParams = params
                //            setSmartRefreshLayoutCommon(smartRefreshLayout);
                smartRefreshLayout.addView(view, params)
                linearLayout.addView(smartRefreshLayout, params)
            } else {
                linearLayout.addView(view, params)
            }
        }
        return linearLayout
    }

    private fun initActionBar() {
        mToolBar = layoutTitle.findViewById(R.id.toolbar_layout) as Toolbar
        tvTitle = layoutTitle.findViewById(R.id.tv_title) as TextView

        baseActivity.setSupportActionBar(mToolBar)
        actionbar = baseActivity.supportActionBar!!
        actionbar.setDisplayShowHomeEnabled(false)
        actionbar.setHomeAsUpIndicator(R.mipmap.ic_arrow_back)

        //set back icon
//            actionbar!!.setDisplayHomeAsUpEnabled(true)

        mToolBar.title = ""
        val defaultTitle = title.toString()
        if (TextUtils.isEmpty(defaultTitle)) {
            tvTitle.setText(R.string.app_name)
        } else {
            tvTitle.text = defaultTitle
        }
    }

    open fun setTitleBack() {
        if (isShowTitle()) actionbar.setDisplayHomeAsUpEnabled(true)
    }

    fun setTvTitle(txt: String) {
        if (TextUtils.isEmpty(txt)) return
        tvTitle.text = txt
    }

    fun setTvTitle(txtId: Int) {
        if (txtId == 0) return
        tvTitle.text = Utils.getApp().resources.getString(txtId)
    }

    fun getSmartRefreshLayout(): SmartRefreshLayout? {
        return if (isRebound()) smartRefreshLayout else null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        baseActivity = context as KBaseActivity
    }

    override fun onResume() {
        super.onResume()
    }

    fun findViewById(resId: Int): View {
        return layoutView.findViewById(resId)
    }

    @LayoutRes
    protected abstract fun layoutId(): Int

    /**
     * 网络请求等
     */
    protected abstract fun loadData()

    /**
     * UI
     */
    protected abstract fun initData(v: View)

    open fun setListener() {}

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun <T> bindToLife(): LifecycleTransformer<T> {
        return bindUntilEvent<T>(FragmentEvent.DESTROY_VIEW)
    }

    override fun showLoading() {

    }

    override fun dismissLoading() {

    }

    override fun showError(msg: String) {

    }


}
