package no.nordicsemi.android.nrfmesh

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.common.theme.NordicActivity
import no.nordicsemi.android.common.theme.NordicTheme
import no.nordicsemi.android.nrfmesh.viewmodel.MainViewModel

@AndroidEntryPoint
class MainActivity : NordicActivity() {
    val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadNetwork()
        setContent {
            NordicTheme {
                if (viewModel.isNetworkLoaded)
                    NetworkScreen()
            }
        }
    }
}

@Composable
fun MainNavigation() {
    NordicTheme {
        NetworkScreen()
    }
}

@Preview
@Composable
fun DefaultPreview() {
    NordicTheme {
        NetworkScreen()
    }
}