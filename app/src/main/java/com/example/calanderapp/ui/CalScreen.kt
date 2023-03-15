package com.example.calanderapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import androidx.navigation.fragment.findNavController
import com.example.calanderapp.R


class CalScreen : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cal_screen, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendar = view.findViewById<CalendarView>(R.id.calView)


        calendar.setOnDateChangeListener { _, year, month, day ->
            // month + 1 because index starts at 0 = jan
            // val msg = "Selected date is " + (month + 1) + "/" +  day + "/" + year
            val action = CalScreenDirections.actionCalScreenToReminderFrag(day, month, year)
            findNavController().navigate(action)
            // Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

        }


    }





}