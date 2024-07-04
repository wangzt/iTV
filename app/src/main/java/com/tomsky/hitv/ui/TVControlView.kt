package com.tomsky.hitv.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tomsky.hitv.data.TVBean
import com.tomsky.hitv.data.TVCategoryBean
import com.tomsky.hitv.databinding.TvItemBinding

class TVControlView(context: Context, attrs: AttributeSet?): LinearLayout(context, attrs) {

    private val titleViews = ArrayList<TextView>()
    private val recyclerViews = ArrayList<RecyclerView>()
    private val adapters = ArrayList<TVAdapter>()

    fun update(list: List<TVCategoryBean>, listener: TVSelectListener) {
        titleViews.clear()
        recyclerViews.clear()
        adapters.clear()
        list.forEachIndexed { index, tvCategoryBean ->
            addTVCategory(index, tvCategoryBean, listener)
        }
    }

    fun clearSelect() {
        adapters.forEachIndexed { _, tvAdapter -> tvAdapter.clearSelect() }
    }

    fun scrollToSelect(scrollView: ScrollView, cateIndex: Int, chanelIndex: Int) {
        val tittleView = titleViews[cateIndex]
        val recyclerView = recyclerViews[cateIndex]
        val adapter = adapters[cateIndex]
        scrollView.scrollTo(0, tittleView.top)
        recyclerView.scrollToPosition(chanelIndex)
        adapter.updateSelect(chanelIndex)
    }

    private fun addTVCategory(index: Int, category: TVCategoryBean, listener: TVSelectListener) {
        val recyclerView = RecyclerView(context)
        val titleView = TextView(context)
        titleView.setTextColor(Color.WHITE)
        titleView.textSize = 16f
        titleView.text = category.group
        addView(titleView)
        addView(recyclerView, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT))
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        recyclerView.addItemDecoration(TVItemDecoration(32))
        val adapter = TVAdapter(index, category.tvList, listener)
        recyclerView.adapter = adapter

        recyclerView.descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        recyclerView.isFocusable = true
        recyclerView.isFocusableInTouchMode = true

        titleViews.add(titleView)
        recyclerViews.add(recyclerView)
        adapters.add(adapter)
    }

    inner class TVAdapter(private val cateIndex: Int, private val items: ArrayList<TVBean>, private val listener: TVSelectListener): RecyclerView.Adapter<TVViewHolder>() {

        private var selectIndex = -1
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TVViewHolder {
            val binding = TvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val viewHolder = TVViewHolder(binding, listener)
            return viewHolder
        }

        override fun getItemCount(): Int {
            return items.size
        }

        override fun onBindViewHolder(holder: TVViewHolder, position: Int) {
            holder.update(cateIndex, position, selectIndex==position, items[position])
        }

        fun updateSelect(chanelIndex: Int) {
            selectIndex = chanelIndex
            notifyDataSetChanged()
        }

        fun clearSelect() {
            selectIndex = -1
            notifyDataSetChanged()
        }
    }

    inner class TVViewHolder(val binding: TvItemBinding, val listener: TVSelectListener): RecyclerView.ViewHolder(binding.root) {

        fun update(cateIndex: Int, chanelIndex:Int, select: Boolean, tvBean: TVBean) {
            Log.i("hitv-logo", "name:${tvBean.display}, logo:${tvBean.logo}")
            Glide.with(context).load(tvBean.logo).into(binding.tvLogo)
            binding.root.isSelected = select
            binding.tvName.text = tvBean.display
            binding.root.setOnClickListener {
                listener.onSelect(cateIndex, chanelIndex, tvBean)
            }
            binding.root.setOnKeyListener { v, keyCode, event ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when(keyCode) {
                        // ok 按键
                        KeyEvent.KEYCODE_ENTER,
                        KeyEvent.KEYCODE_DPAD_CENTER -> {
                            listener.onSelect(cateIndex, chanelIndex, tvBean)
                            true
                        }
                        else -> false
                    }
                }
                false
            }
        }
    }

    inner class TVItemDecoration(private val space:Int): RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.left = space
            outRect.bottom = space
        }
    }
}

interface TVSelectListener {
    fun onSelect(cateIndex: Int, chanelIndex: Int, tvBean: TVBean)
}