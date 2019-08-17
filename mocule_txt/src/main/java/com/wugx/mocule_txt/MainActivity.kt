package com.wugx.mocule_txt

import android.os.Bundle
import com.wugx.k_common.base.KBaseActivity
import com.wugx.k_common.util.utilcode.util.ThreadUtils

class MainActivity : KBaseActivity() {
    override fun initCreate(savedInstanceState: Bundle?) {

        ThreadUtils.executeBySingle(object :ThreadUtils.Task<String>(){
            override fun doInBackground(): String? {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onSuccess(result: String?) {
            }

            override fun onCancel() {
            }

            override fun onFail(t: Throwable?) {
            }
        })
    }

    override fun getLayoutId(): Int = R.layout.txt_activity_main


}
