package no.nordicsemi.android.nrfmesh.feature.groups.group.controls

import android.location.Address
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.isSupportedGroupItem
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.feature.groups.group.GroupInfoListData
import no.nordicsemi.kotlin.data.HexString
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import no.nordicsemi.kotlin.mesh.core.model.ModelId.Companion.decode

@HiltViewModel(assistedFactory = GroupControlsViewModel.Factory::class)
internal class GroupControlsViewModel @AssistedInject internal constructor(
    private val repository: CoreDataRepository,
    @Assisted key: String,
) : ViewModel() {
    private val vals = key.split(":")
    private val groupAddress = vals[0].toUShort(radix = 16)
    private val modelId = vals[1].decode()
    private var group: Group? = null
    private val _uiState = MutableStateFlow(GroupControlsScreenUiState())
    val uiState: StateFlow<GroupControlsScreenUiState> = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GroupControlsScreenUiState()
        )

    private lateinit var network: MeshNetwork

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                network.group(address = groupAddress)?.let { group ->
                    this@GroupControlsViewModel.group = group
                    val models = mutableMapOf<ModelId, List<Model>>()
                    network.nodes
                        .flatMap { it.elements }
                        .flatMap { it.models }
                        .filter { it.isSubscribedTo(group = group) }
                        .forEach { model ->
                            if (isSupportedGroupItem(model)) {
                                models[model.modelId] = (models[model.modelId]
                                    ?.plus(model))
                                    ?: listOf(model)
                            }
                        }
                    val state = _uiState.value.copy(
                        groupState = GroupModelControlsState.Success(
                            network = network,
                            modelId = modelId,
                            group = group,
                            groupInfoListData = GroupInfoListData(
                                group = group,
                                models = models
                            )
                        )
                    )
                    _uiState.emit(value = state)
                    this@GroupControlsViewModel.network = network
                }
            }
        }
    }

    internal fun save() {
        viewModelScope.launch { repository.save() }
    }

    @Suppress("unused")
    fun onApplicationKeyClicked(key: ApplicationKey) {
        viewModelScope.launch {
            val modelsMap = mutableMapOf<ModelId, List<Model>>()
            network.run {
                nodes.filter { it.knows(key = key) }
                    .flatMap { it.elements }
                    .flatMap { it.models }
                    .filter { key.isBoundTo(model = it) }
                    .forEach { model ->
                        modelsMap[model.modelId] = modelsMap[model.modelId]?.let {
                            it + model
                        } ?: mutableListOf()
                    }
            }
        }
    }

    fun onModelClicked(index: Int) {
        val state = _uiState.value.groupState as? GroupModelControlsState.Success ?: return
        _uiState.value = _uiState.value.copy(
            groupState = state.copy(selectedModelIndex = index)
        )
    }

    fun deleteGroup(group: Group): Boolean = network.remove(group = group)

    fun send(message: UnacknowledgedMeshMessage, key: ApplicationKey) {
        viewModelScope.launch {
            repository.send(group = group!!, unackedMessage = message, key = key)
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(key: String): GroupControlsViewModel
    }
}

internal data class GroupControlsScreenUiState(val groupState: GroupModelControlsState = GroupModelControlsState.Loading)

internal sealed interface GroupModelControlsState {
    data object Loading : GroupModelControlsState
    data class Success(
        val network: MeshNetwork,
        val group: Group,
        val modelId: ModelId,
        val nextAvailableGroupAddress: GroupAddress? = null,
        val groupInfoListData: GroupInfoListData,
        val selectedModelIndex: Int = -1,
    ) : GroupModelControlsState

    @Suppress("unused")
    data class Error(val throwable: Throwable) : GroupModelControlsState
}