package no.nordicsemi.android.nrfmesh

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.common.theme.NordicActivity
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.nrfmesh.ui.network.NetworkRoute

@AndroidEntryPoint
class MainActivity : NordicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NordicTheme {
                NetworkRoute()
            }
        }
    }
}