package com.jiuntian.hydratedme.ui.pet

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.jiuntian.hydratedme.R
import com.jiuntian.hydratedme.controller.PetDataController
import com.jiuntian.hydratedme.databinding.FragmentPetBinding
import com.jiuntian.hydratedme.model.PetData
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import com.bumptech.glide.load.DataSource
import com.google.firebase.firestore.ListenerRegistration
import com.jiuntian.hydratedme.controller.GmsController
import java.lang.Math.pow
import java.lang.Math.sqrt

import android.media.MediaPlayer
import com.jiuntian.hydratedme.model.PetType


class PetFragment : Fragment() {

    private lateinit var petViewModel: PetViewModel
    private lateinit var listener: ListenerRegistration
    private var _binding: FragmentPetBinding? = null

    private val binding get() = _binding!!
    private val randomAction =
        arrayOf("_dead", "_hurt", "_fall", "_jump", "_run", "_walk", "_slide")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        petViewModel =
            ViewModelProvider(this).get(PetViewModel::class.java)

        _binding = FragmentPetBinding.inflate(inflater, container, false)
        val root: View = binding.root

        lateinit var petType: String
        val petImage: ImageView = root.findViewById(R.id.pet_animation)
        val petLevel: TextView = root.findViewById(R.id.pet_level)
        val petHp: ProgressBar = root.findViewById(R.id.healthBar)
        val petExp: ProgressBar = root.findViewById(R.id.expBar)
        var hpValue = 0
        lateinit var initialStatus: String

        listener = PetDataController.firebaseUtils.firebaseUserDocument
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Pet", "Listen failed ${e.localizedMessage}", e)
                    return@addSnapshotListener
                }
                if (snapshot == null || !snapshot.exists()) {
                    PetDataController.createPetData(100, 0, PetType.CAT)
                }
                if (snapshot != null && snapshot.exists()) {
                    val record = snapshot.toObject(PetData::class.java)!!
                    petType = record.type.toString()

                    val getPetHpTask = PetDataController.GetPetHpTask()
                    getPetHpTask.setContext(requireContext())
                    hpValue = getPetHpTask.execute().get().toString().toInt()
                    PetDataController.updatePetHp(requireContext(), hpValue)
                    GmsController.submitExp(requireContext(), record.exp)

                    initialStatus = petType.lowercase()
                    val currentLv = ((sqrt((2 * record.exp + 25).toDouble()) + 5) / 10).toInt()
                    val xpForCurrentLv = 50 * currentLv * (currentLv - 1)
                    petHp.progress = hpValue
                    petExp.progress = (record.exp - xpForCurrentLv) / currentLv
                    petLevel.text = currentLv.toString()

                    if (hpValue > 50)
                        initialStatus += "_idle"
                    else if (hpValue > 0)
                        initialStatus += "_hurt_idle"
                    else
                        initialStatus += "_dead_idle"

                    Glide.with(this).load(
                        resources.getIdentifier(
                            initialStatus,
                            "drawable",
                            requireActivity().packageName
                        )
                    ).into(petImage)
                }
            }

        petImage.setOnClickListener {
            val petStatus = petStatus(hpValue)
            val mPlayer: MediaPlayer = MediaPlayer.create(
                context, resources.getIdentifier(
                    petStatus,
                    "raw",
                    requireActivity().packageName
                )
            ) // play audio
            mPlayer.start()
            playGif(petType, petImage, petStatus,initialStatus)
        }

        val leaderboardButton: ImageButton = _binding!!.leaderboardButton
        leaderboardButton.setOnClickListener {
            GmsController.showExpLeaderboard(requireActivity())
        }


        return root
    }

    fun petStatus(hpValue: Int): String {
        if (hpValue > 50)
            return randomAction[(2..6).random()]
        else if (hpValue > 0)
            return randomAction[1] // hurt
        else
            return randomAction[0] // dead
    }

    fun playGif(petType: String, petImage: ImageView, status: String, initialStatus: String) {
        Glide.with(this).asGif().load(
            resources.getIdentifier(
                petType.lowercase() + status,
                "drawable",
                requireActivity().packageName
            )
        ).listener(object : RequestListener<GifDrawable?> {

            override fun onResourceReady(
                resource: GifDrawable?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<GifDrawable?>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                resource?.setLoopCount(1)
                resource?.registerAnimationCallback(object :
                    Animatable2Compat.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable) {
                            Glide.with(requireContext()).load(
                                resources.getIdentifier(
                                    initialStatus,
                                    "drawable",
                                    requireActivity().packageName
                                )
                            ).into(petImage)
                    }
                })
                return false
            }

            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: com.bumptech.glide.request.target.Target<GifDrawable?>?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }
        }).into(petImage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        listener.remove()
    }
}