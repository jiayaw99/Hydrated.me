package com.jiuntian.hydratedme.ui.water

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import com.jiuntian.hydratedme.R
import com.jiuntian.hydratedme.controller.DrinkRecordsController
import com.jiuntian.hydratedme.databinding.FragmentWaterBinding
import com.jiuntian.hydratedme.model.DrinkRecord
import com.jiuntian.hydratedme.model.DrinkType
import com.jiuntian.hydratedme.util.Util
import java.util.*
import com.jiuntian.hydratedme.Strings
import com.jiuntian.hydratedme.controller.PetDataController
import com.jiuntian.hydratedme.controller.ScheduleController
import com.jiuntian.hydratedme.util.FirebaseUtils
import java.text.SimpleDateFormat


class WaterFragment : Fragment() {

    private lateinit var waterViewModel: WaterViewModel
    private var _binding: FragmentWaterBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val firebaseUtils = FirebaseUtils()

    private lateinit var hints: Array<String>
    private lateinit var adapter: FirestoreRecyclerAdapter<DrinkRecord, DrinkRecordViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hints = resources.getStringArray(R.array.water_hints)

        val now = Date()
        val today = Date(now.year, now.month, now.date)
        val tomorrow = Date(now.year, now.month, now.date+1)
        val query : Query = firebaseUtils.firebaseWaterIntakeRecords
            .whereGreaterThanOrEqualTo("time", today)
            .whereLessThan("time", tomorrow)
            .orderBy("time")

        val options: FirestoreRecyclerOptions<DrinkRecord> = FirestoreRecyclerOptions.Builder<DrinkRecord>()
            .setQuery(query, DrinkRecord::class.java)
            .build()

        adapter = object : FirestoreRecyclerAdapter<DrinkRecord, DrinkRecordViewHolder>(options) {
            override fun onBindViewHolder(
                holder: DrinkRecordViewHolder,
                position: Int,
                drinkRecord: DrinkRecord
            ) {
                val iconImageView = holder.iconImageView
                val timeTextView = holder.timeTextView
                val descriptionTextView = holder.descriptionTextView
                val volumeTextView = holder.volumeTextView
                val deleteButton = holder.deleteButton

                // better move is to move this to DrinkRecord enum
                val iconImageViewImageResource : Int = when(drinkRecord.type) {
                    DrinkType.WATER -> R.drawable.ic_water_bin
                    DrinkType.COFFEE -> R.drawable.ic_coffee
                    DrinkType.TEA -> R.drawable.ic_tea
                    DrinkType.SOFT_DRINK -> R.drawable.ic_soft_drink
                    DrinkType.ALCOHOL -> R.drawable.ic_alcohol
                    else -> R.drawable.ic_drinked_24
                }
                iconImageView.setImageResource(iconImageViewImageResource)
                timeTextView.text = dateFormat.format(drinkRecord.time)
                descriptionTextView.text = drinkRecord.type.printableName
                volumeTextView.text = Strings.get(R.string.volume, drinkRecord.volume)
                deleteButton.setOnClickListener {
                    query
                        .whereEqualTo("time", drinkRecord.time)
                        .whereEqualTo("volume", drinkRecord.volume)
                        .whereEqualTo("type", drinkRecord.type.toString())
                        .get().addOnSuccessListener {
                            it.documents.forEach { doc->
                                doc.reference.delete()
                                PetDataController.loseExp(doc.toObject(DrinkRecord::class.java)?.expGain ?: 0)
                            }
                        }
                }

            }

            override fun onCreateViewHolder(
                parent: ViewGroup, viewType: Int
            ): DrinkRecordViewHolder {
                val context = parent.context
                val inflater = LayoutInflater.from(context)
                val drinkRecordView = inflater.inflate(R.layout.item_drink_record, parent, false)
                return DrinkRecordViewHolder(drinkRecordView)
            }

            override fun onDataChanged() {
                super.onDataChanged()
                waterViewModel.totalVolume.apply {
                    value = getTotalVolume()
                }
                Log.v("firebase", "data changed. total: $itemCount ")
            }

            fun getTotalVolume() : Int {
                var sum = 0
                this.snapshots.forEach { drinkRecord ->
                    sum += drinkRecord.volume
                }
                return sum
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }

    override fun onResume() {
        super.onResume()
        val i = (hints.indices).random()
        val hintsTextView: TextView = requireView().findViewById(R.id.water_hint_text)
        hintsTextView.text = hints[i]

        val scheduleGoal = Util.getPreference(requireContext(), getString(R.string.schedule_goal), "2200")

        val animationView = requireView().findViewById<LottieAnimationView>(R.id.water_droplet)

        // Water intake goal
        val tvWaterIntakeGoal: TextView = _binding!!.waterIntakeGoalsText

        waterViewModel.totalVolume.observe(viewLifecycleOwner, {
            tvWaterIntakeGoal.text = getString(R.string.water_intake_target_text,
                it, scheduleGoal)

            val ratio = it.toDouble()/scheduleGoal.toDouble() //to be programmable
            val animator = ValueAnimator.ofFloat(0f, 1f)
            animationView.setMaxFrame((150.0*ratio).toInt())  // 181
            animator.duration = 400
            animator.start()
            animationView.resumeAnimation()
        })

        val nextIntakeAtTextView: TextView = _binding!!.nextIntakeAt
        nextIntakeAtTextView.text = getString(R.string.next_at_xx, ScheduleController.getNextIntakeTime(requireContext()))

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        waterViewModel = ViewModelProvider(this).get(WaterViewModel::class.java)

        _binding = FragmentWaterBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Recycle view
        val rvDrinkRecords: RecyclerView = _binding!!.drinkRecordsRecycleView
        rvDrinkRecords.adapter = adapter
        val linearLayoutManager = CustomLinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        rvDrinkRecords.layoutManager = linearLayoutManager
            // automatic scroll to top
        adapter.registerAdapterDataObserver(object: RecyclerView.AdapterDataObserver(){
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                linearLayoutManager.smoothScrollToPosition(rvDrinkRecords, null, adapter.itemCount)
            }
        })

        //Quick button
        val quickAddButton = _binding!!.quickAddButton
        quickAddButton.setOnClickListener { _ ->
            val cupSize : Int = Util.getPreference(requireContext(), requireContext().getString(R.string.cup_size))
            DrinkRecordsController.addDrinkRecord(Date(), cupSize, DrinkType.WATER, context)

            // just for debug
//            ScheduleController.createNotificationForWater(requireContext(), ReminderData(20,0))
//            ScheduleController.scheduleAlarmsForReminder(requireContext(),
//                ReminderData(Date().hours,Date().minutes+1))
        }
        val volumeOnQuickAddButton = _binding!!.volumeOnQuickAddButton
        val cupVolume : Int = Util.getPreference(requireContext(), requireContext().getString(R.string.cup_size))
        volumeOnQuickAddButton.text = cupVolume.toString()

        //Add record button
        val addRecordBottomSheetDialog = AddRecordBottomSheetDialog()
        val addRecordButton = _binding!!.addDrinkRecordButton
        addRecordButton.setOnClickListener {
            addRecordBottomSheetDialog.show(parentFragmentManager, "WATER")
        }

        return root
    }

    class DrinkRecordViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val iconImageView: ImageView = itemView.findViewById(R.id.drink_record_icon)
        val timeTextView: TextView = itemView.findViewById(R.id.drink_record_time)
        val descriptionTextView: TextView = itemView.findViewById(R.id.drink_record_description)
        val volumeTextView: TextView = itemView.findViewById(R.id.drink_record_volume)
        val deleteButton: ImageButton = itemView.findViewById(R.id.drink_record_delete)
    }

    class CustomLinearLayoutManager(context: Context?): LinearLayoutManager(context) {
        override fun onLayoutChildren(
            recycler: RecyclerView.Recycler?,
            state: RecyclerView.State?
        ) {
            try {
                super.onLayoutChildren(recycler, state)
            } catch (e: IndexOutOfBoundsException) {
                Log.w("DrinkRecord", "IOOBE", e)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}