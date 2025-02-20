package no.nordicsemi.android.nrfmesh.core.common

import no.nordicsemi.kotlin.mesh.core.model.Model
import no.nordicsemi.kotlin.mesh.core.model.SigModelId

fun Model.isGenericOnOffServer() = isBluetoothSigAssigned
        && (modelId as SigModelId).modelIdentifier == Model.GENERIC_ON_OFF_SERVER_MODEL_ID

fun Model.isGenericLevelServer() = isBluetoothSigAssigned
        && (modelId as SigModelId).modelIdentifier == Model.GENERIC_LEVEL_SERVER_MODEL_ID

fun Model.isSceneServer() = isBluetoothSigAssigned
        && (modelId as SigModelId).modelIdentifier == Model.SCENE_SERVER_MODEL_ID

fun Model.isSceneSetupServer() = isBluetoothSigAssigned
        && (modelId as SigModelId).modelIdentifier == Model.SCENE_SETUP_SERVER_MODEL_ID

fun Model.isLightLCServer() = isBluetoothSigAssigned
        && (modelId as SigModelId).modelIdentifier == Model.LIGHT_LC_SERVER_MODEL_ID

fun isSupportedGroupItem(model: Model) = model.isGenericOnOffServer() ||
        model.isGenericLevelServer() ||
        model.isLightLCServer() ||
        model.isSceneServer() ||
        model.isSceneSetupServer()