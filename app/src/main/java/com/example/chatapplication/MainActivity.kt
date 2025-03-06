package com.example.chatapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.example.chatapplication.ui.ChatActivity
import com.example.chatapplication.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var getResult: ActivityResultLauncher<Intent>
    private val STORAGE_REQUEST_CODE = 123123

    private lateinit var uri: Uri
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var usersRef: CollectionReference = db.collection("users")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        mBinding.signInButton.setOnClickListener {
            signIn()
        }

        mBinding.signUpButton.setOnClickListener {
            createAccount()
        }

        mBinding.textViewRegister.setOnClickListener {
            nextAnimation()
        }

        mBinding.textViewSignIn.setOnClickListener {
            previousAnimation()
        }

        mBinding.textViewGoToProfile.setOnClickListener {
            nextAnimation()
        }

        mBinding.textViewSignUp.setOnClickListener {
            previousAnimation()
        }

        mBinding.profileImage.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermission()
            } else {
                getImage()
            }
        }

        getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                mBinding.profileImage.setImageURI(it.data?.data)
            }
        }

    }

    private fun signIn() {
        val email = mBinding.signInInputEmail.editText?.text.toString().trim()
        val password = mBinding.signInInputPassword.editText?.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Sign in successful", Toast.LENGTH_LONG).show()
                    sendToAct()
                } else {
                    Toast.makeText(this, "Sign in failed", Toast.LENGTH_LONG).show()
                }
            }

    }

    private fun createAccount() {
        val email = mBinding.signUpInputEmail.text.toString().trim()
        val password = mBinding.signUpInputPassword.text.toString().trim()
        val confirmPassword = mBinding.signUpInputConfirmPassword.text.toString().trim()
        val userName = mBinding.signUpInputUsername.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_LONG).show()
            return
        }

        if (userName.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_LONG).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Passwords don't match", Toast.LENGTH_LONG).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_LONG).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_LONG).show()
                    sendToAct()
                } else {
                    Toast.makeText(this, "${task.exception}", Toast.LENGTH_LONG).show()
                }
            }

    }

    private fun nextAnimation() {
        mBinding.flipper.setInAnimation(this, android.R.anim.slide_in_left)
        mBinding.flipper.setOutAnimation(this, android.R.anim.slide_out_right)
        mBinding.flipper.showNext()
    }

    private fun previousAnimation() {
        mBinding.flipper.setInAnimation(this, R.anim.slide_in_right)
        mBinding.flipper.setOutAnimation(this, R.anim.slide_out_left)
        mBinding.flipper.showPrevious()
    }

    private fun getImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        getResult.launch(intent)
    }

    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this@MainActivity,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            AlertDialog.Builder(this@MainActivity)
                .setPositiveButton(R.string.dialog_button_yes) { _, _ ->
                    ActivityCompat.requestPermissions(
                        this@MainActivity,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                        STORAGE_REQUEST_CODE
                    )
                }.setNegativeButton(R.string.dialog_button_no) { dialog, _ ->
                    dialog.cancel()
                }.setTitle("Permission needed")
                .setMessage("Permission required to access internal storage").show()
        } else {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_REQUEST_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getImage()
        } else {
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show()
        }
    }

    private fun sendToAct() {
        startActivity(Intent(this@MainActivity, ChatActivity::class.java))
        finish()
    }

}