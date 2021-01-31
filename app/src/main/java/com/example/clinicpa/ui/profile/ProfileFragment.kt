package com.example.clinicpa.ui.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.Navigation
import com.example.clinicpa.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.ByteArrayOutputStream


class ProfileFragment : Fragment() {

    companion object {
        const val REQUEST_CAMERA = 100
    }

    private lateinit var imageUri: Uri
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user != null) {
            if (user.photoUrl != null) {
                Picasso.get().load(user.photoUrl).into(ivProfile)
            } else {
                Picasso.get().load("https://picsum.photos/200").into(ivProfile)
            }

            etName.setText(user.displayName)
            etEmailProf.setText(user.email)
//            etPhone.setText(user.phoneNumber)

            if (user.isEmailVerified) {
                ic_verified.visibility = View.VISIBLE
            } else {
                ic_unverified.visibility = View.VISIBLE
            }
        }

        ivProfile.setOnClickListener {
            intentCamera()
        }

        btnUpdate.setOnClickListener {
            val image = when {
                ::imageUri.isInitialized -> imageUri
                user?.photoUrl == null -> Uri.parse("https://picsum.photos/200")
                else -> user.photoUrl
            }

            val name = etName.text.toString().trim()
//            val phone = etPhone.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Nama Harus Di Isi"
                etName.requestFocus()
                return@setOnClickListener
            }
//            if (phone.isEmpty()) {
//                etPhone.error = "Nomor Harus Di Isi"
//                etPhone.requestFocus()
//                return@setOnClickListener
//            } else {
//                etPhone.setText(user?.phoneNumber)
//            }
            UserProfileChangeRequest.Builder()
                .setDisplayName(name)
                .setPhotoUri(image)
                .build().also {
                    user?.updateProfile(it)?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(activity, "Profile Updated!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(activity, "${it.exception?.message}", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
        }

        ic_unverified.setOnClickListener {
            user?.sendEmailVerification()?.addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(activity, "Email Verifikasi Telah Dikirim", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(activity, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        etEmailProf.setOnClickListener {
            val actionUpdateEmail = ProfileFragmentDirections.actionUpdateEmail()
            Navigation.findNavController(it).navigate(actionUpdateEmail)
        }

        tvChangePassword.setOnClickListener {
            val actionChangePassword = ProfileFragmentDirections.actionChangePassword()
            Navigation.findNavController(it).navigate(actionChangePassword)
        }
    }

    private fun intentCamera() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { intent ->
            activity?.packageManager?.let {
                intent.resolveActivity(it).also {
                    startActivityForResult(intent, REQUEST_CAMERA)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {
            val imgBitmap = data?.extras?.get("data") as Bitmap
            uploadImage(imgBitmap)
        }
    }

    private fun uploadImage(imgBitmap: Bitmap) {
        val baos = ByteArrayOutputStream()
        val ref =
            FirebaseStorage.getInstance().reference.child("img/${FirebaseAuth.getInstance().currentUser?.uid}")

        imgBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val image = baos.toByteArray()

        ref.putBytes(image)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    ref.downloadUrl.addOnCompleteListener {
                        it.result?.let {
                            imageUri = it
                            ivProfile.setImageBitmap(imgBitmap)
                        }
                    }
                }
            }
    }
}