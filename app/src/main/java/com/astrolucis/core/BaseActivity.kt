package com.astrolucis.core

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v4.app.FragmentManager
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.astrolucis.utils.dialogs.AlertDialog

open class BaseActivity : AppCompatActivity() {

    companion object {
        private const val BACK_STACK = "backStack"
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
        if (!isBackStackEmpty()) {
            val backStackEntry = supportFragmentManager.backStackEntryCount
            for (i in 0 until backStackEntry) {
                supportFragmentManager.popBackStackImmediate()
            }
        }
        val tag = System.currentTimeMillis().toString()
        supportFragmentManager
                .beginTransaction()
                .addToBackStack(BACK_STACK)
                .add(getMasterContainerId(), fragment, tag)
                .addToBackStack(tag)
                .commit()
    }

    fun pushFragment(fragment: BaseFragment) {
        val tag = System.currentTimeMillis().toString()
        supportFragmentManager
                .beginTransaction()
                .addToBackStack(BACK_STACK)
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

    fun popAllFragments() {
        supportFragmentManager
                .popBackStackImmediate(BACK_STACK, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun showAlertDialog(id: String, data: AlertDialog.Data<*>, @StringRes title: Int, @StringRes message: Int) {
        showAlertDialog(id, data, resources.getText(title), resources.getText(message))
    }

    fun showAlertDialog(id: String, data: AlertDialog.Data<*>, title: CharSequence, message: CharSequence) {
        runOnUiThread({
            AlertDialog.newInstance(id, data, title, message,
                    resources.getString(android.R.string.ok),
                    resources.getString(android.R.string.cancel))
                    .show(supportFragmentManager, id)
        })
    }

    fun showSnackBar(view: View, @StringRes title: Int) {
        runOnUiThread({
            Snackbar.make(view, title, Snackbar.LENGTH_LONG).show()
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun openURL(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(browserIntent)
    }
}