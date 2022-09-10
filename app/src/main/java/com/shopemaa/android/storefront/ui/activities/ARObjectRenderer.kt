package com.shopemaa.android.storefront.ui.activities

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RawRes
import androidx.annotation.RequiresApi
import com.google.ar.core.HitResult
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.shopemaa.android.storefront.R

class ARObjectRenderer : BaseActivity() {
    private lateinit var arFragment: ArFragment

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_arobject_renderer)

        arFragment = ArFragment()
        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            setModelOnUi(hitResult)
        }
    }

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun setModelOnUi(hitResult: HitResult) {
        loadModel(1) { modelRenderable ->
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            val transformableNode = TransformableNode(arFragment.transformationSystem)
            transformableNode.setParent(anchorNode)
            transformableNode.renderable = modelRenderable
            transformableNode.select()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun loadModel(@RawRes model: Int, callback: (ModelRenderable) -> Unit) {
        ModelRenderable
            .builder()
            .setSource(this, model)
            .build()
            .thenAccept { modelRenderable ->
                callback(modelRenderable)
            }
    }
}
