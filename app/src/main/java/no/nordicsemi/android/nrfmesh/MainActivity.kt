package no.nordicsemi.android.nrfmesh

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import no.nordicsemi.android.material.you.NordicActivity
import no.nordicsemi.android.material.you.NordicTheme

class MainActivity : NordicActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NordicTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Greeting("Android")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DefaultPreview() {
    NordicTheme {
        Greeting("Android")
    }
}