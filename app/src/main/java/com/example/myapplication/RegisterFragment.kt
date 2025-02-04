package com.example.myapplication

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.FragmentRegisterBinding
import com.example.myapplication.services.yourbackend.YourBackendHttpClientFactory
import com.example.myapplication.services.yourbackend.config.DemoPasswordlessOptions
import com.example.myapplication.services.yourbackend.contracts.UserRegisterRequest
import com.google.android.gms.fido.Fido
import dev.passwordless.android.PasswordlessClient
import dev.passwordless.android.rest.PasswordlessOptions
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null

    private lateinit var _passwordless: PasswordlessClient
    private lateinit var _registrationIntentLauncher: ActivityResultLauncher<IntentSenderRequest>

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val fido2ApiClient = Fido.getFido2ApiClient(this.requireContext().applicationContext)

        val options = PasswordlessOptions(
            DemoPasswordlessOptions.API_KEY,
            DemoPasswordlessOptions.RP_ID,
            DemoPasswordlessOptions.ORIGIN,
            DemoPasswordlessOptions.API_URL)

        _passwordless = PasswordlessClient(fido2ApiClient, options,this.activity)

        _registrationIntentLauncher = registerForActivityResult(
            ActivityResultContracts.StartIntentSenderForResult()
        ) { result ->
            // This callback is invoked when the activity is finished
            if (result.resultCode == Activity.RESULT_OK) {
                // Call the suspend function in a coroutine
                lifecycleScope.launch {
                    _passwordless::handleRegistration
                }
            } else {
                // Handle the result accordingly if needed
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonRegister.setOnClickListener {
            lifecycleScope.launch {
                val httpClient = YourBackendHttpClientFactory.create(DemoPasswordlessOptions.YOUR_BACKEND_URL)
                val alias = binding.aliasEditText.text.toString()
                val username = binding.usernameEditText.text.toString()
                val responseToken = httpClient.register(UserRegisterRequest(username,alias)).body()?.token!!
                _passwordless.register(responseToken,alias+username)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
