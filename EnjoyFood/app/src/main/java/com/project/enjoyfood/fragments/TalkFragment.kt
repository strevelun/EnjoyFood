package com.project.enjoyfood.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.project.enjoyfood.R
import com.project.enjoyfood.board.BoardData
import com.project.enjoyfood.board.BoardInActivity
import com.project.enjoyfood.board.BoardListAdapter
import com.project.enjoyfood.board.BoardWriteActivity
import com.project.enjoyfood.databinding.FragmentTalkBinding
import com.project.enjoyfood.firebase.Ref

class TalkFragment : Fragment() {

    private lateinit var binding : FragmentTalkBinding

    private val boardList = mutableListOf<BoardData>()
    private val boardKeyList = mutableListOf<String>()

    private val TAG = TalkFragment::class.java.simpleName

    private lateinit var boardListAdapter : BoardListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_talk, container, false)

        boardListAdapter = BoardListAdapter(boardList)
        binding.boardList.adapter = boardListAdapter

        binding.boardList.setOnItemClickListener { parent, view, position, id ->

            val intent = Intent(context, BoardInActivity::class.java)
            intent.putExtra("key", boardKeyList[position])
            startActivity(intent)
        }
        binding.writBtn.setOnClickListener {
            val intent = Intent(context, BoardWriteActivity::class.java)
            startActivity(intent)
        }
        getBoardData()
        // Inflate the layout for this fragment
        return binding.root
    }

    private fun getBoardData() {
        val postListener = object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {

                boardList.clear()

                for(dataModel in dataSnapshot.children) {
                    Log.d(TAG, dataModel.toString())

                    val item = dataModel.getValue(BoardData::class.java)
                    boardList.add(item!!)
                    boardKeyList.add(dataModel.key.toString())

                }
                boardKeyList.reverse()
                boardList.reverse()
                boardListAdapter.notifyDataSetChanged()
                Log.d(TAG,boardList.toString())
            }

            override fun onCancelled(error: DatabaseError) {

                Log.d(TAG,"ladPost:onCancelled",error.toException())

            }
        }
        Ref.boardRef.addValueEventListener(postListener)
    }
}