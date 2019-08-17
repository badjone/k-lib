package com.wugx.klibdemo

import android.os.Bundle
import com.wugx.k_common.base.KBaseBarActivity

/**
 *
 * @Author wugx
 * @Date 2019/8/16
 */
class OtherActivity : KBaseBarActivity() {
    override fun initCreate(savedInstanceState: Bundle?) {}

    override fun getLayoutId(): Int = R.layout.activity_other
}