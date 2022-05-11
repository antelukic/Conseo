package com.lukic.conseo.loginregister.ui

import com.lukic.conseo.BaseActivity
import com.lukic.conseo.R
import com.lukic.conseo.databinding.ActivityLoginRegisterBinding

class LoginRegisterActivity : BaseActivity<ActivityLoginRegisterBinding>() {

    override fun getLayout(): Int = R.layout.activity_login_register

    override fun setViews() {
        binding.model = this
        binding.lifecycleOwner = this
    }
}