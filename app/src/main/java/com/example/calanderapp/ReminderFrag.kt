package com.example.calanderapp

import android.app.*
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.calanderapp.`interface`.OnItemClickListener
import com.example.calanderapp.data.Events
import com.example.calanderapp.databinding.FragmentReminderBinding
import com.example.calanderapp.ui.ReminderAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*




class ReminderFrag : Fragment(), OnItemClickListener {




    private var _binding : FragmentReminderBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    var newList = mutableListOf<Events>()

    // reference to firebase database
    var fbDatabase = FirebaseFirestore.getInstance()


    // Calendar & Times objects
    private val args: ReminderFragArgs by navArgs()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotiChannel()
        getReminders()
    }


    // Notification Channel
    private fun createNotiChannel() {
        val name = "Noti CHannel"
        val desc = " A Description of the Channel"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(channelID, name, importance)
        channel.description = desc
        val notificationMan = requireContext().getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationMan.createNotificationChannel(channel)

    }


    // fetches and displays specific events from firebase into recyclerview
    private fun getReminders() {

        val dayNum = args.day
        val monthNum = args.month


        fbDatabase.collection("users")
            .whereEqualTo("day", dayNum)
            .whereEqualTo("month", monthNum)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "Document ID:${document.id} => Reminder:${document.data.get("reminder")}")
                    var eventss = Events().apply {
                        reminder = document.data.get("reminder").toString()
                        hour = document.data.get("hour").toString().toInt()
                        min= document.data.get("min").toString().toInt()
                        day= document.data.get("day").toString().toInt()
                        month= document.data.get("month").toString().toInt()
                        year= document.data.get("year").toString().toInt()

                    }
                    newList += eventss
                }
                recyclerView.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }






    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       _binding = FragmentReminderBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = ReminderAdapter(newList, this)





        // Logic to add event to firebase cloud
        binding.btnClick.setOnClickListener {
            val editTxt: String = binding.editText.text.toString()
            val editHr: Int =  binding.timePicker.hour
            val editMin: Int = binding.timePicker.minute

            val day = args.day
            val month = args.month
            val year = args.year




            val user = Events(editTxt, editHr, editMin, day, month, year)
            val msg = " Added"
            val error = "Error"

            // Displays error when field is empty
            if (editTxt.isEmpty()) {
                binding.editText.error = "Enter Reminder Please!"
                return@setOnClickListener
            }




            fun saveReminder() {
                fbDatabase.collection("users")
                    .add(user)
                    .addOnSuccessListener {
                        Log.d(TAG, "Added")
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener{
                        Log.w(TAG, "Error")
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
            }
            saveReminder()
            scheduleNoti()

            binding.editText.text.clear()
            val action = ReminderFragDirections.actionReminderFragToCalScreen()
            findNavController().navigate(action)

        }





    }



    private fun scheduleNoti() {

        val title = binding.editText.text.toString()
        val intent = Intent(requireContext().applicationContext, Noti::class.java).apply {
            putExtra(eventExtra, title)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext().applicationContext,
            notificationID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        // get time function
        val time = getTime()
        // allows alarm to wakeup while app is closed
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingIntent
        )
        // show notification function
        showAlert(time, title)

    }

    // Dialog pop up when submitting event
    private fun showAlert(time: Long, title: String) {
        val date = Date(time)
        val dateFormat = android.text.format.DateFormat.getLongDateFormat(requireContext().applicationContext)
        val timeFormat = android.text.format.DateFormat.getTimeFormat(requireContext().applicationContext)
        // iffy
        AlertDialog.Builder(requireContext())
            .setTitle("Noti Set")
            .setMessage("Reminder: " + title +
                    "\nAt: " + dateFormat.format(date) + " " + timeFormat.format(date))
            .setPositiveButton("Okay"){_,_ ->}
            .show()
    }

    // returns calender format/instance
    private fun getTime(): Long {

        val day = args.day
        val month = args.month
        val year = args.year

        val min = binding.timePicker.minute
        val hr = binding.timePicker.hour

        val calendar =  Calendar.getInstance()
        calendar.set(year,month,day,hr,min)
        return calendar.timeInMillis

    }


    private fun showSnackBar(msg: String) {
        view?.let { Snackbar.make(it,msg,Snackbar.LENGTH_SHORT).show() }
    }

    // logic for clicking reminder(s)
    override fun onItemClicked(position: Int) {
        showDiag(position)
    }

    // logic for deleting documents via position/name
    private fun showDiag(position: Int) {

        // the position/reminder of the loaded list via the month/day you clicked
        val editTxt = newList[position].reminder





        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alert")
            .setMessage("Delete Reminder?")
            .setNeutralButton("Cancel") { _, _ ->
                showSnackBar("Canceled")
            }
            .setNegativeButton("No") { _, _ ->
                showSnackBar("Declined")
            }
            .setPositiveButton("Yes") { _, _ ->
                val action = ReminderFragDirections.actionReminderFragToCalScreen()
                findNavController().navigate(action)
                fbDatabase.collection("users").whereEqualTo("reminder", editTxt)
                    .get()
                    .addOnSuccessListener { results ->
                        for (document in results)
                            document.reference.delete().addOnFailureListener { e ->
                                Log.e(TAG, "deleteJobs: failed to delete document ${document.reference.id}", e)

                            }
                        Log.d(TAG, "DocumentSnapshot successfully deleted!")
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting documents.", exception)
                    }
                showSnackBar("Deleted!")
            }
            .show()
    }






}