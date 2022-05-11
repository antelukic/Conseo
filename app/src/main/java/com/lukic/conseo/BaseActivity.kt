package com.lukic.conseo

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import com.lukic.conseo.utils.CheckNetworkConnection

abstract class BaseActivity<VB: ViewDataBinding>: AppCompatActivity() {

    lateinit var binding: VB
    private var alertDialog: AlertDialog? = null

    abstract fun getLayout(): Int

    abstract fun setViews(): Int


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, getLayout())

        setViews()

        val checkNetworkConnection = CheckNetworkConnection(MyApplication.getInstance())
        checkNetworkConnection.observe(this){ isConnected ->
            if(!isConnected){
                showInternetDialog()
            } else {
                if(alertDialog?.isShowing == true)
                    alertDialog?.dismiss()
            }
        }
    }

    private fun showInternetDialog(){
        alertDialog = this.let {
            val builder = AlertDialog.Builder(this)
            builder.apply {
                setTitle("Network connection error")
                setMessage("Please turn on your internet data")
            }
            builder.show()
        }
        alertDialog?.show()
    }

}