package no.nordicsemi.android.nrfmesh

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dagger.hilt.android.AndroidEntryPoint
import no.nordicsemi.android.material.you.NordicActivity
import no.nordicsemi.android.material.you.NordicTheme

@AndroidEntryPoint
class MainActivity : NordicActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainNavigation()
        }
    }
}

@Composable
fun MainNavigation() {
    NordicTheme {
        MainScreen()
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MainNavigation()
}