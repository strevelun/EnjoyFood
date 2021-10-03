package com.project.enjoyfood.board

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.project.enjoyfood.R

class BoardListAdapter(val boardList : MutableList<BoardData>) : BaseAdapter() {

    override fun getCount(): Int {
        return boardList.size
    }

    override fun getItem(position: Int): Any {
        return boardList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var view =convertView

        if(view == null) {
            view = LayoutInflater.from(parent?.context).inflate(R.layout.board_list, parent,false)

        }
        val title = view?.findViewById<TextView>(R.id.titleText)
        val content = view?.findViewById<TextView>(R.id.contentText)
        val time = view?.findViewById<TextView>(R.id.timeText)

        title!!.text = boardList[position].title
        content!!.text = boardList[position].content
        time!!.text = boardList[position].time

        return view!!
    }
}