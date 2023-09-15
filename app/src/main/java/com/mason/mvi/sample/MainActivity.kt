package com.mason.mvi.sample

import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import com.mason.mvi.MviViewModel
import com.mason.mvi.base.BaseActivity
import com.mason.mvi.sample.databinding.ActivityMainBinding
import com.mason.util.toast.toast

class MainActivity : BaseActivity<MainViewModel, ActivityMainBinding>() {
    override val viewModel: MainViewModel by viewModels()

    override fun initView(defaultUntilEvent: Lifecycle.Event) {
        binding.apply {
            btnDialog.throttleFirst {
                showLoading()
            }
            btnToast.throttleFirst {
                toast(btnToast.text)
            }
        }
    }
}

class MainViewModel : MviViewModel() {

}
