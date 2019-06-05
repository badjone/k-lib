package com.wugx.k_utils.base

/**
 *
 *
 *@author Wugx
 *@date   2018/12/19
 */
abstract class BasePresenter<V : IBaseView> : IPresenter<V> {

    var mRootView: V? = null
        private set


    override fun attachView(mRootView: V) {
        this.mRootView = mRootView
    }

    override fun detachView() {
        mRootView = null
    }

    private val isViewAttached: Boolean
        get() = mRootView != null

    open fun checkViewAttached() {
        if (!isViewAttached) throw MvpViewNotAttachedException()
    }

    private class MvpViewNotAttachedException internal constructor() :
        RuntimeException("Please call IPresenter.attachView(IBaseView) before" + " requesting data to the IPresenter")


}