package no.nordicsemi.android.nrfmesh.feature.groups

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import no.nordicsemi.android.nrfmesh.core.common.isSupportedGroupItem
import no.nordicsemi.android.nrfmesh.core.data.CoreDataRepository
import no.nordicsemi.android.nrfmesh.feature.groups.navigation.GroupRoute
import no.nordicsemi.kotlin.mesh.core.messages.UnacknowledgedMeshMessage
import no.nordicsemi.kotlin.mesh.core.model.ApplicationKey
import no.nordicsemi.kotlin.mesh.core.model.Group
import no.nordicsemi.kotlin.mesh.core.model.GroupAddress
import no.nordicsemi.kotlin.mesh.core.model.MeshNetwork
import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.ModelId
import javax.inject.Inject

@HiltViewModel
internal class GroupViewModel @Inject internal constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: CoreDataRepository,
) : ViewModel() {
    private val groupAddress = savedStateHandle.toRoute<GroupRoute>().address.toUShort(radix = 16)
    private var group: Group? = null
    private val _uiState = MutableStateFlow(GroupScreenUiState())
    val uiState: StateFlow<GroupScreenUiState> = _uiState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GroupScreenUiState()
        )

    private lateinit var network: MeshNetwork

    init {
        viewModelScope.launch {
            repository.network.collect { network ->
                network.group(address = groupAddress)?.let { group ->
                    this@GroupViewModel.group = group
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
                        groupState = GroupState.Success(
                            network = network,
                            group = network.group(address = groupAddress)!!,
                            groupInfoListData = GroupInfoListData(
                                group = network.group(address = groupAddress)!!,
                                models = models
                            )
                        )
                    )
                    _uiState.emit(value = state)
                    this@GroupViewModel.network = network
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
        val state = _uiState.value.groupState as? GroupState.Success ?: return
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
}

internal data class GroupScreenUiState(val groupState: GroupState = GroupState.Loading)

internal sealed interface GroupState {
    data object Loading : GroupState
    data class Success(
        val network: MeshNetwork,
        val group: Group,
        val nextAvailableGroupAddress: GroupAddress? = null,
        val groupInfoListData: GroupInfoListData,
        val selectedModelIndex: Int = -1,
    ) : GroupState

    @Suppress("unused")
    data class Error(val throwable: Throwable) : GroupState
}