package com.astrolucis.core

import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.astrolucis.R

open class BaseActivity : AppCompatActivity() {

    companion object {
        private const val backStack = "backStack"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (savedInstanceState ?: this.intent.extras)?.let {
            parseState(it)
        }
    }

    protected open fun parseState(state: Bundle) {

    }

    protected open fun getMasterContainerId(): Int {
        return 0
    }

    fun startWithFragment(fragment: BaseFragment) {
        val tag = System.currentTimeMillis().toString()
        supportFragmentManager
                .beginTransaction()
                .add(getMasterContainerId(), fragment, tag)
                .addToBackStack(tag)
                .commit()
    }

    fun pushFragment(fragment: BaseFragment) {
        val tag = System.currentTimeMillis().toString()
        supportFragmentManager
                .beginTransaction()
                .replace(getMasterContainerId(), fragment, tag)
                .addToBackStack(tag)
                .commit()
    }

    fun popFragment() {
        supportFragmentManager.popBackStackImmediate()
    }

    fun isBackStackEmpty(): Boolean {
        return supportFragmentManager.backStackEntryCount == 0
    }

    fun isAtRootFragment(): Boolean {
        return supportFragmentManager.backStackEntryCount == 1
    }

    fun popToRootFragment() {
        val backStackId = supportFragmentManager.getBackStackEntryAt(0).id
        supportFragmentManager
                .popBackStackImmediate(backStackId, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun popAllFragment() {
        supportFragmentManager
                .popBackStackImmediate(backStack, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun showAlertDialog(@StringRes title: Int, @StringRes message: Int) {
        return showAlertDialog(resources.getText(title), resources.getText(message))
    }

    fun showAlertDialog(title: CharSequence, message: CharSequence) {
        runOnUiThread({
            val ad = AlertDialog.Builder(this, R.style.AppTheme_Dialog)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok) { dialog, which -> }
                    .create()
            ad.show()
        })
    }
}