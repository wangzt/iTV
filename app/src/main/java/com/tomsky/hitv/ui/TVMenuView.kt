package com.tomsky.hitv.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tomsky.hitv.data.TVMenuAction
import com.tomsky.hitv.data.TVMenuItem
import com.tomsky.hitv.databinding.TvMenuBinding
import com.tomsky.hitv.databinding.TvMenuItemBinding
import com.tomsky.hitv.util.SP

class TVMenuView (context: Context, attrs: AttributeSet?): RelativeLayout(context, attrs) {

    private val binding: TvMenuBinding = TvMenuBinding.inflate(LayoutInflater.from(context as Activity), this)

    fun initView(listener: TVMenuSelectListener) {
        val items = ArrayList<TVMenuItem>()
        items.add(TVMenuItem("更新数据", TVMenuAction.UPDATE_DATA.ordinal))
        items.add(TVMenuItem("关闭", TVMenuAction.CLOSE.ordinal))
        binding.tvMenuRecyclerview.run {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
            isFocusable = true
            isFocusableInTouchMode = true
            adapter = TVMenuAdapter(items, listener)
        }
    }

    fun show(show: Boolean) {
        if (show) {
            val version = SP.tvVersion
            binding.tvVersion.text = "当前版本: ${version}"
        } else {
            binding.tvMenuRecyclerview.clearFocus()
        }
        visibility = if (show) View.VISIBLE else View.GONE
    }

    inner class TVMenuAdapter(private val items: ArrayList<TVMenuItem>, private val listener: TVMenuSelectListener): RecyclerView.Adapter<TVMenuViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TVMenuViewHolder {
            val binding = TvMenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            binding.root.isFocusable = true
            binding.root.isFocusableInTouchMode = true
            binding.root.isClickable = true
            val viewHolder = TVMenuViewHolder(binding, listener)
            return viewHolder
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: TVMenuViewHolder, position: Int) {
            holder.update(items[position])
        }

    }

    inner class TVMenuViewHolder(val binding: TvMenuItemBinding, val listener: TVMenuSelectListener): RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("ClickableViewAccessibility")
        fun update(tvBean: TVMenuItem) {
//            Log.i("hitv-logo", "name:${tvBean.display}, logo:${tvBean.logo}")
            binding.tvMenuItemText.text = tvBean.display
            // 这个是为了避免在手机上需要点击两次的问题，获取焦点会消耗一次
            binding.root.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
//                    if (v.isFocused) {
                    v.performClick()
//                    }
                }
                false
            }
            binding.root.setOnClickListener {
                listener.onSelect(tvBean)
            }
            binding.root.setOnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when(keyCode) {
                        // ok 按键
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            listener.onSelect(tvBean)
                            true
                        }
                        else -> false
                    }
                }
                false
            }
        }
    }

}

interface TVMenuSelectListener {
    fun onSelect(tvBean: TVMenuItem)
}