package com.tokyoc.line_client

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class MemberFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.activity_member, container, false)

    companion object {
        fun newInstance(): MemberFragment = MemberFragment()
    }
}