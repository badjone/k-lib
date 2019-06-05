package com.wugx.k_utils.base

import com.trello.rxlifecycle3.LifecycleTransformer

interface IBaseView {

    fun showLoading()

    fun dismissLoading()

    fun showError(msg:String)


    /**
     * RxLifecycle
     */
    fun <T> bindToLife(): LifecycleTransformer<T>
}
