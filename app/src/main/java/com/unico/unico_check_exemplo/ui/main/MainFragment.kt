package com.unico.unico_check_exemplo.ui.main

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.acesso.acessobio_android.AcessoBioListener
import com.acesso.acessobio_android.iAcessoBioDocument
import com.acesso.acessobio_android.iAcessoBioSelfie
import com.acesso.acessobio_android.onboarding.AcessoBio
import com.acesso.acessobio_android.onboarding.IAcessoBioBuilder
import com.acesso.acessobio_android.onboarding.camera.UnicoCheckCameraOpener.Document
import com.acesso.acessobio_android.onboarding.camera.UnicoCheckCameraOpener.Selfie
import com.acesso.acessobio_android.onboarding.camera.document.DocumentCameraListener
import com.acesso.acessobio_android.onboarding.camera.selfie.SelfieCameraListener
import com.acesso.acessobio_android.onboarding.types.DocumentType
import com.acesso.acessobio_android.services.dto.ErrorBio
import com.acesso.acessobio_android.services.dto.ResultCamera
import com.unico.unico_check_exemplo.R
import com.unico.unico_check_exemplo.UnicoTheme
import com.unico.unico_check_exemplo.databinding.MainFragmentBinding
import com.unico.unico_check_exemplo.utils.showToast

class MainFragment: Fragment(), AcessoBioListener, iAcessoBioSelfie, iAcessoBioDocument {

    companion object {
        fun newInstance() = MainFragment()
    }

    private val REQUEST_CAMERA_PERMISSION = 1
    private lateinit var unicoCheckBuilder: IAcessoBioBuilder
    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!
    private var cameraOpenerSelfie: Selfie? = null
    private var cameraOpenerDocument: Document? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)

        if(hasPermission()){

            initUnicoCheck()
            enableButtonsSelfie()
            enableButtonsDocument()
            initListeners()

        }else{
            hasPermission()
        }

        return binding.root
    }


    private fun hasPermission(): Boolean {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(
                    Manifest.permission.CAMERA
            ), this@MainFragment.REQUEST_CAMERA_PERMISSION)
            return false
        }
        return true
    }

    private fun initUnicoCheck(){
        unicoCheckBuilder = AcessoBio(context, this)
            .setTimeoutSession(40.0)
            .setTimeoutToFaceInference(40.0)
            .setTheme(UnicoTheme())
    }


    private fun enableButtonsSelfie(){
        unicoCheckBuilder.build().prepareSelfieCamera(object : SelfieCameraListener {
            override fun onCameraReady(cameraOpener: Selfie) {
                this@MainFragment.cameraOpenerSelfie = cameraOpener
            }

            override fun onCameraFailed(message: String) {
                showToast(message, context!!)
                binding.results.text = message
            }
        })
    }

    private fun enableButtonsDocument(){
        unicoCheckBuilder.build().prepareDocumentCamera(object : DocumentCameraListener {
            override fun onCameraReady(cameraOpener: Document) {
                this@MainFragment.cameraOpenerDocument = cameraOpener
            }

            override fun onCameraFailed(message: String) {
                showToast(message, context!!)
                binding.results.text = message
            }
        })
    }

    private fun initListeners(){

        binding.btFramebp.setOnClickListener {
            setCameraSmart()
            cameraOpenerSelfie?.open(this@MainFragment)
        }

        binding.btFramebpNormal.setOnClickListener {
            setCameraNormal()
            cameraOpenerSelfie?.open(this@MainFragment)
        }

        binding.btFramebpWithButton.setOnClickListener {
            setCameraWithButton()
            cameraOpenerSelfie?.open(this@MainFragment)
        }

        binding.btFrameDocument.setOnClickListener {
            selectDocument()
        }

        binding.arrowReturn.setOnClickListener(View.OnClickListener {
            binding.functions.visibility = View.VISIBLE
            binding.preview.visibility = View.GONE
        })
    }

    private fun selectDocument() {
        val documentos = arrayOf(
            DocumentType.CNH.toString(),
            DocumentType.RG_FRENTE.toString(),
            DocumentType.RG_VERSO.toString()
        )

        AlertDialog.Builder(context)
            .setTitle(resources.getString(R.string.select_document))
            .setSingleChoiceItems(
                    documentos, -1
            ) { dialog, item ->
                    if (item == 0) {
                        cameraOpenerDocument?.open(DocumentType.CNH, this@MainFragment)
                    } else if (item == 1) {
                        cameraOpenerDocument?.open(DocumentType.RG_FRENTE, this@MainFragment)
                    } else {
                        cameraOpenerDocument?.open(DocumentType.RG_VERSO, this@MainFragment)
                    }
                dialog.dismiss()
            }.create().show()
    }

    private fun showImgPreview(base64: String) {
        if (binding.seeimg.isChecked) {
            binding.functions.visibility = View.GONE
            binding.preview.visibility = View.VISIBLE
            val decodedString = Base64.decode(base64, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            binding.imgpreview.setImageBitmap(decodedByte)
        }
    }

    private fun setCameraNormal() {
        unicoCheckBuilder.setAutoCapture(false)
        unicoCheckBuilder.setSmartFrame(false)
    }

    private fun setCameraSmart() {
        unicoCheckBuilder.setAutoCapture(true)
        unicoCheckBuilder.setSmartFrame(true)
    }

    private fun setCameraWithButton() {
        unicoCheckBuilder.setAutoCapture(false)
        unicoCheckBuilder.setSmartFrame(true)
    }

    override fun onSuccessSelfie(result: ResultCamera) {
        Log.d("Response", "onSuccessCamera")
        showImgPreview(result.base64)
    }

    override fun onSuccessDocument(base64: String?) {
        Log.d("Response", "onSuccesstDocument")
        binding.results.setText("Base64 do documento gerado no log com sucesso.")
        showImgPreview(base64!!)
    }

    override fun onErrorSelfie(errorBio: ErrorBio) {
        Log.d("Response", "onErrorCamera")
        binding.results.setText(errorBio.description)
    }

    override fun onErrorDocument(error: String?) {
        Log.d("Response", "onErrorDocument")
        binding.results.setText(error)
    }

    override fun onErrorAcessoBio(errorBio: ErrorBio) {
        Log.d("Response", "onErrorAcessoBio")
        binding.results.setText(errorBio.description)
    }

    override fun onUserClosedCameraManually() {
        Log.d("Response", "userClosedCameraManually")
        binding.results.setText("Usuario fechou camera manualmente")
    }

    override fun onSystemClosedCameraTimeoutSession() {
        Log.d("Response", "systemClosedCameraTimeoutSession")
        binding.results.setText("Sistema fechou a camera por timeout de sessão")
    }

    override fun onSystemChangedTypeCameraTimeoutFaceInference() {
        Log.d("Response", "systemChangedTypeCameraTimeoutFaceInference")
        binding.results.setText("Sistema disparou o timeout de inferencia de face")
    }
}