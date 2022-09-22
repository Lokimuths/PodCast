package com.example.podcast.library.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T>(context: Context?) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    protected val inflater: LayoutInflater = LayoutInflater.from(context)
    protected val items: MutableList<T> = arrayListOf()

    override fun getItemViewType(position: Int): Int {
        throw IllegalStateException("Invalid type at pos=$position")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        throw IllegalStateException("Invalid HOLDER ! position=$position")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        throw IllegalStateException("Invalid HOLDER ! viewType=$viewType")
    }

    override fun getItemCount() = items.size

    fun contains(any: T): Boolean {
        return items.contains(any)
    }

    fun isEmpty(): Boolean = items.isEmpty()

    open fun getItem(index: Int): T = items[index]

    open fun getItemOrNull(index: Int): T? = items.getOrNull(index)

    open fun getListCopy(): List<T> = ArrayList(items)

    open fun addList(position: Int, list: List<T>?) {
        list?.let {
            items.addAll(position, it)
        }
    }

    fun setItems(list: List<T>) {
        items.addAll(list)
        notifyItemRangeInserted(list.size, items.size)
    }

    fun getAllItems(): MutableList<T> {
        return items
    }

    fun clear() {
        items.clear()
    }

    open fun removeItem(index: Int): T = items.removeAt(index)

    open fun removeItemsRange(from: Int, to: Int): List<T> {
        val result = ArrayList<T>()
        for (i in from..to) {
            val item = items.removeAt(i)
            if (item != null) {
                result.add(item)
            }
        }
        return result
    }

    open fun addItem(item: T) {
        items.add(item)
    }

    open fun addItem(position: Int, item: T) {
        items.add(position, item)
    }

}

fun <T> BaseAdapter<T>?.itemCountSafe() = this?.itemCount ?: 0
