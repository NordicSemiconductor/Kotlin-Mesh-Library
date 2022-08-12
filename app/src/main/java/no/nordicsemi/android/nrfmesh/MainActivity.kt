package no.nordicsemi.android.nrfmesh

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.common.theme.NordicActivity
import no.nordicsemi.android.common.theme.NordicTheme

@AndroidEntryPoint
class MainActivity : NordicActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NordicTheme {
                NetworkRoute()
            }
        }
    }
}

@Composable
fun MainNavigation() {
    NordicTheme {
        NetworkRoute()
    }
}

@Preview
@Composable
fun DefaultPreview() {
    NordicTheme {
        NetworkRoute()
    }
}